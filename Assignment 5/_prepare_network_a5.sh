#!/bin/bash
# It is not easy to save and link state (up or down)
# So always bring links up

#set -x 
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
TMPDIR=/tmp/compx204-a5-$USER
DNSMASQ=$TMPDIR/dns.masq
OVERLAYWD=$TMPDIR/wd/
OVERLAYUD=$TMPDIR/ud/
SAVE_DIR="$1"


LOAD_CONFIG=
if [[ -e "$SAVE_DIR/overlay.tar.gz" ]]; then
	LOAD_CONFIG=1
	echo "Found existing configuration: $SAVE_DIR"
else
	echo "No existing configuration found."
	echo "This script will initialise a fresh copy of the assignment."
	echo "Your configuration will be saved to: $SAVE_DIR"
fi
read -p "Do you want to continue? [y/N] " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
	exit 1
fi

[ -e "$TMPDIR" ] && rm -r "$TMPDIR"
if [[ -e "$TMPDIR" ]]; then
	echo "ERROR: Cannot delete existing $TMPDIR"
	echo "Please manually cleanup"
	exit 1
fi
mkdir "$TMPDIR"
if [[ ! -e "$TMPDIR" ]]; then
	echo "ERROR: Cannot create $TMPDIR"
	echo "Check your permissions on $TMPDIR"
	exit 1
fi
mkdir "$TMPDIR"/pids
cp /etc/resolv.conf "$TMPDIR"
cp /etc/hosts "$TMPDIR"

mkdir -p "$SAVE_DIR"

mount -t sysfs none /sys/
mount -t tmpfs none /run/
#mount -t proc none /proc/

# Disable reverse path filtering
sysctl -q net.ipv4.conf.default.rp_filter=0
sysctl -q net.ipv4.conf.all.rp_filter=0
sysctl -q net.ipv4.ip_forward=1

hostname compx204-assignment-5
# Map host_int -> address/subnet
declare -A linkaddr


echo "resolv-file=" >> $DNSMASQ
echo "no-resolv" >> $DNSMASQ
echo "no-poll" >> $DNSMASQ
echo "server=" >> $DNSMASQ
echo "address=/google.com/10.0.0.1" >> $DNSMASQ

# For mantra TODO delete
#mkdir /run/resolvconf/
#echo "nameserver 192.168.1.240" > /run/resolvconf/resolv.conf

function save_and_clean {
	echo "Please wait while your work is saved"
	cd "$SAVE_DIR"
	backupdir=backup_$(date +"%Y-%m-%d-%H:%M:%S")
	if [[ -n $LOAD_CONFIG ]]; then
		mkdir "$backupdir"
		cp ./overlay.tar.gz "$backupdir"
		cp ./*addr "$backupdir"
		cp ./*routes "$backupdir"
		rm "./overlay.tar.gz"
		rm ./*addr
		rm ./*routes
	fi
	tar -cf "./overlay.tar.gz" -C "$TMPDIR" ./ud

	for path in /run/netns/* ; do
		ns="${path##*/}"
		echo "Saving" $ns
		ip netns exec $ns ip -4 addr save > ./$ns.addr
		ip netns exec $ns ip -4 route save > ./$ns.routes
	done
	
	shopt -s nullglob
	for p in "$TMPDIR"/pids/*
	do
		pd="${p##*/}"
		echo "$p"
		echo "$pd"
		kill "$pd"
	done

	rm -r "$TMPDIR"
	kill $dnsmasq_pid
	echo "Save completed to: $SAVE_DIR"
}

function load_config {
	echo "Loading existing configuration from: $SAVE_DIR"
	cd "$SAVE_DIR"

	# Load /etc/ changes
	if [[ -d "$TMPDIR/wc" ]]; then
		echo "Unexpected overlay wc dir already exists"
	fi
	tar -xf "./overlay.tar.gz" -C "$TMPDIR"

	# Load ip addresses and routes
	for path in *.addr; do
		ns="${path%.addr}"
		#echo $ns
		ip netns exec $ns ip addr restore < ./$ns.addr 2> /dev/null
	done

	for path in *.routes; do
		ns="${path%.routes}"
		#echo $ns
		ip netns exec $ns ip route restore < ./$ns.routes 2> /dev/null
	done

	cd - 1> /dev/null

}

