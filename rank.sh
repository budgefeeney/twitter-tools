#!/bin/sh

SRC_DIR=`dirname $0`

java -Xmx3000m -cp $SRC_DIR/target/twitter-tools-1.1.1-jar-with-dependencies.jar cc.twittertools.scripts.Rank $@
