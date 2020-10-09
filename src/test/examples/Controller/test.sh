#!/bin/bash


# I want this script to work on both unix and using GNU bash in cygwin
# so we first try to find the .exe version and if not available, use the
# unix command
SQLITE="`which sqlite3.exe`"
if [ "x$SQLITE" = "x" ]; then
	SQLITE=sqlite3
fi

OPLRUN=`which oplrun.exe`
if [ "x$OPLRUN" = "x" ]; then
	# we need oplrunjava on unix
	OPLRUN=oplrunjava
fi

# rm -f example.db


# Run sequentially, this works as expected
# "$OPLRUN" model1.mod model1.dat
# "$OPLRUN" model2.mod model2.dat

"$OPLRUN" controller.mod

$SQLITE example.db .dump