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

import org.junit.Test;

public class CosemDateTest {

    @Test
    public void testExample1() throws Exception {
        final int year = 0xFFFF;
        final int month = 0xFF;
        final int dayOfMonth = 0xFE;
        new CosemDate(year, month, dayOfMonth);
        // last day of the month in every year and month
    }

    @Test
    public void testExample2() throws Exception {
        final int year = 0xFFFF;
        final int month = 0xFF;
        final int dayOfMonth = 0xFE;
        final int dayOfWeek = 0x07;
        new CosemDate(year, month, dayOfMonth, dayOfWeek);
        // last sunday in every year and month
    }

    @Test
    public void testExample3() throws Exception {
        final int year = 0xFFFF;
        final int month = 0x03;
        final int dayOfMonth = 0xFE;
        final int dayOfWeek = 0x07;
        new CosemDate(year, month, dayOfMonth, dayOfWeek);
        // last sunday in march in every year
    }

    @Test
    public void testExample4() throws Exception {
        final int year = 0xFFFF;
        final int month = 0x03;
        final int dayOfMonth = 0x01;
        final int dayOfWeek = 0x07;
        new CosemDate(year, month, dayOfMonth, dayOfWeek);
        // first sundday in march every year
    }

    @Test
    public void testExample5() throws Exception {
        final int year = 0xFFFF;
        final int month = 0x03;
        final int dayOfMonth = 0x16;
        final int dayOfWeek = 0x05;
        new CosemDate(year, month, dayOfMonth, dayOfWeek);
        // fourth friday in march in every year
    }

    @Test
    public void testExample6() throws Exception {
        final int year = 0xFFFF;
        final int month = 0x0A;
        final int dayOfMonth = 0x16;
        final int dayOfWeek = 0x07;
        new CosemDate(year, month, dayOfMonth, dayOfWeek);
        // fourth sunday in oktober in every year
    }

    @Test
    public void testExample7() throws Exception {
        final int year = 0xFFFF;
        final int month = 0x0A;
        final int dayOfMonth = 0x16;
        final int dayOfWeek = 0x07;
        new CosemDate(year, month, dayOfMonth, dayOfWeek);
        // fourth sunday in oktober in every year
    }
}
