package cc.twittertools.spider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import cc.twittertools.post.Tweet;
import cc.twittertools.post.embed.Retweet;
import cc.twittertools.post.embed.WebExcerpt;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private final static Logger LOG = LoggerFactory.getLogger(TweetsHtmlParser.class);

  public List<Tweet> parse (String account, String pageHtml)
  { Preconditions.checkNotNull(pageHtml, "Page HTML in parse was null");
    if ((pageHtml = pageHtml.trim()).isEmpty())
      return Collections.emptyList();

    Document document = Jsoup.parse(pageHtml);

    Elements tweets = document.select("li.js-stream-item");

    List<Tweet> result = new ArrayList<>();
    tweetloop:for (Element tweet : tweets)
    {
      Elements possibleHeaderTags = tweet.select("a.tweet-timestamp");
      if (possibleHeaderTags.isEmpty()) {
        // we expect this to occur once, when we hit the scroll "bump" at the bottom, but not otherwise
        if (! tweet.hasClass("scroll-bump-user-card")
                && tweet.select("div.follow-bar").isEmpty()
                && tweet.select("div.ScrollBump-header").isEmpty()) {
          LOG.error("No tweet content in ostensible tweet HTML block:\n" + tweet.toString());
        }
        continue tweetloop;
      }
      Element header = possibleHeaderTags.get(0);
      String href   = header.attr("href");
      String author = StringUtils.substringBefore(href.substring(1), "/");
      String idStr  = StringUtils.substringAfterLast(href, "/");
      long   id     = Long.parseLong(idStr);

      placeEmoticonsInText(tweet);

      DateTime localDate = cc.twittertools.post.old.Tweet.TWITTER_FMT.parseDateTime(header.attr("title"));
      DateTime utcDate   = new DateTime(Long.parseLong(header.select("span.js-short-timestamp").attr("data-time-ms")));

      final String body;
      Elements bodyTags = tweet.select("p.tweet-text");
      if (bodyTags.isEmpty()) {
        body = "";
      }else {
        Element bodyTag = bodyTags.get(0);
        bodyTag.select("span.u-hidden").remove();
        body = insertSpaceBeforeHttpInstances(bodyTag.text());
      }


      // Check if there's an embedded retweet
      Elements embeds = tweet.select("div.QuoteTweet-innerContainer");
      Retweet embeddedTweet = null;
      if (! embeds.isEmpty()) {
        Element embed = embeds.get(0);

        String eHref  = embed.attr("href");
        String eAuthor = StringUtils.substringBefore(eHref.substring(1), "/");
        String eIdStr  = StringUtils.substringAfterLast(eHref, "/");
        long   eId     = Long.parseLong(eIdStr);

        Element eBodyTag = embed.select("div.QuoteTweet-text").get(0);
        eBodyTag.select("span.u-hidden").remove();
        String eBody = insertSpaceBeforeHttpInstances(eBodyTag.text());

        embeddedTweet = new Retweet(eId, eAuthor, eBody, readOptionalWebpageExcerpt(embed, body), Optional.empty());
      }

      if (author.equalsIgnoreCase(account)) {
        result.add(new Tweet(id, author, body, utcDate, localDate, readOptionalWebpageExcerpt(tweet, body), Optional.ofNullable(embeddedTweet)));
      } else {
        Retweet outerRetweet = new Retweet(
                id, author, body, readOptionalWebpageExcerpt(tweet, body), Optional.ofNullable(embeddedTweet));

        // The ID we read in earlier is actually the ID of the retweeted tweet.
        // We have to look into another tag to find the ID of the account-holder's retweet.
        Elements primeTweetIdTags = tweet.select("div.tweet");
        final long primeTweetId;
        if (primeTweetIdTags.isEmpty() || ! primeTweetIdTags.get(0).hasAttr("data-retweet-id")) {
          primeTweetId = 1L;
          LOG.error("Can't find a parent data-retweet-id for @" + account + " retweeting " + " @" + author + ": " + body);
        } else {
          primeTweetId = Long.parseLong(primeTweetIdTags.get(0).attr("data-retweet-id"));
        }

        result.add(new Tweet(primeTweetId, account, "", utcDate, localDate, Optional.empty(), Optional.of(outerRetweet)));
      }
    }

    return result;
  }

  /**
   * To support users of older operating systems, Twitter replaces emoticons,
   * with IMG tags with graphical realisations of those emoticons. When you
   * look at the text attribute of the enclosing tag consequently, you don't
   * get to see the emoticons. Fortunately twitter does put the emoticon
   * text in an ALT attribute of the IMG tag, so we simply prepend that ALT
   * text to the image tag, so that the emoticons will be included in a call
   * to text().
   * @param tweetTextContainer
   */
  public static void placeEmoticonsInText(Element tweetTextContainer) {
    Elements emoticons = tweetTextContainer.select("img.Emoji");
    for (Element emo : emoticons) {
      emo.prependText(emo.attr("alt"));
    }
  }

  /**
   * The use of &lt;span&gt; tags in tweets means that there is no
   * space between URLs and the text that precedes them, when there
   * should be. This simply fixes this by prepending spaces
   * @param body
   * @return
   */
  public static String insertSpaceBeforeHttpInstances (String body) {
    return body.replaceAll("https://", " https://")
               .replaceAll("http://", " http://")
               .replaceAll("(?<!(?:https://|http://))pic\\.twitter\\.com", " pic.twitter.com");
  }

  private Optional<WebExcerpt> readOptionalWebpageExcerpt(Element parent, String body) {
    Elements embeddedUrls = parent.select("div.js-macaw-cards-iframe-container");
    if (embeddedUrls.isEmpty()) {
      return Optional.empty();
    }

    int start = StringUtils.indexOf(body, "http");
    if (start < 0)
      return Optional.empty();

    int end = start;
    while (end < body.length()) {
      int c = body.codePointAt(end);
      if (c < 33 || c > 127) { // "isWhiteSpace or !isAscii"
        break;
      }
      ++end;
    }

    final URI uri, cardUri;
    try {
      uri     = new URI(body.substring(start, end));
      cardUri = new URI("https://twitter.com" + embeddedUrls.get(0).attr("data-src"));
    } catch (URISyntaxException ue) {
      return Optional.empty();
    }

    return Optional.of(new WebExcerpt(uri, cardUri));
  }
}
