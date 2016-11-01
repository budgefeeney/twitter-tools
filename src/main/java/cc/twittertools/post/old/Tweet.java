package cc.twittertools.post.old;

import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.twittertools.post.Sigil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.collect.Sets;

/**
 * All the records of a tweet from twitter. Generally the fields should be
 * fairly obvious. However there is one issue which is the distrinction
 * between "author" and "account". The author is the username of the person
 * who <em>originally</em> wrote the tweet. The "account" is the username
 * of the person from whose feed we found the tweet. These two fields will
 * differ in the case of retweets, the account is the name of the person
 * who retweeted the tweet, the author is the name of the person who
 * originally wrote it.
 * @author bryanfeeney
 *
 */
public class Tweet
{
    /**
     *  Sample is 3:37 PM - 24 Jan 11
     *
     *  <p>We use the UTC time zone to avoid impossible times (e.g. 00:30 the day
     *  the clocks move forward).
     */
    public final static DateTimeFormatter TWITTER_FMT =
            DateTimeFormat.forPattern("h:m a - d MMM yy").withZone(DateTimeZone.UTC);

    private static final Pattern ENDS_WITH_DIGITS = Pattern.compile("\\.\\d+$");

    private final DateTime localTime;
    private final DateTime utcTime;
    private final Set<String> hashTags;
    private final String author;
    private final String account;
    private final String msg;
    private final Set<String> addressees;
    private final long id;
    private final long requestedId;
    private final boolean isRetweetFromId;
    private final boolean isRetweetFromMsg;


    public Tweet(long id, long reqId, String date, String author, String msg) {
        this(id, reqId, null, TWITTER_FMT.parseDateTime(date), author, msg);
    }

    public Tweet(String account, long id, long reqId, DateTime utcTime, DateTime localTime, String author, String msg) {
        this(
	      /* hashTags = */     Sets.newHashSet(Sigil.HASH_TAG.extractSigils(msg).getRight()),
	      /* account = */      account,
	      /* author = */       author,
	      /* msg = */          msg,
	      /* addressees = */   Sets.newHashSet(Sigil.ADDRESSEE.extractSigils(msg).getRight()),
	      /* id = */           id,
	      /* requestedId = */  reqId,
	      /* isRetweetFromMsg = */ ! Sigil.RETWEET.extractSigils(msg).getRight().isEmpty(),
	      /* utcTime = */      utcTime,
	      /* localTime = */    localTime
        );
    }


    public Tweet(long id, long reqId, DateTime utcTime, DateTime localTime, String author, String msg) {
        this(
      /* hashTags = */     Sets.newHashSet(Sigil.HASH_TAG.extractSigils(msg).getRight()),
      /* author = */       author,
      /* msg = */          msg,
      /* addressees = */   Sets.newHashSet(Sigil.ADDRESSEE.extractSigils(msg).getRight()),
      /* id = */           id,
      /* requestedId = */  reqId,
      /* isRetweetFromMsg = */ ! Sigil.RETWEET.extractSigils(msg).getRight().isEmpty(),
      /* utcTime = */      utcTime,
      /* localTime = */    localTime
        );
    }

    public Tweet(Set<String> hashTags, String author, String msg, Set<String> addressees,
                 long id, long requestedId, boolean isRetweetFromMsg, DateTime utcTime, DateTime localTime) {
        this(
  	      /* hashTags = */     Sets.newHashSet(Sigil.HASH_TAG.extractSigils(msg).getRight()),
  	      /* account = */      author, // <--- This is the extra field handled by this constructor
  	      /* author = */       author,
  	      /* msg = */          msg,
  	      /* addressees = */   Sets.newHashSet(Sigil.ADDRESSEE.extractSigils(msg).getRight()),
  	      /* id = */           id,
  	      /* requestedId = */  requestedId,
  	      /* isRetweetFromMsg = */ isRetweetFromMsg,
  	      /* utcTime = */      utcTime,
  	      /* localTime = */    localTime
        );
    }

