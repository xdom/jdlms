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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Class representing a COSEM Date.
 */
public class CosemDate extends CommonDateFormat {
    private static final int NOT_SPECIFIED = 0xff;

    static final int LENGTH = 5;

    private static final int DAYLIGHT_SAVINGS_END = 0xfd;
    private static final int DAYLIGHT_SAVINGS_BEGIN = 0xfe;

    private static final int LAST_DAY_OF_MONTH = 0xfe;
    private static final int SECOND_LAST_DAY_OF_MONTH = 0xfd;

    private byte[] octetString;

    public CosemDate(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfWeek = 0xFF;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        init(year, month, dayOfMonth, dayOfWeek);

    }

    /**
     * Constructs a COSEM Date.
     * 
     * @param year
     *            the year from 0 to 0xffff.
     * @param month
     *            the month from 1 to 12. Set to 0xff if not specified.
     * @param dayOfMonth
     *            the day of the month starting from 1 to max 31. Set to 0xfe for the last day of a month and 0xfd for
     *            the second last day of a month. Set to 0xff if not specified.
     * @throws IllegalArgumentException
     *             if a parameter does not fit the range
     */
    public CosemDate(int year, int month, int dayOfMonth) throws IllegalArgumentException {
        this(year, month, dayOfMonth, 0xff);
    }

    /**
     * Constructs a COSEM Date.
     * 
     * @param year
     *            the year from 0 to 0xffff.
     * @param month
     *            the month
     * @param dayOfMonth
     *            the day of the month starting from 1 to max 31. Set to 0xfe for the last day of a month and 0xfd for
     *            the second last day of a month. Set to 0xff if not specified.
     * @throws IllegalArgumentException
     *             if a parameter does not fit the range
     */
    public CosemDate(int year, Month month, int dayOfMonth) throws IllegalArgumentException {
        this(year, month.value, dayOfMonth, 0xff);
    }

    /**
     * Constructs a COSEM Date.
     * 
     * @param year
     *            the year from 0 to 0xffff.
     * @param month
     *            the month from 1 to 12. Set to 0xff if not specified.
     * @param dayOfMonth
     *            the day of the month starting from 1 to max 31. Set to 0xfe for the last day of a month and 0xfd for
     *            the second last day of a month. Set to 0xff if not specified.
     * @param dayOfWeek
     *            the day of a week from 1 to 7. 1 is Monday. Set to 0xff if not specified or use
     *            {@link CosemDate#CosemDate(int, int, int)}
     * @throws IllegalArgumentException
     *             if a parameter does not fit the range
     */
    public CosemDate(int year, int month, int dayOfMonth, int dayOfWeek) throws IllegalArgumentException {
        verifyYear(year);
        verifyMonth(month);
        veryfyDays(year, month, dayOfMonth, dayOfWeek);

        init(year, month, dayOfMonth, dayOfWeek);
    }

    private void init(int year, int month, int dayOfMonth, int dayOfWeek) {
        this.octetString = new byte[length()];
        this.octetString[0] = (byte) ((year & 0xff00) >> 8);
        this.octetString[1] = (byte) (year & 0xff);
        this.octetString[2] = (byte) (month & 0xff);
        this.octetString[3] = (byte) (dayOfMonth & 0xff);
        this.octetString[4] = (byte) (dayOfWeek & 0xff);
    }

    private CosemDate(byte[] octetString) {
        this.octetString = octetString;
    }

    public static CosemDate decode(InputStream stream) throws IOException {
        byte[] octetString = new byte[LENGTH];

        if (stream.read(octetString) != LENGTH) {
            throw new IOException("Stream was too short.");
        }

        return decode(octetString);
    }

    public static CosemDate decode(byte[] octetString) {
        if (octetString.length != LENGTH) {
            throw new IllegalArgumentException("Wrong size.");
        }
        return new CosemDate(octetString);
    }

    private void veryfyDays(int year, int month, int dayOfMonth, int dayOfWeek) {
        verifyDayOfMonth(dayOfMonth);
        verifyDayOfWeek(dayOfWeek);

        if (dayOfMonth == LAST_DAY_OF_MONTH || dayOfMonth == NOT_SPECIFIED || dayOfWeek == NOT_SPECIFIED) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);

        if (dayOfMonth == SECOND_LAST_DAY_OF_MONTH) {
            calendar.set(Calendar.DAY_OF_MONTH, lastDayOfMonth(calendar) - 1);
        }
        else {
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }

