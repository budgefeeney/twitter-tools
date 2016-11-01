package cc.twittertools.post;


import cc.twittertools.post.embed.Retweet;
import cc.twittertools.post.embed.Webpage;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Optional;

/**
 * A representation of a Tweet. Tweets are now complex:
 *
 * <ul>
 * <li>There is a simple tweet, which is the user posting text
 * <li>There is a simple tweet with a link to a page: Twitter may embed a fragment of that page in the timeline.</li>
 * <li>There is a simple retweet, where another author's tweet appears directly in this account
 * <li>There is a complex retweet, where another author's tweet appears in this account with
 * commentary
 * <li>There is a manual tweet, where using copy & paste and sigils like "RT" users (or more likely the
 * apps they use) indicate a tweet's content has come from elsewhere
 * <li>Tweets may be nested, this author may retweet without commentary a tweet which itself retweets with commentary
 * a third tweet
 * </ul>
 *
 * Consequently note there is a distinction between author and account, and ID and requestedID. In each case
 * the latter refers to the page we're looking at, and the former refers to the original tweet and its author.
 */
public final class Tweet extends Retweet {

    private final DateTime localTime;
    private final DateTime utcTime;

    public Tweet(long id, String author, String msg, DateTime utcTime, DateTime localTime) {
        this(id, author, msg, utcTime, localTime, Optional.empty(), Optional.empty());
    }

    public Tweet(long id, String author, String msg, DateTime utcTime, DateTime localTime, Webpage embeddedWebpage) {
        this(id, author, msg, utcTime, localTime, Optional.of(embeddedWebpage), Optional.empty());
    }

    public Tweet(long id, String author, String msg, DateTime utcTime, DateTime localTime, Retweet embeddedRetweet) {
        this(id, author, msg, utcTime, localTime, Optional.empty(), Optional.of(embeddedRetweet));
    }

    public Tweet(long id, String author, String msg, DateTime utcTime, DateTime localTime, Webpage embeddedPage, Retweet embeddedRetweet) {
        this(id, author, msg, utcTime, localTime, Optional.of(embeddedPage), Optional.of(embeddedRetweet));
    }

    public Tweet(long id, String author, String msg, DateTime utcTime, DateTime localTime, Optional<Webpage> embeddedPage, Optional<Retweet> embeddedRetweet) {
        super(id, author, msg, embeddedPage, embeddedRetweet);
        this.localTime = localTime;
        this.utcTime   = utcTime;
    }


    public DateTime getLocalTime() {
        return localTime;
    }
    public DateTime getUtcTime() {
        return utcTime;
    }

    @Override
    public String toString() {
        return getLocalTime().toString() + super.toString();
    }

    @Override
    public String toShortTabDelimString() {
        return super.toString()
                + '\t' + ISODateTimeFormat.dateTimeNoMillis().print(this.getLocalTime())
                + '\t' + ISODateTimeFormat.dateTimeNoMillis().print(this.getUtcTime());
    }

    // ISODateTimeFormat.dateTimeNoMillis().parseDateTime(parts[3])

}