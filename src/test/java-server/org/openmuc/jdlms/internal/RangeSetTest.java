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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.openmuc.jdlms.internal.RangeTest.newRangeFor;

import org.junit.Test;

public class RangeSetTest {

    @Test
    public void test1() throws Exception {
        RangeSet<Integer, Range<Integer>> s = new RangeSet<>();
        assertNull(s.add(newRangeFor(10, 20)));
        assertNull(s.add(newRangeFor(21, 23)));
        assertNull(s.add(newRangeFor(25, 30)));
        assertNull(s.add(newRangeFor(99, 130)));

        assertNotNull(s.add(newRangeFor(120, 200)));

        assertNotNull(s.add(newRangeFor(15, 30)));
    }

    @Test
    public void test2() throws Exception {

        Range<Integer> r1 = newRangeFor(10, 20);
        Range<Integer> r2 = newRangeFor(21, 23);
        Range<Integer> r3 = newRangeFor(25, 30);
        Range<Integer> r4 = newRangeFor(99, 130);

        RangeSet<Integer, Range<Integer>> s = new RangeSet<>();
        s.add(r1);
        s.add(r2);
        s.add(r3);
        s.add(r4);

        Range<Integer> r = s.getIntersectingRange(15);
        assertEquals(r1, r);

        r = s.getIntersectingRange(22);
        assertEquals(r2, r);

        r = s.getIntersectingRange(27);
        assertEquals(r3, r);

        r = s.getIntersectingRange(110);
        assertEquals(r4, r);

        r = s.getIntersectingRange(24);
        assertNull(r);

        r = s.getIntersectingRange(70);
        assertNull(r);

        r = s.getIntersectingRange(200);
        assertNull(r);
    }

}
