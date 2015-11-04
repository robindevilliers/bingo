package uk.co.malbec.hound.reporter.machinery;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Vector<T> implements Consumer<T> {

    private List<T> values = new ArrayList<>();

    @Override
    public void accept(T t) {
        values.add(t);
    }

    public List<T> getValues() {
        return values;
    }
}
