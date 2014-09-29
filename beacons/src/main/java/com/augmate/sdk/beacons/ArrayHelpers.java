package com.augmate.sdk.beacons;

import java.util.ArrayList;
import java.util.Iterator;

public class ArrayHelpers {
    /**
     * evaluates to true or false, used as a filter for array processing functions
     * @param <T>
     */
    public interface Predicate<T> {
        public boolean evaluate(T item);
    }

    public static <T> int removeWhere(ArrayList<T> list, Predicate<? super T> predicate) {
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