    public Tweet(Set<String> hashTags, String account, String author, String msg, Set<String> addressees,
                 long id, long requestedId, boolean isRetweetFromMsg, DateTime utcTime, DateTime localTime) {
        super();
        assert hashTags != null              : "Hash tags set can be empty but not null";
        assert ! StringUtils.isBlank(author) : "Username can be neither blank nor null";
        assert msg != null                   : "Message cannot be null";
        assert addressees != null            : "Addressees cannot be null";
        assert id > 0                        : "ID must be strictly positive";
        assert requestedId > 0               : "Requested ID must be strictly positive";

        this.hashTags   = hashTags;
        this.author     = author;
        this.account    = account;
        this.msg        = msg;
        this.addressees = addressees;
        this.id         = id;
        this.requestedId      = requestedId;
        this.isRetweetFromId  = id != requestedId;
        this.isRetweetFromMsg = isRetweetFromMsg;
        this.utcTime   = utcTime;
        this.localTime = localTime;
    }


    public Set<String> getHashTags() {
        return hashTags;
    }

    public String getAuthor() {
        return author;
    }

    public String getAccount() {
        return account;
    }

    public String getMsg() {
        return msg;
    }

    public Set<String> getAddressees() {
        return addressees;
    }

    public long getId() {
        return id;
    }

    public long getRequestedId() {
        return requestedId;
    }

    public boolean isRetweetFromId() {
        return isRetweetFromId;
    }

    public boolean isRetweetFromMsg() {
        return isRetweetFromMsg;
    }

    public DateTime getLocalTime() {
        return localTime;
    }

    public DateTime getUtcTime() {
        return utcTime;
    }

    @Override
    public String toString()
    { return localTime + " - @" + author + " : \t " + msg;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tweet other = (Tweet) obj;
        if (id != other.id)
            return false;
        return true;
    }


    public String getMsgLessSigils() {
        String msg = this.msg;
        for (String addressee : addressees)
            msg = Sigil.ADDRESSEE.stripFromMsg(msg, addressee);
        for (String hashTag : hashTags)
            msg = Sigil.HASH_TAG.stripFromMsg(msg, hashTag);
        if (isRetweetFromMsg)
            msg = Sigil.RETWEET.stripFromMsg(msg);

        return msg;
    }

    /**
     * Return a tab delimited string with all this tweets raw information
     * terminated by a newline.
     */
    public String toShortTabDelimString()
    {
        return
    /* 0 */          this.getAuthor()
    /* 1 */ + '\t' + this.getId()
    /* 2 */ + '\t' + this.getRequestedId()
    /* 3 */ + '\t' + ISODateTimeFormat.dateTimeNoMillis().print(this.getUtcTime())
    /* 4 */ + '\t' + ISODateTimeFormat.dateTimeNoMillis().print(this.getLocalTime())
    /* 5 */ + '\t' + toTimeZoneString (new Period (this.getUtcTime(), this.getLocalTime()))
    /* 6 */ + '\t' + this.getMsg()
                + '\n';
    }

    public static final String toTimeZoneString (Period period)
    {

        int hours = period.getDays() * 24 + period.getHours();
        int mins  = period.getMinutes();

        // round minutes to the nearest 15min interval
        mins = ((mins + 2) / 15) * 15;

        return String.format ("%+03d:%02d", hours, mins);
    }

    /**
     * Parses a line created by {@link #toShortTabDelimString()} back into
     * a {@link Tweet}. Will throw raw exceptions if the line
     * @param line
     * @return
     */
    public static Tweet fromShortTabDelimString(String account, String line)
    { if ((line = StringUtils.trimToEmpty(line)).isEmpty())
        return null;

        String[] parts = StringUtils.split(line, '\t');
        return new Tweet (
                account,
                Long.parseLong(parts[1]),
                Long.parseLong(parts[2]),
                ISODateTimeFormat.dateTimeNoMillis().parseDateTime(parts[3]),
                ISODateTimeFormat.dateTimeNoMillis().parseDateTime(parts[4]),
                parts[0],
                parts[6]
        );
    }


    /**
     * Given a tweets file determins what the username should be.
     * @param file
     * @return
     */
    public static String userNameFromFile(Path file)
    {	String fileName = file.getFileName().toString();
        Matcher m = ENDS_WITH_DIGITS.matcher(fileName);
        return m.find()
                ? fileName.substring (0, m.start())
                : fileName;
    }

}