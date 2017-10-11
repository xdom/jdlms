/**
 * Copyright 2012-17 Fraunhofer ISE
 *
 * This file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 *
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jDLMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 */
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
        if (this.rootElement != null) {
            return addNode(entry, this.rootElement);
        }

        this.smallest = this.biggest = this.rootElement = newEntry;

        ++this.size;
        this.internalList.add(entry);
        return null;

    }

    private T addNode(T entry, RangeEntry curr) {
        int comparison = curr.value.compareTo(entry);
        if (comparison > 0) {
            return handleLeft(entry, curr);
        }
        else if (comparison < 0) {
            return handleRight(entry, curr);
        }
        else {
            return entry;
        }

    }

    private T handleRight(T entry, RangeEntry curr) {
        if (curr.rightChild != null) {
            return addNode(entry, curr.rightChild);
        }
        else {
            return addAsPrevRight(entry, curr);
        }
    }

    private T handleLeft(T entry, RangeEntry curr) {
        if (curr.leftChild != null) {
            return addNode(entry, curr.leftChild);
        }
        else {
            return addAsPrevLeft(entry, curr);
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
