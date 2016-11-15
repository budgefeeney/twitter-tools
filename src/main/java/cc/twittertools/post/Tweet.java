package cc.twittertools.post;


import cc.twittertools.post.embed.Retweet;
import cc.twittertools.post.embed.WebExcerpt;
import cc.twittertools.post.tabwriter.TabWriter;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.Period;

import java.net.URI;
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

    public Tweet(long id, String author, String msg, DateTime utcTime, DateTime localTime, WebExcerpt embeddedWebExcerpt) {
        this(id, author, msg, utcTime, localTime, Optional.of(embeddedWebExcerpt), Optional.empty());
    }

    public Tweet(long id, String author, String msg, DateTime utcTime, DateTime localTime, Retweet embeddedRetweet) {
        this(id, author, msg, utcTime, localTime, Optional.empty(), Optional.of(embeddedRetweet));
    }

    public Tweet(long id, String author, String msg, DateTime utcTime, DateTime localTime, WebExcerpt embeddedPage, Retweet embeddedRetweet) {
        this(id, author, msg, utcTime, localTime, Optional.of(embeddedPage), Optional.of(embeddedRetweet));
    }

    public Tweet(long id, String author, String msg, DateTime utcTime, DateTime localTime, Optional<WebExcerpt> embeddedPage, Optional<Retweet> embeddedRetweet) {
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

    public final static TabWriter<Tweet> WRITER = new TabWriter<Tweet>() {
        @Override
        public String asTabDelimStr(Tweet value) {
            return     ISODateTimeFormat.dateTimeNoMillis().print(value.getLocalTime())
              + '\t' + ISODateTimeFormat.dateTimeNoMillis().print(value.getUtcTime())
              + '\t' + toTimeZoneString (new Period (value.getUtcTime(), value.getLocalTime()))
              + '\t' + Retweet.WRITER.asTabDelimStr(value);
        }

		public final String toTimeZoneString (Period period)
		{   int hours = period.getDays() * 24 + period.getHours();
		    int mins  = period.getMinutes();
		    
		    // round minutes to the nearest 15min interval
		    mins = ((mins + 2) / 15) * 15;
		    
		    return String.format ("%+03d:%02d", hours, mins);
		}
        
        @Override
        public Pair<Tweet, Integer> fromTabDelimParts(String[] parts, int from) {
            DateTime localTime = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(parts[from + 0]);
            DateTime utcTime   = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(parts[from + 1]);

            // a bit of a hack this, the "retweet" is actually this "tweet"
            // a good example of why inheritance is worse than composition. Oh well :-/
            Pair<Retweet, Integer> rtPair = Retweet.WRITER.fromTabDelimParts(parts, from + 3);
            Retweet rt = rtPair.getLeft();

            return Pair.of (
                new Tweet (
                    rt.getId(),
                    rt.getAuthor(),
                    rt.getMsg(),
                    utcTime,
                    localTime,
                    rt.getEmbeddedPage(),
                    rt.getEmbeddedRetweet()
                ),
                rtPair.getRight()
            );
        }
    };

    /**
     * Returns a new tweet with the given retweet embedded.
     * The given URI, from which the embedded retweet was accessed, will be
     * deleted from the message text if it occurs. Only the first occurrence is deleted.s
     */
    @Override
    public Tweet withEmbeddedRetweet(URI embeddedRetweetUri, Retweet embeddedRetweet) {
        if (this.containsRetweet()) {
            throw new IllegalStateException("This tweet already contains a retweet");
        }

        return new Tweet (
                getId(),
                getAuthor(),
                StringUtils.replaceOnce(getMsg(), embeddedRetweetUri.toASCIIString(), ""),
                utcTime,
                localTime,
                getEmbeddedPage(),
                Optional.of(embeddedRetweet)
        );
    }

}