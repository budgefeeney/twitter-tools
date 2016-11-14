package cc.twittertools.scripts;


import cc.twittertools.download.AsyncEmbeddedJsonStatusBlockCrawler;
import cc.twittertools.post.SavedTweetReader;
import cc.twittertools.post.Tweet;
import cc.twittertools.post.embed.Retweet;
import cc.twittertools.spider.IndividualUserTweetsSpider;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cc.twittertools.spider.IndividualUserTweetsSpider.*;

/**
 * Given a list of tweet files, read {@link cc.twittertools.post.Tweet}s in from each,
 * and for every tweet that has a link to Twitter, or a Twitter link-shortening
 * service, see if that link refers to a tweet. If it does go to the link, parse the
 * tweet, and embed it as an embedded retweet.
 */
public class RetweetFinder implements Callable<Boolean> {

    public static final Charset TWITTER_DEFAULT_CHARSET = Charsets.UTF_8;
    public static final String META_HTTP_EQUIV_REFRESH = "<meta http-equiv=\"refresh\"";
    public static final int MAX_HTML_REDIRECT_COUNT = 5;
    public static final int MIN_URL_LENGTH = "http://t.co/".length();


    private interface Sink<T> extends AutoCloseable {
        void put(T value);
        void close() throws IOException; // scope the exception
    }
    private static class ArraySink<T> implements Sink<T> {
        private final List<T> values = new ArrayList<>();
        @Override public void put (T value) {
            values.add(value);
        }
        public List<T> values() {
            return values;
        }
        @Override public void close() { }
    }
    private static class FileSink<T> implements Sink<T> {
        private final BufferedWriter wtr;
        private final Function<T, String> stringFunction;
        public FileSink(Path outPath, Function<T, String> stringFunction) throws IOException {
            this.wtr = Files.newBufferedWriter(outPath, Charsets.UTF_8);
            this.stringFunction = stringFunction;
        }
        @Override public void put (T value) {
            try {
                wtr.write(stringFunction.apply(value));
                wtr.newLine();
            } catch (IOException ioe) {
                throw new IllegalStateException(ioe.getMessage(), ioe);
            }
        }
        @Override public void close() throws IOException {
            wtr.close();
        }
    }

    private final static Logger log = Logger.getLogger(RetweetFinder.class);
    private final Iterator<Path> inPaths;
    private final HttpClient httpClient;

