package cc.twittertools.sink;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Sink} which accumulated values in an accessible array.
 */
public class ArraySink<T> implements Sink<T> {
    private final List<T> values = new ArrayList<>();

    @Override
    public void put(T value) {
        values.add(value);
    }

    public List<T> values() {
        return values;
    }

    @Override
    public void close() {
    }
}
