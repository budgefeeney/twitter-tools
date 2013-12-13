#!/bin/bash

SRC_DIR=`dirname $0`

params="encode"
params="$params --infile /Users/bryanfeeney/opt/twitter-tools-spider/src/test/resources/spider"
params="$params --outfile /Users/bryanfeeney/Desktop/SmallDB-NoCJK"
params="$params --skip-retweets"
params="$params --start-date 20130501"
params="$params --end-date 20131001"
params="$params --enable-stemming"
params="$params --min-word-len 1"
params="$params --max-word-len 1000"
params="$params --min-word-count 50"
params="$params --incl-numbers"
params="$params --feat-author"
params="$params --feat-woy"
params="$params --feat-moy"
params="$params --dict-addrs __null_dict"
params="$params --dict-urls /Users/bryanfeeney/Desktop/SmallDB-NoCJK/Stats/urls-ordered.dict"
params="$params --dict-words /Users/bryanfeeney/Desktop/SmallDB-NoCJK/Stats/words-ordered.dict"
params="$params --dict-stocks /Users/bryanfeeney/Desktop/SmallDB-NoCJK/Stats/stocks-ordered.dict"
params="$params --dict-smileys /Users/bryanfeeney/Desktop/SmallDB-NoCJK/Stats/smileys-ordered.dict"
params="$params --dict-tags /Users/bryanfeeney/Desktop/SmallDB-NoCJK/Stats/hashtags-ordered.dict"
params="$params --selected-acs /Users/bryanfeeney/Dropbox/SideTopicDatasets/user-even-more-shorter.txt"

jar_name="twitter-tools"
jar_version="1.1.1"
jar_dir="$SRC_DIR/target"
jar_file="$jar_dir/$jar_name-$jar_version-jar-with-dependencies.jar"

echo $params
java -Xmx3000m -cp $jar_file cc.twittertools.scripts.Main $params

