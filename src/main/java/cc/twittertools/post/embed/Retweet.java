package cc.twittertools.post.embed;

import cc.twittertools.post.Sigil;
import cc.twittertools.post.old.Tweet;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;
import java.util.Set;

/**
 * A tweet fragment embedded in an original Tweet where it's referenced as a retweet.
 */
public class Retweet {

    private final Set<String> hashTags;
    private final String author;
    private final String msg;
    private final Set<String> addressees;
    private final long id;

    private final Optional<WebExcerpt> embeddedPage;
    private final Optional<Retweet> embeddedRetweet;
    private final boolean isManualRetweet;


    public Retweet (long id, String author, String msg, Optional<WebExcerpt> embeddedPage, Optional<Retweet> embeddedRetweet) {
        this(
          /* hashTags = */     Sets.newHashSet(Sigil.HASH_TAG.extractSigils(msg).getRight()),
          /* author = */       author,
          /* msg = */          msg,
          /* addressees = */   Sets.newHashSet(Sigil.ADDRESSEE.extractSigils(msg).getRight()),
          /* id = */           id,
          /* embeddedPage  = */ embeddedPage,
          /* embeddedRetweet = */ embeddedRetweet
        );
    }


    public Retweet(Set<String> hashTags, String author, String msg, Set<String> addressees,
                   long id, Optional<WebExcerpt> embeddedPage, Optional<Retweet> embeddedRetweet) {
        super();

        assert hashTags != null              : "Hash tags set can be empty but not null";
        assert ! StringUtils.isBlank(author) : "Username can be neither blank nor null";
        assert msg != null                   : "Message cannot be null";
        assert addressees != null            : "Addressees cannot be null";
        assert id > 0                        : "ID must be strictly positive";

        if (embeddedRetweet.isPresent()) {
            Retweet e = embeddedRetweet.get();
            msg = StringUtils.replaceOnce(msg, "https://twitter.com/" + e.getAuthor().toLowerCase() + "/status/" + e.getId(), "");
        }
        if (embeddedPage.isPresent()) {
            WebExcerpt e = embeddedPage.get();
            msg = StringUtils.replace(msg, e.getUri().toASCIIString(), "");
        }

        this.hashTags   = hashTags;
        this.author     = author;
        this.msg        = msg;
        this.addressees = addressees;
        this.id         = id;
        this.embeddedPage    = embeddedPage;
        this.embeddedRetweet = embeddedRetweet;
        this.isManualRetweet = ! Sigil.RETWEET.extractSigils(msg).getRight().isEmpty();
    }

    public Set<String> getHashTags() {
        return hashTags;
    }

    public String getAuthor() {
        return author;
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

    public Optional<WebExcerpt> getEmbeddedPage() {
        return embeddedPage;
    }

    @Override
    public String toString()
    { return  "@"
            + author
            + " : \t "
            + msg
            + embeddedPage.map(p -> " [" + p.toString() + "]").orElse("")
            + embeddedRetweet.map(r -> " Retweeting " + r.toString()).orElse("");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Retweet retweet = (Retweet) o;

        if (id != retweet.id) return false;
        if (!author.equals(retweet.author)) return false;
        if (!msg.equals(retweet.msg)) return false;
        return embeddedPage.equals(retweet.embeddedPage);

    }

    @Override
    public int hashCode() {
        int result = author.hashCode();
        result = 31 * result + msg.hashCode();
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + embeddedPage.hashCode();
        return result;
    }

    public String getMsgLessSigils() {
        String msg = this.msg;
        for (String addressee : addressees)
            msg = Sigil.ADDRESSEE.stripFromMsg(msg, addressee);
        for (String hashTag : hashTags)
            msg = Sigil.HASH_TAG.stripFromMsg(msg, hashTag);

        return msg;
    }

    /**
     * Return a tab delimited string with all this tweets raw information
     * terminated by a newline.
     */
    public String toShortTabDelimString()
    {   return
            this.getAuthor()
            + '\t' + this.getId()
            + '\t' + this.getMsg()
            + '\t' + this.embeddedPage.map(WebExcerpt::toShortTabDelimString).orElse(WebExcerpt.emptyShortTabDelimString())
            + '\t' + this.embeddedRetweet.map(Retweet::toShortTabDelimString).orElse("FUCK!");
    }

    /**
     * Parses a line created by {@link #toShortTabDelimString()} back into
     * a {@link Tweet}. Will throw raw exceptions if the line
     */
    public static Retweet fromShortTabDelimString(String account, String[] parts, int from)
    {
        if (parts.length <= from) {
            return null;
        }

        return null;
    }

    /**
     * Does this tweet/retweet itself contain a retweeted tweet
     * <p>
     * Modern twitter allows nested retweets
     * @return
     */
    public boolean containsRetweet() {
        return embeddedRetweet.isPresent();
    }
    public boolean isManualRetweet() {
        return isManualRetweet;
    }

}
