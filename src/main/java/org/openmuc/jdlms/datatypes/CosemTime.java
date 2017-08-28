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

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Class representing a COSEM Time.
 */
public class CosemTime extends CommonDateFormat {

    static final int LENGTH = 4;

    private byte[] octetString;

    /**
     * Constructs a COSEM Time.
     * 
     * @param hour
     *            the hour from 0 to 23. 0xff if not specified.
     * 
     * @param minute
     *            the minute from 0 to 59. 0xff if not specified.
     * @param second
     *            the second from 0 to 59. 0xff if not specified.
     * @throws IllegalArgumentException
     *             if a parameter does not fit the range
     */
    public CosemTime(int hour, int minute, int second) {
        this(hour, minute, second, 0xff);
    }

    /**
     * Constructs a COSEM Time.
     * 
     * @param hour
     *            the hour from 0 to 23. 0xff if not specified.
     * 
     * @param minute
     *            the minute from 0 to 59. 0xff if not specified.
     * @param second
     *            the second from 0 to 59. 0xff if not specified.
     * @param hundredths
     *            the hundredths seconds from 0 to 99. 0xff if not specified.
     * @throws IllegalArgumentException
     *             if a parameter does not fit the range
     */
    public CosemTime(int hour, int minute, int second, int hundredths) throws IllegalArgumentException {
        verify(hour, "Hour", 0, 23);
        verify(minute, "Minute", 0, 59);
        verify(second, "Second", 0, 59);
        verify(hundredths, "Hundredths", 0, 99);

        init(hour, minute, second, hundredths);
    }

    private void init(int hour, int minute, int second, int hundredths) {
        this.octetString = new byte[length()];
        this.octetString[0] = (byte) (hour & 0xff);
        this.octetString[1] = (byte) (minute & 0xff);
        this.octetString[2] = (byte) (second & 0xff);
        this.octetString[3] = (byte) (hundredths & 0xff);
    }

    public CosemTime(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int hundredths = calendar.get(Calendar.MILLISECOND) / 10;
        init(hour, minute, second, hundredths);
    }

    private CosemTime(byte[] octetString) {
        this.octetString = octetString;
    }

    public static CosemTime decode(InputStream inputStream) throws IOException {

        byte[] octetString = new byte[LENGTH];

        if (inputStream.read(octetString) != LENGTH) {
            throw new IOException("Stream has a invalid length.");
        }
        return decode(octetString);
    }

    public static CosemTime decode(byte[] octetString) {
        return new CosemTime(octetString);
    }

    private void verify(int value, String name, int lowerBound, int upperBound) {
        if (value < lowerBound || value > upperBound && value != 0xff) {
            throw new IllegalArgumentException(format("%s is out of range [%d, %d]", name, lowerBound, upperBound));
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
        final int hour = get(Field.HOUR);
        if (hour != 0xFF) {
            calendar.set(Calendar.HOUR_OF_DAY, get(Field.HOUR));
        }

        final int minute = get(Field.MINUTE);
        if (minute != 0xFF) {
            calendar.set(Calendar.MINUTE, get(Field.MINUTE));
        }

        final int second = get(Field.SECOND);
        if (second != 0xFF) {
            calendar.set(Calendar.SECOND, get(Field.SECOND));
        }

        final int hundredths = get(Field.HUNDREDTHS);
        if (hundredths != 0xFF) {
            calendar.set(Calendar.MILLISECOND, hundredths * 10);
        }
    }

    @Override
    public int length() {
        return LENGTH;
    }

    @Override
    public int get(Field field) {
        switch (field) {
        case HOUR:
            return this.octetString[0] & 0xff;
        case MINUTE:
            return this.octetString[1] & 0xff;
        case SECOND:
            return this.octetString[2] & 0xff;
        case HUNDREDTHS:
            return this.octetString[3] & 0xff;

        default:
            throw new IllegalArgumentException(
                    String.format("Field %s found in %s.", field.name(), getClass().getSimpleName()));
        }
    }

}