# create_host name switch
function create_host {
	local name=$1
	local switch=$2
	# Create eth0 inside the host
	ip netns add $name
	ip netns exec $name sysctl -q net.ipv4.conf.default.rp_filter=0
	ip netns exec $name sysctl -q net.ipv4.conf.all.rp_filter=0
	ip netns exec $name sysctl -q net.ipv4.ip_forward=1
	ip netns exec $name ip l set up lo

	ip link add $name type veth peer name eth0
	ip link set eth0 netns $name
	ip netns exec $name ip l set up eth0

	# Add to bridge
	ip link set $name master $switch
	ip link set $name up

	mkdir -p $OVERLAYWD/$name
	mkdir -p $OVERLAYUD/$name
	if [[ -z $LOAD_CONFIG ]]; then
		cp "$TMPDIR"/hosts "$OVERLAYUD/$name/"
		cp "$TMPDIR"/resolv.conf "$OVERLAYUD/$name/"
	fi
}

function create_router {
	local name=r$1
	local bridge=$2
	local ip=$3

	ip netns add $name
	ip netns exec $name sysctl -q net.ipv4.conf.default.rp_filter=0
	ip netns exec $name sysctl -q net.ipv4.conf.all.rp_filter=0
	ip netns exec $name sysctl -q net.ipv4.ip_forward=1
	ip netns exec $name ip l set up lo

	if [[ -n $bridge ]]; then 
		ip link add $name type veth peer name to_$bridge
		ip link set $name master $bridge
		ip link set $name up

		ip link set to_$bridge netns $name
		ip netns exec $name ip l set up to_$bridge
		if [[ -z $LOAD_CONFIG ]]; then
			ip netns exec $name ip a add "$ip" dev to_$bridge
		fi
	fi
}

