package cc.twittertools.spider;

import java.net.URI;
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
        if (tweet.select("div.ScrollBump-header").isEmpty()) {
          LOG.error("No tweet content in ostensible tweet HTML block:\n" + tweet.toString());
        }
        continue tweetloop;
      }
      Element header = possibleHeaderTags.get(0);
      String href   = header.attr("href");
      String author = StringUtils.substringBefore(href.substring(1), "/");
      String idStr  = StringUtils.substringAfterLast(href, "/");
      long   id     = Long.parseLong(idStr);

      Elements emoticons = tweet.select("img.Emoji");
      for (Element emo : emoticons) {
        emo.prependText(emo.attr("alt"));
      }

      DateTime localDate = cc.twittertools.post.old.Tweet.TWITTER_FMT.parseDateTime(header.attr("title"));
      DateTime utcDate   = new DateTime(Long.parseLong(header.select("span.js-short-timestamp").attr("data-time-ms")));

      Element bodyTag = tweet.select("p.tweet-text").get(0);
      bodyTag.select("span.u-hidden").remove();
      String body = insertSpaceBeforeHttpInstances(bodyTag.text());

      // Check if there's an embedded retweet
      Elements embeds = tweet.select("div.QuoteTweet-innerContainer");
      Retweet embeddedTweet = null;
      if (! embeds.isEmpty()) {
        Element embed = embeds.get(0);

        String eHref  = embed.attr("href");
        String eAuthor = StringUtils.substringBefore(eHref.substring(1), "/");
        String eIdStr  = StringUtils.substringAfterLast(eHref, "/");
        long   eId     = Long.parseLong(idStr);

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
        Elements primeTweetIdTags = tweet.select("div.class");
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

  private String insertSpaceBeforeHttpInstances (String body) {
    return body.replaceAll("https://", " https://")
            .replaceAll("http://", " http://");
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

    URI uri = URI.create(body.substring(start, end));
    URI cardUri = URI.create("https://twitter.com" + embeddedUrls.get(0).attr("data-src"));

    return Optional.of(new WebExcerpt(uri, cardUri));
  }
}
