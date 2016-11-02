package cc.twittertools.post.tabwriter;

import cc.twittertools.post.Pair;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * The interface for any value that can be written in a fixed-length tab
 * delimited text-format.
 */
public interface TabWriter<T> {

    String SOME = "Some";
    String NONE = "None";

    /**
     * A tab delimited string representation of the given value. The
     * string is not terminated by a newline.
     */
    String asTabDelimStr(T value);

    /**
     * Generate a value from the given line, which shoudl be tab
     * delimited.
     */
    default T fromTabDelimStr(String str) {
        String[] parts = StringUtils.splitPreserveAllTokens(str, '\t');
        return fromTabDelimParts(parts, 0).getLeft();
    }

    /**
     * Generate a value from the given delimited text-fields. Return
     * the value and the next available field to read from.
     */
    Pair<T, Integer> fromTabDelimParts(String[] parts, int from);

    /**
     * Writes an optional of this value as tab delimited text. Does this by
     * first writing a marker to see if a value is present or not, then
     * the value itself if present.
     */
    default String asTabDelimStr (Optional<T> value) {
        return value.map(v -> SOME + '\t' + this.asTabDelimStr(v)).orElse(NONE);
    }

    /**
     * Reads in an optional of this value as written by {@link #asTabDelimStr(Optional)},
     * by inspecting the first marker field, and then reading in the rest using
     * {@link #fromTabDelimParts(String[], int)} if availabel.
     *
     * Returns a pair with the optional of the value that was read, and the index
     * of the next available field to read from
     */
    default Pair<Optional<T>, Integer> optFromTabDelimParts (String[] parts, int from) {
        if (parts[from].equals (NONE)) {
            return Pair.of (Optional.empty(), Integer.valueOf(from + 1));
        } else if (! parts[from].equals(SOME)) {
            throw new IllegalArgumentException("First field in a representation of an optional value is neither '" + SOME + "' or '" + NONE + "'");
        } else {
            Pair<T, Integer> inner = this.fromTabDelimParts(parts, from + 1);
            return Pair.of (Optional.of(inner.getLeft()), inner.getRight());
        }
    }

    /**
     * Writes a collection of values to a writer, where values are newline
     * delimited, and value fields are tab delimited.
     */
    default void writeAllAsTabDelim (Writer wtr, Iterator<T> values) throws IOException {
        while (values.hasNext()) {
            wtr.write(asTabDelimStr(values.next()));
            wtr.write('\n');
        }
    }

    /**
     * Reads a collection of values from a reader, where values are newline
     * delimited, and value fields are tab delimited.
     */
    default List<T> readAllFromTabDelim (BufferedReader rdr) throws IOException {
        List<T> values = new ArrayList<>();
        String line;
        while ((line = rdr.readLine()) != null) {
            values.add (fromTabDelimStr(line));
        }
        return values;
    }
}