function reverse_ip_addr {
	addr=${1%%/*}
	addr1=${addr%%.*}
	addr4=${addr##*.}
	addr2=${addr#*.}
	addr3=${addr2#*.}
	addr2=${addr2%%.*}
	addr3=${addr3%%.*}

	echo $addr4.$addr3.$addr2.$addr1
}

function add_dns_mapping {
	local hostname=$1
	local addr=${2%%/*}
	local addr1=${addr%%.*}
	local addr4=${addr##*.}
	local addr2=${addr#*.}
	local addr3=${addr2#*.}
	addr2=${addr2%%.*}
	addr3=${addr3%%.*}

	local addr_ptr=$addr4.$addr3.$addr2.$addr1
	echo "address=/$hostname/$addr" >> "$DNSMASQ"
	echo "ptr-record=$addr_ptr.in-addr.arpa.,$hostname" >> "$DNSMASQ"
}


# Create a link between two routers
# create_link router1 router2 delay address
function create_link {
	local router1=$1
	local router2=$2
	local delay=$3
	local address1=$4
	local address2=$5

	ip link add to_$router1 type veth peer name to_$router2
	ip link set to_$router1 netns $router2
	ip link set to_$router2 netns $router1
	ip netns exec $router1 ip l set up to_$router2
	ip netns exec $router2 ip l set up to_$router1
	if [[ -z $LOAD_CONFIG ]]; then
		ip netns exec $router1 ip addr add $address1 dev to_$router2 
		ip netns exec $router2 ip addr add $address2 dev to_$router1
	fi

	linkaddr["${router1}_to_${router2}"]=$address1
	linkaddr["${router2}_to_${router1}"]=$address2

	add_dns_mapping ${router1}-link_to_${router2} ${address1}
	add_dns_mapping ${router2}-link_to_${router1} ${address2}

	# Set tc delay
	ip netns exec $router1 tc qdisc add dev to_$router2 root netem delay $delay
	ip netns exec $router2 tc qdisc add dev to_$router1 root netem delay $delay
}

function add_route {
	local router=$1
	local address=$2
	local via=$3

	if [[ -n "${linkaddr[${via}_to_${router}]}" ]]; then
		via=${linkaddr[${via}_to_${router}]}
		via=${via%/30}
	fi
	if [[ -z $LOAD_CONFIG ]]; then
		ip netns exec $router ip route add $address via $via
	fi
}

function create_router_2hosts {
	local name=$1
	local address=$2

	ip link add name br${name} type bridge
	ip link set br${name} up

	create_host ${name}1 br${name}
	create_host ${name}2 br${name}
	create_router ${name} br${name} $address.254/24

	add_dns_mapping ${name}1 ${address}.100
	add_dns_mapping ${name}2 ${address}.200
	add_dns_mapping r${name}-bridge ${address}.254
}

function configure_host {
	local host=$1
	local address=$2

	if [[ -z $LOAD_CONFIG ]]; then
		ip netns exec $host ip addr add $2 dev eth0
	fi
}


create_router_2hosts HLZ 192.168.1
create_router_2hosts TRG 192.168.2
create_router_2hosts WLG 192.168.3
create_router_2hosts AKL 192.168.4
create_router ROT
create_router TUO
create_router NPE
create_router PRL

create_link rHLZ rTRG 0.5ms 10.1.0.1/30 10.1.0.2/30
create_link rTRG rROT 2ms 10.2.0.1/30 10.2.0.2/30
create_link rROT rTUO 1ms 10.3.0.1/30 10.3.0.2/30
create_link rTUO rNPE 1.5ms 10.4.0.1/30 10.4.0.2/30
create_link rNPE rPRL 1.5ms 10.5.0.1/30 10.5.0.2/30
create_link rPRL rWLG 1ms 10.6.0.1/30 10.6.0.2/30
create_link rWLG rHLZ 5ms 10.7.0.1/30 10.7.0.2/30
create_link rHLZ rAKL 1.5ms 10.8.0.1/30 10.8.0.2/30
create_link rTRG rAKL 2ms 10.9.0.1/30 10.9.0.2/30

add_route rAKL 192.168.1.0/24 rHLZ
add_route rTRG 192.168.1.0/24 rHLZ
add_route rWLG 192.168.1.0/24 rHLZ

add_route rAKL 192.168.2.0/24 rTRG
add_route rROT 192.168.2.0/24 rTRG
add_route rTUO 192.168.2.0/24 rROT
add_route rNPE 192.168.2.0/24 rTUO
add_route rPRL 192.168.2.0/24 rNPE
add_route rWLG 192.168.2.0/24 rPRL
add_route rHLZ 192.168.2.0/24 rTRG

add_route rAKL 192.168.3.0/24 rHLZ
add_route rTRG 192.168.3.0/24 rROT
add_route rROT 192.168.3.0/24 rTUO
add_route rTUO 192.168.3.0/24 rNPE
#add_route rNPE 192.168.3.0/24 rPRL
add_route rPRL 192.168.3.0/24 rWLG
add_route rHLZ 192.168.3.0/24 rWLG

add_route rWLG 192.168.4.0/24 rHLZ
add_route rHLZ 192.168.4.0/24 rAKL
add_route rTRG 192.168.4.0/24 rAKL

# AKL fully configured
configure_host AKL1 192.168.4.100/24
configure_host AKL2 192.168.4.200/24
add_route AKL1 default 192.168.4.254
add_route AKL2 default 192.168.4.254

# WLG subnet issues
configure_host WLG1 192.168.3.100/24
configure_host WLG2 192.168.3.200/25
add_route WLG1 default 192.168.3.254
add_route WLG2 default 192.168.3.254
# Don't forward packets back on the router, so this
# works better
ip netns exec rWLG iptables -I FORWARD -s 192.168.3.0/24 -d 192.168.3.0/24 -j DROP

# HLZ default+DNS issues
configure_host HLZ1 192.168.1.100/24
configure_host HLZ2 192.168.1.200/24
add_route HLZ2 default 192.168.1.254

# TRG not configured

if [[ -z $LOAD_CONFIG ]]; then
	echo "nameserver 192.168.1.240" > "$TMPDIR/ud/AKL1/resolv.conf"
	echo "nameserver 192.168.1.240" > "$TMPDIR/ud/AKL2/resolv.conf"
	echo "nameserver 192.168.1.240" > "$TMPDIR/ud/WLG1/resolv.conf"
	echo "nameserver 192.168.1.240" > "$TMPDIR/ud/WLG2/resolv.conf"
	echo "nameserver 192.168.1.240" > "$TMPDIR/ud/HLZ1/resolv.conf"
	echo $RANDOM > "$SAVE_DIR"/uid
fi

# Create bridge for HLZ
#ip link add name brHLZ type bridge
#ip link set brHLZ up
#create_host HLZ1 brHLZ
#create_host HLZ2 brHLZ

# 
#create_router rHLZ brHLZ 192.168.1.254/24
#create_router rTRG brHLZ 

# Connect routers
#create_link rHLZ rTRG 40ms 10.1.0.1/30 10.1.0.2/30

# Add an address to brHLZ for dnsmasq
ip addr add 192.168.1.240/24 dev brHLZ
add_dns_mapping dns-server 192.168.1.240
ip route add default via 192.168.1.254

if [[ -n $LOAD_CONFIG ]]; then
	load_config
fi

trap save_and_clean EXIT

# Start the DNS server
dnsmasq -d -C "$DNSMASQ" -i brHLZ 2> /dev/null 1> /dev/null &
dnsmasq_pid=$!

ping -c 3 192.168.4.100 1> /dev/null

echo "Assignment 5 loaded successfully"
echo "Type 'exit' when you are done"
bash

exit 0
ip netns add HLZ1
ip netns add HLZ2

ip link add host1 type veth peer name eth0
ip link set eth0 netns host1
ip netns exec host1 ip l set up eth0

ip link add host2 type veth peer name eth0
ip link set eth0 netns host2
ip netns exec host2 ip l set up eth0


ip link add name br0 type bridge
ip link set host1 master br0
ip link set host2 master br0
ip link set host1 up
ip link set host2 up


