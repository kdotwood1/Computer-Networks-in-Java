#!/bin/bash
# It is not easy to save and link state (up or down)
# So always bring links up

#set -x 
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
TMPDIR=/tmp/compx204-a5-$USER
SAVE_DIR=~/manual_save_$(date +"%Y-%m-%d-%H:%M:%S")

mkdir "$SAVE_DIR"


function save_and_clean {
	echo "Please wait while your work is saved"
	cd "$SAVE_DIR"
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
}

save_and_clean

echo "Saved to:" "$SAVE_DIR"
echo "Please check the folders contents"
echo "Make a copy of that save, and run the start script pointing to the new directory"
