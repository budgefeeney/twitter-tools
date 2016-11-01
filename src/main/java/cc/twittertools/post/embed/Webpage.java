package cc.twittertools.post.embed;

/**
 * Created by bryanfeeney on 25/10/2016.
 */


import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * If Twitter sees a URL in a tweet, it can optionally inject a "card" containing the
 * an excerpt of the page's content. This is a separate download. This version of a
 * {@link Webpage} link contains the oringal link, the link to the info card, and
 * optionally the card's title and body if they're downloaded separately.
 */
public final class Webpage {
    private final static class Excerpt {
        private final String title;
        private final String body;

        public Excerpt(String title, String body) {
            Objects.requireNonNull(title, "Title cannot be null");
            Objects.requireNonNull(body, "Body cannot be null");

            this.title = title;
            this.body = body;
        }

        public String getTitle() {
            return title;
        }
        public String getBody() {
            return body;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Excerpt excerpt = (Excerpt) o;

            if (!title.equals(excerpt.title)) return false;
            return body.equals(excerpt.body);

        }

        @Override
        public int hashCode() {
            int result = title.hashCode();
            result = 31 * result + body.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return StringUtils.left(title, 20) + "...";
        }
    }



    private final URI uri;
    private final URI cardUri;
    private       Optional<Excerpt> excerpt;


    public Webpage(URI uri, URI cardUri) {
        this (uri, cardUri, Optional.empty());
    }

    public Webpage(URI uri, URI cardUri, Optional<Excerpt> excerpt) {
        Objects.requireNonNull(uri, "URI cannot be null");
        Objects.requireNonNull(cardUri, "Card URI cannot be null");

        this.uri = uri;
        this.cardUri = cardUri;
        this.excerpt = excerpt;
    }

    public URI getUri() {
        return uri;
    }

    public URI getCardUri() {
        return cardUri;
    }

    public Optional<Excerpt> getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(Excerpt excerpt) {
        this.excerpt = Optional.of(excerpt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Webpage webpage = (Webpage) o;

        if (!uri.equals(webpage.uri)) return false;
        if (!cardUri.equals(webpage.cardUri)) return false;
        return excerpt.equals(webpage.excerpt);

    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + cardUri.hashCode();
        result = 31 * result + excerpt.hashCode();
        return result;
    }

    public String toString() {
        return excerpt.map(Excerpt::toString).orElse(uri.toASCIIString());
    }

    public String toShortTabDelimString() {
        final String common = uri.toASCIIString() + '\t' + cardUri.toASCIIString();
        final String excerptString =
            excerpt.map(e -> "D" + '\t' + e.getTitle() + '\t' + e.getBody())
                   .orElse(  "P" + '\t' + ""           + '\t' + "");

        return common + '\t' + excerptString;
    }

    public static String emptyShortTabDelimString() {
        return "" + '\t' + ""
                  + '\t'
                  + "" + '\t' + "" + '\t' + "";
    }



    public static Webpage fromShortTabDelimString(String[] parts, int from) {
        if (parts.length <= from) {
            return null;
        }

        final URI uri = URI.create(parts[from + 0]);
        final URI cardUri = URI.create(parts[from + 1]);
        final Optional<Excerpt> excerpt;

        if (parts[from + 2] == "P") {
            excerpt = Optional.empty();
        } else {
            excerpt = Optional.of (new Excerpt(
                    parts[from + 3], parts[from + 4]
            ));
        }

        return new Webpage(uri, cardUri, excerpt);
    }
}
