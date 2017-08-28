package org.openmuc.jdlms.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class RangeTest {

    @Test
    @Parameters(method = "paramIntersectTestParams")
    public void paramIntersectTest(int lowerBound, int upperBound, int elem, boolean expectedResult) throws Exception {
        Range<Integer> range = newRangeFor(lowerBound, upperBound);

        doCallRealMethod().when(range).intersectsWith(Matchers.anyInt());

        assertEquals(expectedResult, range.intersectsWith(elem));
    }

    public Object paramIntersectTestParams() {
        Object[] params1 = { 10, 20, 15, true };
        Object[] params2 = { 10, 20, 20, true };
        Object[] params3 = { 10, 20, 10, true };

        Object[] params4 = { 10, 20, 30, false };
        Object[] params5 = { 10, 20, 2, false };

        return new Object[][] { params1, params2, params3, params4, params5 };
    }

    @Test
    @Parameters(method = "rangeIntersectTestParams")
    public <E extends Comparable<E>> void rangeIntersectTest(Range<E> r1, Range<E> r2, boolean expectedResult)
            throws Exception {

        // tests symmetry of the intersect function
        // this does not imply transitivity
        assertEquals(expectedResult, r1.intersects(r2));
        assertEquals(expectedResult, r2.intersects(r1));
    }

    public Object rangeIntersectTestParams() {

        Object[] params1 = { newRangeFor(10, 29), newRangeFor(25, 737), true };
        Object[] params2 = { newRangeFor(10, 29), newRangeFor(29, 737), true };

        Object[] params5 = { newRangeFor(10, 100), newRangeFor(10, 102), true };

        Object[] params3 = { newRangeFor(10, 29), newRangeFor(88, 737), false };

        Object[] params4 = { newRangeFor(12, 15), newRangeFor(1, 2), false };

        return new Object[][] { params1, params2, params3, params4, params5 };
    }

    public static Range<Integer> newRangeFor(Integer lowerBound, Integer upperBound) {
        return newRangeFor(lowerBound, upperBound, Integer.class);
    }

    @SuppressWarnings("unchecked")
    public static <E extends Comparable<E>> Range<E> newRangeFor(E lowerBound, E upperBound, Class<E> clazz) {
        Range<E> range = mock(Range.class);

        setInternalState(range, "lowerBound", lowerBound);
        setInternalState(range, "upperBound", upperBound);
        doCallRealMethod().when(range).intersects(Matchers.any(Range.class));
        doCallRealMethod().when(range).intersectsWith(Matchers.any(clazz));
        doCallRealMethod().when(range).compareTo(Matchers.any(Range.class));
        doCallRealMethod().when(range).getLowerBound();
        doCallRealMethod().when(range).getUpperBound();
        doCallRealMethod().when(range).toString();
        return range;
    }
}
