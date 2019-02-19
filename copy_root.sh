#!/bin/sh

if [ -z "$1" ]
then
	echo "Please provide target directory"
    exit 1
else
    cp -riv build.gradle gradle* LICENCE.txt local.properties Readme.md settings.gradle .gitlab* $1
fi

