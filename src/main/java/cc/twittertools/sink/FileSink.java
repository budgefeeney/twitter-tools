package cc.twittertools.sink;

/**
 * A sink whose values are written to a file o each call to
 * {@link #pu}
 */

import org.apache.commons.io.Charsets;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * A sink which writes its values to files as text.
 */
public class FileSink<T> implements Sink<T> {
    private final BufferedWriter wtr;
    private final Function<T, String> stringFunction;
    private final boolean flushOnPut;

    public FileSink(Path outPath, Function<T, String> stringFunction) throws IOException {
        this(outPath, stringFunction, false);
    }

    public FileSink(Path outPath, Function<T, String> stringFunction, boolean flushOnPut) throws IOException {
        this.wtr = Files.newBufferedWriter(outPath, Charsets.UTF_8);
        this.stringFunction = stringFunction;
        this.flushOnPut = flushOnPut;
    }
    @Override public synchronized void put (T value) {
        try {
            wtr.write(stringFunction.apply(value));
            wtr.newLine();
            if (flushOnPut) {
                wtr.flush();
            }
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe.getMessage(), ioe);
        }
    }
    @Override public void close() throws IOException {
        wtr.flush();
        wtr.close();
    }
}