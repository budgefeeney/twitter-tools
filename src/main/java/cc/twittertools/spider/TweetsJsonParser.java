package cc.twittertools.spider;

import java.util.Collections;
import java.util.List;

import cc.twittertools.post.Tweet;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TweetsJsonParser
{
  private final TweetsHtmlParser htmlParser = new TweetsHtmlParser();
  
  public List<Tweet> parse (final String user, String json)
  { Preconditions.checkNotNull(json, "JSON in parse was null");  
    if ((json = json.trim()).isEmpty())
      return Collections.emptyList();
    
    JsonObject doc = new JsonParser().parse(json).getAsJsonObject();
    
    String tweetsHtml = doc.get("items_html").getAsString();
    return htmlParser.parse (user, tweetsHtml);
  }
}