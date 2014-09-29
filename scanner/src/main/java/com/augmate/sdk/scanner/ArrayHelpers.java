package com.augmate.sdk.scanner;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ArrayHelpers {
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

    public static <T> List<T> where(Set<T> list, Predicate<? super T> predicate) {
        List<T> out = new ArrayList<T>();
        for(Iterator<T> iter = list.iterator(); iter.hasNext(); ) {
            T value = iter.next();
            if(predicate.evaluate(value)) {
                out.add(value);
            }
        }
        return out;
    }

    public static <T> boolean whereOne(Set<T> list, Predicate<? super T> predicate) {
        for(Iterator<T> iter = list.iterator(); iter.hasNext(); ) {
            T value = iter.next();
            if(predicate.evaluate(value)) {
                return true;
            }
        }
        return false;
    }
}
