package com.augmate.beacontest.app;

import java.util.Iterator;
import java.util.List;

public class ArrayUtils {
    /**
     * evaluates to true or false, used as a filter for array processing functions
     * @param <T>
     */
    public interface Predicate<T> {
        public boolean evaluate(T item);
    }

    public static <T> int removeWhere(List<T> list, Predicate<? super T> predicate) {
        int removed = 0;
        for(Iterator<T> iter = list.iterator(); iter.hasNext(); ) {
            if(predicate.evaluate(iter.next())) {
                iter.remove();
                removed++;
            }
        }
        return removed;
    }
}
