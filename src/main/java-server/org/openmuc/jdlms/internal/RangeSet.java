package org.openmuc.jdlms.internal;

import java.util.LinkedList;
import java.util.List;

public class RangeSet<E extends Comparable<E>, T extends Range<E>> {

    private int size;
    private RangeEntry rootElement;
    private RangeEntry smallest;
    private RangeEntry biggest;
    private final List<T> internalList;

    public RangeSet() {
        this.size = 0;
        this.rootElement = null;
        this.internalList = new LinkedList<>();
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public List<T> toList() {
        return this.internalList;
    }

    public T getIntersectingRange(E elem) {
        RangeEntry curr = this.rootElement;

        while (curr != null) {

            if (curr.value.intersectsWith(elem)) {
                return curr.value;
            }

            if (curr.value.getLowerBound().compareTo(elem) < 0) {
                curr = curr.rightChild;
            }
            else {
                curr = curr.leftChild;
            }
        }

        return null;
    }

    /**
     * Adds a range to the range set.
     * 
     * @param entry
     *            the new entry to add.
     * @return the conflicting range;
     */
    public T add(T entry) {
        final RangeEntry newEntry = new RangeEntry(entry);
        if (this.rootElement == null) {
            this.smallest = this.biggest = this.rootElement = newEntry;

            ++this.size;
            this.internalList.add(entry);
            return null;
        }
        else {
            RangeEntry curr = this.rootElement;
            RangeEntry prev = null;
            while (true) {
                int comparison = curr.value.compareTo(entry);
                if (comparison == 1) {
                    prev = curr;
                    curr = curr.leftChild;

                    if (curr == null) {
                        return addAsPrevLeft(entry, prev);
                    }
                }
                else if (comparison == -1) {
                    prev = curr;
                    curr = curr.rightChild;

                    if (curr == null) {
                        return addAsPrevRight(entry, prev);
                    }
                }
                else {
                    return curr.value;
                }

            }

        }

    }

    private T addAsPrevRight(T e, RangeEntry prev) {
        if (prev.value.intersects(e)) {
            return prev.value;
        }

        RangeEntry newEntry = new RangeEntry(e);
        prev.rightChild = newEntry;
        prev.rightChild.parent = prev;

        if (this.biggest.value.compareTo(e) == -1) {
            this.biggest = newEntry;
        }
        this.internalList.add(e);
        ++this.size;
        return null;
    }

    private T addAsPrevLeft(T e, RangeEntry prev) {
        if (e.intersects(prev.value)) {
            return prev.value;
        }

        RangeEntry newEntry = new RangeEntry(e);
        prev.leftChild = newEntry;
        prev.leftChild.parent = prev;

        if (this.smallest.value.compareTo(e) == 1) {
            this.smallest = newEntry;
        }
        this.internalList.add(e);
        ++this.size;
        return null;
    }

    private class RangeEntry {
        private final T value;
        private RangeEntry leftChild;
        private RangeEntry rightChild;

        @SuppressWarnings("unused")
        private RangeEntry parent;

        public RangeEntry(T value) {
            this.value = value;
            this.leftChild = null;
            this.rightChild = null;
            this.parent = null;
        }

    }

}
