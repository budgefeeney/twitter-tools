package cc.twittertools.post.embed;

/**
 * Created by bryanfeeney on 25/10/2016.
 */


import cc.twittertools.post.Pair;
import cc.twittertools.post.tabwriter.TabWriter;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * If Twitter sees a URL in a tweet, it can optionally inject a "card" containing the
 * an excerpt of the page's content. This is a separate download. This version of a
 * {@link WebExcerpt} link contains the original link, the link to the info card, and
 * optionally the card's title and body if they're downloaded separately.
 */
public final class WebExcerpt {
    public final static class Excerpt {
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

        static final TabWriter<Excerpt> WRITER = new TabWriter<Excerpt>() {
            @Override
            public String asTabDelimStr(Excerpt value) {
                return value.getTitle() + '\t' + value.getBody();
            }

            @Override
            public Pair<Excerpt, Integer> fromTabDelimParts(String[] parts, int from) {
                String title   = parts[from + 0];
                String body    = parts[from + 1];

                return Pair.of(new Excerpt(title, body), from + 2);
            }
        };
    }



    private final URI uri;
    private final URI cardUri;
    private       Optional<Excerpt> excerpt;


    public WebExcerpt(URI uri, URI cardUri) {
        this (uri, cardUri, Optional.empty());
    }

    public WebExcerpt(URI uri, URI cardUri, Optional<Excerpt> excerpt) {
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

        WebExcerpt webExcerpt = (WebExcerpt) o;

        if (!uri.equals(webExcerpt.uri)) return false;
        if (!cardUri.equals(webExcerpt.cardUri)) return false;
        return excerpt.equals(webExcerpt.excerpt);

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


    public static WebExcerpt fromShortTabDelimString(String[] parts, int from) {
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

        return new WebExcerpt(uri, cardUri, excerpt);
    }

    public final static TabWriter<WebExcerpt> WRITER = new TabWriter<WebExcerpt>() {
        @Override
        public String asTabDelimStr(WebExcerpt value) {
            return value.getUri().toASCIIString()
                 + '\t'
                 + value.getCardUri().toASCIIString()
                 + '\t'
                 + Excerpt.WRITER.asTabDelimStr(value.getExcerpt());
        }

        @Override
        public Pair<WebExcerpt, Integer> fromTabDelimParts(String[] parts, int from) {
            URI uri     = URI.create(parts[from + 0]);
            URI cardUri = URI.create(parts[from + 1]);

            Pair<Optional<Excerpt>, Integer> exPair = Excerpt.WRITER.optFromTabDelimParts(parts, from + 2);
            return Pair.of (new WebExcerpt(uri, cardUri, exPair.getLeft()), exPair.getRight());
        }
    };
}
