package cc.twittertools.post.embed;

import cc.twittertools.post.Pair;
import cc.twittertools.post.Sigil;
import cc.twittertools.post.old.Tweet;
import cc.twittertools.post.tabwriter.TabWriter;
import com.google.common.collect.Sets;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;

import java.net.URI;
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
            msg = removeFirstCaseInsensitive(msg, "https://twitter.com/" + e.getAuthor() + "/status/" + e.getId());
        }
        if (embeddedPage.isPresent()) {
            WebExcerpt e = embeddedPage.get();
            msg = removeFirstCaseInsensitive(msg, e.getUri().toASCIIString());
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

    public static String removeFirstCaseInsensitive (String haystack, String needle) {
        String lwrHaystack = haystack.toLowerCase();
        String lwrNeedle   = needle.toLowerCase();

        int pos = lwrHaystack.indexOf(lwrNeedle);
        if (pos > 0) {
            haystack = haystack.substring(0, pos)
                     + haystack.substring(Math.min(pos + lwrNeedle.length(), haystack.length()));
        }
        return haystack;
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

    public Optional<Retweet> getEmbeddedRetweet() {
        return embeddedRetweet;
    }


    /**
     * Returns the text of this message, all retweets, and any text ex
     * @return
     */
    public String getAllText(boolean includeWebExcerpts) {
        return getMsg()
                + ' '
                + getEmbeddedPage().flatMap(WebExcerpt::getExcerpt)
                .map(e -> e.getTitle() + " " + e.getBody())
                .orElse("")
                + ' '
                + getEmbeddedRetweet().map(r -> r.getAllText(includeWebExcerpts))
                .orElse("");
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


    public static final TabWriter<Retweet> WRITER = new TabWriter<Retweet>() {
        @Override
        public String asTabDelimStr(Retweet val) {
            return
                         val.getAuthor()
                + '\t' + val.getId()
                + '\t' + val.getMsg()
                + '\t' + WebExcerpt.WRITER.asTabDelimStr(val.getEmbeddedPage())
                + '\t' + asTabDelimStr(val.getEmbeddedRetweet());
        }

        @Override
        public Pair<Retweet, Integer> fromTabDelimParts(String[] parts, int from) {
            String author = parts[from + 0];
            long   id     = Long.parseLong(parts[from + 1]);
            String msg    = parts[from + 2];

            Pair<Optional<WebExcerpt>, Integer> exPair =
                    WebExcerpt.WRITER.optFromTabDelimParts(parts, from + 3);
            Pair<Optional<Retweet>, Integer> rtPair =
                    this.optFromTabDelimParts(parts, exPair.getRight());

            return Pair.of (new Retweet(id, author, msg, exPair.getLeft(), rtPair.getLeft()),
                            rtPair.getRight());
        }
    };

    /**
     * Returns a shallow copy of this retweet with the given tweet embedded within it
     * as another retweet.
     *
     * If the given optional is empty, just return this as is.
     */
    public Retweet withEmbeddedRetweet(URI embeddedRetweetUri, Retweet embeddedRetweet) {
        if (this.containsRetweet()) {
            throw new IllegalStateException("This tweet already contains a retweet");
        }

        return new Retweet (
            id,
            author,
            StringUtils.replaceOnce(msg, embeddedRetweetUri.toASCIIString(), ""),
            embeddedPage,
            Optional.of(embeddedRetweet)
        );
    }
}
