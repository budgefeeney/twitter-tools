package cc.twittertools.sink;

import java.io.IOException;

/**
 * The opposite of an {@link java.util.Iterator}, this allows you to
 * add values to some output source.
 */
public interface Sink<T> extends AutoCloseable {
    void put(T value);

    void close() throws IOException; // scope the exception
}