        int dayOfWeekBasedOnVar = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeekBasedOnVar == 0) {
            dayOfWeekBasedOnVar = 7;
        }

        if (dayOfWeekBasedOnVar != dayOfWeek) {
            throw new IllegalArgumentException("Day of week and day of month are provided, but don't match.");
        }
    }

    private static void verifyYear(int year) {
        if (year < 0 || year > 0xffff) {
            throw new IllegalArgumentException("Parameter year is out of range [0, 0xffff]");
        }
    }

    private static void verifyMonth(int month) {
        boolean monthTooSmall = month < 1;
        boolean isNotStatusFlag = month != DAYLIGHT_SAVINGS_END && month != DAYLIGHT_SAVINGS_BEGIN && month != 0xff;
        boolean monthTooLarge = month > 12 && isNotStatusFlag;
        if (monthTooSmall || monthTooLarge) {
            throw new IllegalArgumentException("Parameter month is out of range.");
        }
    }

    private static void verifyDayOfMonth(int dayOfMonth) {
        // boolean resrvedRange = dayOfMonth >= 0xe0 || dayOfMonth <= 0xfc; // for future use

        boolean monthTooSmall = dayOfMonth < 1;
        boolean montTooLarge = dayOfMonth > 31;
        boolean isNotStatusFlag = dayOfMonth != SECOND_LAST_DAY_OF_MONTH && dayOfMonth != LAST_DAY_OF_MONTH
                && dayOfMonth != 0xff;
        if (monthTooSmall || montTooLarge && isNotStatusFlag) {
            throw new IllegalArgumentException("Parameter day of month is out of range.");
        }
    }

    private static void verifyDayOfWeek(int dayOfWeek) {
        if (dayOfWeek < 1 || dayOfWeek > 7 && (dayOfWeek != 0xff)) {
            throw new IllegalArgumentException("Parameter day of week is out of range.");
        }
    }

    @Override
    public byte[] encode() {
        return Arrays.copyOf(this.octetString, length());
    }

    @Override
    public Calendar toCalendar() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        initCalendar(calendar);

        return calendar;
    }

    void initCalendar(Calendar calendar) {
        int year = get(Field.YEAR);
        if (year != 0xFFFF) {
            calendar.set(Calendar.YEAR, year);
        }

        // TODO: consider daylight_savings
        int month = get(Field.MONTH);
        if (month != NOT_SPECIFIED) {
            calendar.set(Calendar.MONTH, month - 1);
        }

        int dayOfMonth = get(Field.DAY_OF_MONTH);
        int dayOfWeek = get(Field.DAY_OF_WEEK);

        if (dayOfMonth == LAST_DAY_OF_MONTH) {
            if (dayOfWeek == NOT_SPECIFIED) {
                dayOfMonthToLastDay(calendar);
            }
            else {
                lastWeekDayInMonth(calendar, dayOfWeek);
            }
        }
        else if (dayOfMonth == SECOND_LAST_DAY_OF_MONTH) {
            dayOfMonth = lastDayOfMonth(calendar) - 1;
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }
        else {
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }
    }

    private void lastWeekDayInMonth(Calendar calendar, int dayOfWeek) {
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek + 1);
        calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, -1);
    }

    private void dayOfMonthToLastDay(Calendar calendar) {
        int dayOfMonth;
        dayOfMonth = lastDayOfMonth(calendar);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    }

    private int lastDayOfMonth(Calendar calendar) {
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    @Override
    public int length() {
        return LENGTH;
    }

    public enum Month {
        JANUARY(1),
        FEBRUARY(2),
        MARCH(3),
        APRIL(4),
        MAY(5),
        JUNE(6),
        JULY(7),
        AUGUST(8),
        SEPTEMBER(9),
        OCTOBER(10),
        NOVEMBER(11),
        DECEMBER(12);

        private int value;

        private Month(int value) {
            this.value = value;
        }
    }

    @Override
    public int get(Field field) {
        switch (field) {
        case YEAR:

            int year = this.octetString[0] << 8;
            year |= this.octetString[1] & 0xff;
            return year;

        case MONTH:
            return (this.octetString[2] & 0xff);

        case DAY_OF_MONTH:
            return this.octetString[3] & 0xff;

        case DAY_OF_WEEK:
            return this.octetString[4] & 0xff;

        default:

            throw new IllegalArgumentException(
                    String.format("Field %s found in %s.", field.name(), getClass().getSimpleName()));
        }

    }

}
