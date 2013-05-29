package cc.twittertools.spider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import cc.twittertools.post.Tweet;

import com.google.common.base.Preconditions;

/**
 * Takes a profile page for a user, and parses it.
 * @author bfeeney
 *
 */
public class TweetsHtmlParser
{
  public List<Tweet> parse (String user, String pageHtml)
  { Preconditions.checkNotNull(pageHtml, "Page HTML in parse was null");
    if ((pageHtml = pageHtml.trim()).isEmpty())
      return Collections.emptyList();
    
    Document document = Jsoup.parse(pageHtml);

    Elements times  = document.select("div.content small.time a.tweet-timestamp");
    Elements bodies = document.select("div.content p.tweet-text");
    
    int numTweets = times.size();
    List<Tweet> result = new ArrayList<>(numTweets);
    for (int i = 0; i < numTweets; i++)
    { String href  = times.get(i).attr("href");
      String idStr = StringUtils.substringAfterLast(href, "/");
      String date  = times.get(i).attr("title");
      long   id    = Long.parseLong(idStr);
      String body  = bodies.get(i).text();
      
      result.add(new Tweet (id, id, date, user, body));
    }
    
    return result;
  }
}
