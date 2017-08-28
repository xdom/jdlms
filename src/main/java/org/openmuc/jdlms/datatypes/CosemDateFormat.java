/*
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
 *
 */
package org.openmuc.jdlms.datatypes;

import java.util.Calendar;

import org.openmuc.jdlms.datatypes.CosemDateTime.ClockStatus;

public interface CosemDateFormat {

    /**
     * The octet string.
     * 
     * @return the octet string.
     */
    byte[] encode();

    /**
     * Converts the COSEM DATE/TIME to a {@link Calendar}.
     * 
     * @return the object as a calendar.
     */
    Calendar toCalendar();

    long asUnixTimeStanp();

    /**
     * The size of the octet string.
     * 
     * @return the size.
     */
    int length();

    /**
     * Retrieves the value for a certain value.
     * 
     * @param field
     *            the field which should be retrieved.
     * @return the value as an int32.
     * @throws IllegalArgumentException
     *             if the class doesn't have the field.
     */
    int get(Field field) throws IllegalArgumentException;

    /**
     * Fields of the {@linkplain CosemDateFormat}.
     */
    public enum Field {
        /**
         * Interpreted as a short unsigned.
         * 
         * @see Calendar#YEAR
         */
        YEAR,
        /**
         * The month is in the range [1, 12]
         * <p>
         * 1 is January
         * </p>
         * 
         * @see Calendar#MONTH
         */
        MONTH,
        /**
         * The day of month is in the range [1, 31]
         * 
         * @see Calendar#DAY_OF_MONTH
         */
        DAY_OF_MONTH,
        /**
         * The day of week is in the range [1, 7]
         * <p>
         * 1 is Monday
         * </p>
         * 
         * @see Calendar#DAY_OF_WEEK
         */
        DAY_OF_WEEK,
        /**
         * The hour is in the range [0, 23]
         * 
         * @see Calendar#HOUR_OF_DAY
         */
        HOUR,
        /**
         * The minute is in the range [0, 59]
         * 
         * @see Calendar#MINUTE
         */
        MINUTE,
        /**
         * The minute is in the range [0, 59]
         * 
         * @see Calendar#SECOND
         */
        SECOND,
        /**
         * The hundredths is in the range [0, 99]
         * 
         * @see Calendar#MILLISECOND
         */
        HUNDREDTHS,
        /**
         * The deviation is in the range [-720, 720]. Deviation with the value 0x8000 or 32768 means, deviation is not
         * specified.
         * 
         * @see Calendar#ZONE_OFFSET
         */
        DEVIATION,
        /**
         * Set into {@link ClockStatus}.
         * 
         * @see ClockStatus#clockStatusFrom(byte)
         */
        CLOCK_STATUS;
    }
}
