#!/bin/bash

PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin/
PID_FILE="/tmp/compx204-a5-$USER.pid"

if [[ -e "$PID_FILE" ]]; then
	OLD_PID=$(cat "$PID_FILE")
	if [[ -e "/proc/$OLD_PID/" ]]; then		
		echo "You appear to have this running already"
		echo "Clean up the old version first"
		echo "Or delete $PID_FILE"
		exit 1
	else
		echo "Found old pid file, cleaning up"
		rm "$PID_FILE"
		if [[ -e "$PID_FILE" ]]; then
			echo "Cannot delete $PID_FILE"
		fi
	fi
fi

if [[ $# -ne 1 ]]; then
	echo "usage: $0 <save_directory>"
	echo "If save_directory doesn't exist, this script will initialise a new assignment"
	echo "If save_directory exists, this script will load from where you left off"
	exit 1
fi

if [[ $1 = ~* ]]; then
	echo
	echo "You ran:"
	echo "$0 \\$1"
	echo
	echo "You almost certainly wanted to run:"
	echo "$0 $1"
	echo
	echo "'\\' has a few special meanings on the command line"
	echo "1) If you put it at the end of a line you type on the terminal, it means the"
	echo "line carries on to next and is interpreted a one long command."
	echo "This allows you to enter a long command over multiple lines."
	echo 
	echo "2) '\\' also acts as a escape character which make the next character be taken"
        echo "as is rather than being expanded to its special meaning. '~' normally has "
	echo "the special meaning of your home directory. And is replaced with:"
	echo ~
	echo
	echo "Exiting now."
	exit 1
fi

unshare -nrmuU bash -c "echo \$\$ > $PID_FILE ; trap 'rm $PID_FILE' EXIT ; bash '${BASH_SOURCE%/*}/_prepare_network_a5.sh' '$1'" 
