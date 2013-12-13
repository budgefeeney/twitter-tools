#!/bin/bash

params="encode"
params="$params --infile /Users/bryanfeeney/opt/twitter-tools-spider/src/test/resources/spider"
params="$params --outfile /Users/bryanfeeney/Desktop/SmallDB-20"
params="$params --skip-retweets"
params="$params --start-date 20130501"
params="$params --end-date 20131001"
params="$params --enable-stemming"
params="$params --min-word-len 1"
params="$params --max-word-len 1000"
params="$params --min-word-count 20"
params="$params --incl-numbers"
params="$params --feat-author"
params="$params --feat-woy"
params="$params --feat-moy"
params="$params --dict-addrs __null_dict"
params="$params --dict-urls /Users/bryanfeeney/Desktop/DatasetStats2/urls-sorted.dict"
params="$params --dict-words /Users/bryanfeeney/Desktop/DatasetStats2/words-sorted.dict"
params="$params --dict-stocks /Users/bryanfeeney/Desktop/DatasetStats2/stocks-sorted.dict"
params="$params --dict-smileys /Users/bryanfeeney/Desktop/DatasetStats2/smileys-sorted.dict"
params="$params --dict-tags /Users/bryanfeeney/Desktop/DatasetStats2/hashtags-sorted.dict"
params="$params --selected-acs /Users/bryanfeeney/Dropbox/usershorter.txt"

jar_name="twitter-tools"
jar_version="1.1.1"
jar_dir="target"
jar_file="$jar_dir/$jar_name-$jar_version-jar-with-dependencies.jar"

echo $params
java -Xmx3000m -cp $jar_file cc.twittertools.scripts.Main $params

