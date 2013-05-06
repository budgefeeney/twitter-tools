package cc.twittertools.corpus.data;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Object representing a status.
 */
public class Status {
  
  public static final String TIMESTAMP = "timestamp";
  public static final String DATETIME = "createdAt";
  public static final String MESSAGE = "text";
  public static final String USER = "screenName";
  public static final String ID = "id";
  public static final String REQUESTED_ID = "requested_id";
  
  private final static Logger LOG = Logger.getLogger (Status.class);  
  private static final JsonParser parser = new JsonParser();
   
  

  private long id;
  private String screenname;
  private String createdAt;
  private String text;
  private JsonObject jsonObject;
  private String jsonString;
  private long timestamp;

  protected Status() {}
  
  public long getId() {
    return id;
  }

  public String getText() {
    return text;
  }

  public String getCreatedAt() {
    return createdAt;
  }
  
  public long getTimestamp() {
    return timestamp;
  }

  public String getScreenname() {
    return screenname;
  }

  public JsonObject getJsonObject() {
    if (jsonObject != null)
      return jsonObject;
    
    jsonObject = new JsonObject();
    jsonObject.addProperty (ID, id);
    jsonObject.addProperty (USER, screenname);
    jsonObject.addProperty (MESSAGE, text);
    jsonObject.addProperty (DATETIME, createdAt);
    jsonObject.addProperty (TIMESTAMP, Long.toString (timestamp));
    
    return jsonObject;
  }

  public String getJsonString() {
    return jsonString;
  }
  
  @Override
  public String toString() {
    return screenname + " : " + text;
  }

  public static Status fromJson(String json) {
    Preconditions.checkNotNull(json);

    JsonObject obj = (JsonObject) parser.parse(json);
    if (obj.get("html") == null)
      return null;

    Status status = new Status();
    String html = obj.get("html").getAsString();
    html = StringEscapeUtils.unescapeXml(html);
    
    // use some jsoup magic to parse html and fetch require elements
    org.jsoup.nodes.Document document = Jsoup.parse(html);

    Element dateElement = document.select("a").last();
    status.createdAt = dateElement.text();

    Element textElement = document.select("p").first();
    status.text = textElement.text();

    String idRaw = parseUrlGetLastElementInPath(obj.get("url").getAsString());
    status.id = Long.parseLong(idRaw);

    status.screenname = parseUrlGetLastElementInPath(obj.get("author_url").getAsString());

    // TODO: We need to parse out the other fields.

    status.jsonObject = obj;
    status.jsonString = json;

    return status;
  }
  
  /**
   * Brittle implementation to get twitter data from webpage instead of api
   * @param html
   * @return
   */
  public static Status fromHtml(String html) {
    Preconditions.checkNotNull(html);
    // use some jsoup magic to parse html and fetch require elements
    org.jsoup.nodes.Document document = Jsoup.parse(html);
    Status status = new Status();
    
    Element dateElement = document.select("div.content div.stream-item-header small.time a").first();
    status.createdAt = dateElement.attr("title");
    
    Element stampElement = dateElement.select("span").first();
    String stampStr = stampElement.attr("data-time");
    if (! (stampStr = StringUtils.trimToEmpty(stampStr)).isEmpty())
      try
      { status.timestamp = Long.parseLong (stampStr);
      }
      catch (NumberFormatException e)
      { LOG.warn ("Can't parse time-stamp " + stampStr);
      }
    
    Element textElement = document.select("p.js-tweet-text").first();
    status.text = textElement.text();
    
    Element dataElement = document.select("div.permalink-tweet").first();
    
    status.id = Long.parseLong(dataElement.attr("data-tweet-id"));

    status.screenname = dataElement.attr("data-screen-name");

    return status;
  }

  private static String parseUrlGetLastElementInPath(String string) {
    String[] split = string.split("/");
    String idRaw = split[split.length-1];
    return idRaw;
  }
}
