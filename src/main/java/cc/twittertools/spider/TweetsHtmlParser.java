package cc.twittertools.spider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cc.twittertools.post.Tweet;

import com.google.common.base.Preconditions;

/**
 * Takes a profile page for a user, and parses it, producing tweets. Note
 * that not all returned tweets may have been authored by the same user,
 * retweets will have the author set to the original author. Note also
 * that we can't extract retweet date, so it will be the original tweet
 * date instead, which means you may see jumps in the tweet timeline,
 * with e.g. a retweet dating from February appearing among a sequence of tweets
 * from May.
 * @author bfeeney
 *
 */
public class TweetsHtmlParser
{
  public List<Tweet> parse (String pageHtml)
  { Preconditions.checkNotNull(pageHtml, "Page HTML in parse was null");
    if ((pageHtml = pageHtml.trim()).isEmpty())
      return Collections.emptyList();
    
    Document document = Jsoup.parse(pageHtml);

    Elements userIds = document.select("div.content small.time a.tweet-timestamp");
    Elements bodies  = document.select("div.content p.tweet-text");
    
    int numTweets = userIds.size();
    List<Tweet> result = new ArrayList<>(numTweets);
    for (int i = 0; i < numTweets; i++)
    { Element time  = userIds.get(i).select("span._timestamp").get(0);    
      
      String href   = userIds.get(i).attr("href");
      String author = StringUtils.substringBefore(href.substring(1), "/");
      String idStr  = StringUtils.substringAfterLast(href, "/");
      DateTime date = new DateTime (Long.parseLong(time.attr("data-time")) * 1000L);
      long   id     = Long.parseLong(idStr);
      String body   = bodies.get(i).text();
      
      result.add(new Tweet (id, id, date, author, body));
    }
    
    return result;
  }
}
