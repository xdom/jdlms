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
package org.openmuc.jdlms.datatypes;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmuc.jdlms.datatypes.CosemDateFormat.Field;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;

@RunWith(JUnitParamsRunner.class)
public class CosemDateTimeTest {

    @Test
    public void test1() throws Exception {
        CosemDate cosemDate = new CosemDate(2016, 6, 21);
        CosemTime cosemTime = new CosemTime(12, 46, 19, 20);

        CosemDateTime cosemDateTime = new CosemDateTime(cosemDate, cosemTime,
                (int) TimeUnit.MINUTES.convert(5, TimeUnit.HOURS));

        int deviation = cosemDateTime.get(Field.DEVIATION);

        assertEquals(5 * 60, deviation);
    }

    @Test
    public void testSymmetry2() throws Exception {
        CosemDateTime cosemDateTime = buildSampleDatetime();

        CosemDateTime cosemDateTime2 = CosemDateTime.decode(cosemDateTime.encode());

        for (Field field : CosemDateFormat.Field.values()) {
            assertEquals(cosemDateTime.get(field), cosemDateTime2.get(field));
        }
    }

    @Test
    public void testSymmetry3() throws Exception {
        CosemDateTime cosemDateTime = buildSampleDatetime();

        byte[] bytesDate1 = cosemDateTime.encode();

        CosemDateTime cosemDateTime2 = CosemDateTime.decode(bytesDate1);
        byte[] bytesDate2 = cosemDateTime2.encode();

        assertArrayEquals(bytesDate1, bytesDate2);
    }

    @Test
    public void test5() throws Exception {
        final int year = 2016;
        final int month = 6;
        final int dayOfMonth = 21;
        CosemDate cosemDate = new CosemDate(year, month, dayOfMonth);

        final int hour = 12;
        final int minute = 46;
        final int second = 19;
        final int hundredths = 20;
        CosemTime cosemTime = new CosemTime(hour, minute, second, hundredths);

        CosemDateTime cosemDateTime = new CosemDateTime(cosemDate, cosemTime, -120);

        assertEquals(year, cosemDateTime.get(Field.YEAR));
        assertEquals(month, cosemDateTime.get(Field.MONTH));
        assertEquals(dayOfMonth, cosemDateTime.get(Field.DAY_OF_MONTH));

        assertEquals(hour, cosemDateTime.get(Field.HOUR));
        assertEquals(minute, cosemDateTime.get(Field.MINUTE));
        assertEquals(second, cosemDateTime.get(Field.SECOND));
        assertEquals(hundredths, cosemDateTime.get(Field.HUNDREDTHS));

    }

    @Test(expected = IllegalArgumentException.class)
    @Parameters({ "2016, 13, 13", "2016, 12, 32", "99, 2, 0", "2, 66, 0", "2, 59, 77" })
    @TestCaseName("test error handling of codem date withe year = {0}, month = {1}, day = {2}")
    public void testDateExCase1(int year, int month, int day) throws Exception {
        new CosemDate(year, month, day);
    }

    private CosemDateTime buildSampleDatetime() {
        CosemDate cosemDate = new CosemDate(2016, 6, 21);
        CosemTime cosemTime = new CosemTime(12, 46, 19, 20);

        return new CosemDateTime(cosemDate, cosemTime, -2, TimeUnit.HOURS);
    }

    /*
     * Shows recalculation in Calendar with Zone Offset and DST
     */
    @Test
    public void calendarOffsetTest() {
        // jDLMS uses GMT
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.HOUR_OF_DAY, 2);
        cal.set(Calendar.ZONE_OFFSET, -3600000);
        cal.set(Calendar.DST_OFFSET, -3600000);
        cal.getTime();

        // All values are as set.
        assertEquals(-3600000, cal.get(Calendar.ZONE_OFFSET));
        assertEquals(-3600000, cal.get(Calendar.DST_OFFSET));
        assertEquals(2, cal.get(Calendar.HOUR_OF_DAY));

        // Change a field to trigger internal recalculation
        cal.add(Calendar.SECOND, 1);
        cal.add(Calendar.SECOND, -1);

        // Zone offset and DST offset are moved to the hour of day
        assertEquals(4, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.ZONE_OFFSET));
        assertEquals(0, cal.get(Calendar.DST_OFFSET));
    }

    @Test
    public void testNotSpecifiedDeviation() throws Exception {
        CosemDate cosemDate = new CosemDate(2016, 6, 21);
        CosemTime cosemTime = new CosemTime(12, 46, 19, 20);
        CosemDateTime cosemDateTime = new CosemDateTime(cosemDate, cosemTime);
        assertEquals(0, cosemDateTime.get(Field.DEVIATION));
    }
}