    public RetweetFinder(Iterator<Path> inPaths) {
        this.inPaths    = inPaths;

        List<Header> defaultHeaders = IndividualUserTweetsSpider.DEFAULT_HEADERS
                .entrySet().stream()
                .map(e -> new BasicHeader(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        this.httpClient = HttpClients.custom()
                        .setConnectionTimeToLive(AsyncEmbeddedJsonStatusBlockCrawler.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                        .evictIdleConnections(AsyncEmbeddedJsonStatusBlockCrawler.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                        .setDefaultHeaders(defaultHeaders)
                        .build();
    }

    public Boolean call() throws IOException {
        while (inPaths.hasNext()) {
            Path inPath = inPaths.next();
            if (log.isInfoEnabled()) {
                log.info("Processing file: " + inPath.toAbsolutePath().toString());
            }
            processFile(inPath);
        }
        return true;
    }

    private final void processFile (Path inFile) throws IOException {
        final Path outPath = inFile.getParent().resolve(withSuffix(inFile.getFileName(), "-out"));
        try (SavedTweetReader rdr = new SavedTweetReader(inFile);
             Sink<Tweet> wtr = new FileSink<>(outPath, t -> Tweet.WRITER.asTabDelimStr(t)) ) {
            processTweets (rdr, wtr);
        }
    }

    private final void processTweets (Iterator<Tweet> inputs, Sink<Tweet> outputSink) throws IOException {
        while (inputs.hasNext()) {
            final Tweet tweet = inputs.next();
            Optional<Tweet> augmentedTweet = Optional.empty();
            for (URI uri : urlsIn(tweet.getMsg())) {
                augmentedTweet =
                    fetchContent(tweet.getAuthor(), tweet.getId(), uri)
                            .flatMap(p -> parseTweetPage(p))
                            .filter(t -> t.getId() != tweet.getId())
                            .map(t -> tweet.withEmbeddedRetweet(uri, t));

                if (augmentedTweet.isPresent()) {
                    break;
                }
            }
            outputSink.put(augmentedTweet.orElse(tweet));
        }
    }

    /**
     * Splits a filename into a the name and the extension, adds the given
     * suffix to the end of the name, then adds the extension back to the
     * end of the suffix.
     */
    private final static String withSuffix(Path name, String suffix) {
        return withSuffix (name.toString(), suffix);
    }

    /**
     * Splits a filename into a the name and the extension, adds the given
     * suffix to the end of the name, then adds the extension back to the
     * end of the suffix.
     */
    private final static String withSuffix(String name, String suffix) {
        int i = name.lastIndexOf('.');
        if (i <= 0) {
            return name + suffix;
        } else {
            --i;
            return name.substring(0, i) + suffix + name.substring (i);
        }
    }

    /**
     * Performs a single HTTP request for the given URI
     */
    private final Optional<String> fetchContent(String account, long id, URI uri) throws IOException {
        return fetchContent(account, id, uri, MAX_HTML_REDIRECT_COUNT);
    }

    /**
     * Performs a single HTTP request for the given URI
     */
    private final Optional<String> fetchContent(String account, long id, URI uri, int maxHtmlRedirectCount) throws IOException {
        // So this is a bit of a headf**k. Somehow, through the magic of
        // transitive dependencies, there are two versions of Apache's HttpClient
        // in this project.
        //
        // The old version in org.apache.commons.httpclient
        // The new version in org.apache.http.client
        //
        // The old version works reliably for Twitter indexing, and so I'm
        // keeping it, as I don't have time to re-write and test all that.
        // The new version has support for returning the final URL after a
        // sequence of redirects. Consequently, I'm using them both. :-/

        BasicHttpContext ctx = new BasicHttpContext();
        HttpGet      get = new HttpGet(uri);

        get.addHeader("Referer", accountUrlOnTwitter(account));
        HttpResponse res = httpClient.execute(get, ctx);
        if (res.getStatusLine().getStatusCode() > 399)
            return Optional.empty();

        // Check it's not the original tweet. This happens if someone has
        // embedded a picture or URL: the raw tweet text has a link which
        // redirects to a rich HTML rendering of the same tweet showing the
        // picture or a "card" with a summary of the URL content.

        URI finalUrl = uri;
        RedirectLocations locations = (RedirectLocations) ctx.getAttribute(DefaultRedirectStrategy.REDIRECT_LOCATIONS);
        if (locations != null) {
            finalUrl = locations.getAll().get(locations.getAll().size() - 1);
        }

        HttpEntity entity = res.getEntity();
        ByteArrayOutputStream out = new ByteArrayOutputStream();//(int) entity.getContentLength());
        entity.writeTo(out);
        String content = new String(out.toByteArray(), TWITTER_DEFAULT_CHARSET);

        // Check for a HTML redirect
        if (maxHtmlRedirectCount > 0) {
            final Optional<URI> redirectUri = tryFindHtmlRedirect(content);
            if (redirectUri.isPresent()) {
                if (log.isInfoEnabled()) {
                    log.info("Following HTML redirect (max-redirects=" + maxHtmlRedirectCount + ") to " + redirectUri.get() + "");
                }
                return fetchContent(account, id, redirectUri.get(), --maxHtmlRedirectCount);
            }
        }

        // So we've ended up at a page, after following HTTP and HTML redirects.
        // But is this page for a single tweet, or something else?
        if (! isTwitterStatusUrl(finalUrl) || finalUrl.getPath().startsWith("/" + account)) {
            return Optional.empty();
        }

        return Optional.of(content);
    }

    private boolean isTwitterStatusUrl(URI finalUrl) {
        return finalUrl.getHost().endsWith("twitter.com") && finalUrl.getPath().matches("/[^/]+/status/\\d+");
    }

    /**
     * Inspects the HTML content for a META HTTP-EQUIV Refresh tag. If found,
     * and if the content is a URL, which can be parsed, then return it.
     * Otherwise return empty
     */
    private static Optional<URI> tryFindHtmlRedirect(String content) {
        int pos = StringUtils.indexOfIgnoreCase(content, META_HTTP_EQUIV_REFRESH);
        if (pos >= 0) {
            int urlStart = StringUtils.indexOfIgnoreCase(content, "URL=", pos + META_HTTP_EQUIV_REFRESH.length());
            if (urlStart > 0) {
                urlStart += "URL=".length();
                int urlEnd = content.indexOf('"', urlStart);
                if (urlEnd > urlStart + MIN_URL_LENGTH) {
                    try {
                        return Optional.of(new URI(content.substring(urlStart, urlEnd)));
                    } catch (URISyntaxException use) {
                        return Optional.empty();
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Makes a Twitter referrer URI from an acount
     */
    private final String accountUrlOnTwitter(String account) {
        return "https://twitter.com/" + account;
    }

    /**
     * Finds all URLs in a piece of text that might conceivably refer
     * to a single tweet page.
     */
    private final static Iterable<URI>  urlsIn (final String text) {
        return () -> new Iterator<URI>() {
            private int pos = 0;

            @Override
            public boolean hasNext() {
                pos = text.indexOf("http", pos);
                if (pos < 0) {
                    return false;
                }
                final String candidate = text.substring(pos);

                if (candidate.matches("https?://twitter.com/[^/]+/status/\\d+.*")) {
                    return true;
                } else if (candidate.matches("https?://t\\.co.*")) {
                    return true;
//                        } else if (candidate.matches("https?://")) {
//                            return true
                } else {
                    pos += 1;
                    return hasNext();
                }
            }

            @Override
            public URI next() {
                if (pos < 0) throw new IllegalStateException("Called next() before hasNext()");
                final String candidate = text.substring(pos);
                int end = 4;
                while (end < candidate.length()
                        && CharUtils.isAscii(candidate.charAt(end))
                        && ! Character.isWhitespace(candidate.charAt(end))) {
                    ++end;
                }
                pos += end;
                return URI.create(candidate.substring(0, end));
            }
        };
    }

    public static Optional<Retweet> parseTweetPage (final String input) {
        Document document = Jsoup.parse(input);

        Elements tweetContainers = document.select("div.TweetPermalink-tweetDetail");
        if (tweetContainers.isEmpty()) {
            return Optional.empty();
        }
        Element tweetContainer = tweetContainers.first();
        long id = Long.parseLong(tweetContainer.attr("data-scribe-item-id"));

        // the user-name
        Elements userBox = tweetContainer.select("div.UserCell-body");
        String screenName = "";
        if (! userBox.isEmpty()) {
            Elements screenNames = userBox.select("span.UserNames-screenName"); // which is for URLs only, and separate from DisplayName, which is what is normally shown
            if (! screenNames.isEmpty()) {
                screenName = screenNames.first().text().trim();
                if (screenName.startsWith("@")) {
                    screenName = screenName.substring(1);
                }
            }
        }

        // The message body
        Elements tweetTextTag = tweetContainer.select("div.TweetDetail-text");
        String tweetText =
                tweetTextTag.isEmpty() ? "" : tweetTextTag.text().trim();

        // TODO process emoticons

        // TODO process embedded tweets

        if (screenName.isEmpty() || tweetText.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new Retweet(id, screenName, tweetText, Optional.empty(), Optional.empty()));
        }
    }

    public static void main(String[] args) throws IOException {
        final String USERNAME = "kylo_is_a_mole";
        final DateTime NOW = DateTime.now();
        List<Tweet> tweets = Arrays.asList(
            new Tweet(1, USERNAME, "*sigh* it's five or FEWER https://t.co/1clwMHbydl", NOW, NOW),
            new Tweet(2, USERNAME, "@AngryGenHux sorry arm \nI can't take anything you say seriously anymore", NOW, NOW),
            new Tweet(3, USERNAME, "freezing his ball mid-air was not cheating\\ni was sick of hearing \\\"who serves first? i serve first? you serve first?\\\" https://t.co/pKpAgq3zqO", NOW, NOW),
            new Tweet(4, USERNAME, "dad https://t.co/abXfeoUdaf", NOW, NOW)
        );

        RetweetFinder f = new RetweetFinder(Collections.emptyIterator());
        ArraySink<Tweet> results = new ArraySink<>();
        f.processTweets(tweets.iterator(), results);

        results.values().forEach(t -> System.out.println(t));
    }

}
