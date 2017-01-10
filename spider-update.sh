#!/bin/sh

SRC_DIR=`dirname $0`

BASE_DIR="/Users/bryanfeeney/opt-hillary/twitter-tools-spider/src/test/resources/spider"

INPUT_PATH="$BASE_DIR/ranked.csv"
USERS_PATH="$BASE_DIR/selectedUserList.csv"
OUTPUT_PATH="$BASE_DIR"

echo "java -Xmx3000m -cp $SRC_DIR/target/twitter-tools-1.1.1-jar-with-dependencies.jar cc.twittertools.spider.UserTweetUpdatesSpider $INPUT_PATH $USERS_PATH $OUTPUT_PATH"
java -Xmx3000m -cp $SRC_DIR/target/twitter-tools-1.1.1-jar-with-dependencies.jar cc.twittertools.spider.UserTweetUpdatesSpider $INPUT_PATH $USERS_PATH $OUTPUT_PATH

