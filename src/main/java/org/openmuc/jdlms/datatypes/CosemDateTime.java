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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Class representing the COSEM DateTime.
 */
public class CosemDateTime extends CommonDateFormat {

    private static final int DEVIATION_NOT_SPECIFIED = 0x8000;

    private static final int LENGTH = 12;

    /**
     * last three bytes of the final octetString.
     * 
     * Deviation and clock status.
     */
    private byte[] subOctetString;

    private final CosemDate date;
    private final CosemTime time;

    public enum ClockStatus {
        INVALID_VALUE(0x1),
        DUBTFUL_VALUE(0x2),
        DIFFERENT_CLOCK_BASE(0x4),
        INVALID_CLOCK_STATUS(0x8),
        DAYLIGHT_SAVING_ACTIVE(0x80);

        private static final ClockStatus[] values = ClockStatus.values();
        private byte flagMask;

        private ClockStatus(int bitmask) {
            this.flagMask = (byte) bitmask;
        }

        private static byte clockStatusToBitString(ClockStatus... clockStatus) {
            if (clockStatus.length > values.length) {
                throw new IllegalArgumentException("Too many status flags set.");
            }

            byte bitString = 0;

            for (ClockStatus status : clockStatus) {
                bitString |= status.flagMask;
            }
            return bitString;
        }

        public static Set<ClockStatus> clockStatusFrom(byte bitString) {
            Set<ClockStatus> clockStatus = new HashSet<>();

            for (ClockStatus statusFlag : values) {
                if ((bitString & statusFlag.flagMask) == statusFlag.flagMask) {
                    clockStatus.add(statusFlag);
                }
            }

            return clockStatus;
        }
    }

    /**
     * Constructs a a COSEM Date_Time.
     * 
     * @param year
     *            the year from 0 to {@code 0xFFFF}.
     * @param month
     *            the month from 1 to 12. Set to {@code 0xFF} if not specified.
     * @param dayOfMonth
     *            the day of the month starting from 1 to max 31. Set to {@code 0xFE} for the last day of a month and
     *            {@code 0xFD} for the second last day of a month. Set to {@code 0xFF} if not specified.
     * @param hour
     *            the hour from 0 to 23. {@code 0xFF} if not specified.
     * @param minute
     *            the minute from 0 to 59. {@code 0xFF} if not specified.
     * @param second
     *            the second from 0 to 59. {@code 0xFF} if not specified.
     * @param deviation
     *            the deviation in minutes from local time to GMT. From {@code -720} to {@code 720}. {@code 0x8000} if
     *            not specified
     * @param clockStatus
     *            the clock status flags
     * @throws IllegalArgumentException
     *             if a parameter does not fit the range
     */
    public CosemDateTime(int year, int month, int dayOfMonth, int hour, int minute, int second, int deviation,
            ClockStatus... clockStatus) {
        this(year, month, dayOfMonth, 0xff, hour, minute, second, 0xff, deviation, clockStatus);
    }

    /**
     * Constructs a a COSEM Date_Time.
     * 
     * @param year
     *            the year from 0 to 0xffff.
     * @param month
     *            the month from 1 to 12. Set to 0xff if not specified.
     * @param dayOfMonth
     *            the day of the month starting from 1 to max 31. Set to 0xfe for the last day of a month and 0xfd for
     *            the second last day of a month. Set to 0xff if not specified.
     * @param dayOfWeek
     *            the day of a week from 1 to 7. 1 is Monday. Set to 0xff if not specified.
     * @param hour
     *            the hour from 0 to 23. 0xff if not specified.
     * @param minute
     *            the minute from 0 to 59. 0xff if not specified.
     * @param second
     *            the second from 0 to 59. 0xff if not specified.
     * @param hundredths
     *            the hundredths seconds from 0 to 99. 0xff if not specified.
     * @param deviation
     *            the deviation in minutes from local time to GMT. From -720 to 720. 0x8000 if not specified
     * @param clockStatus
     *            the clock status flags
     * @throws IllegalArgumentException
     *             if a parameter does not fit the range
     */
    public CosemDateTime(int year, int month, int dayOfMonth, int dayOfWeek, int hour, int minute, int second,
            int hundredths, int deviation, ClockStatus... clockStatus) {
        this.date = new CosemDate(year, month, dayOfMonth, dayOfWeek);
        this.time = new CosemTime(hour, minute, second, hundredths);

        initFields(deviation, clockStatus);
    }

    public CosemDateTime(long timeStamp, TimeZone timeZone) {
        this.date = new CosemDate(timeStamp);
        this.time = new CosemTime(timeStamp);

        int deviation = (int) TimeUnit.MINUTES.convert(timeZone.getRawOffset(), TimeUnit.MILLISECONDS);
        if (timeZone.getDSTSavings() != 0) {
            initFields(deviation, ClockStatus.DAYLIGHT_SAVING_ACTIVE);
        }
        else {
            initFields(deviation);
        }

    }

    /**
     * Constructs a a COSEM Date_Time.
     * 
     * @param date
     *            the date
     * @param time
     *            the time
     * @param deviation
     *            the deviation in minutes from local time to GMT. From -720 to 720. 0x8000 if not specified
     * @param clockStatus
     *            the clock status flags
     * @throws IllegalArgumentException
     *             if a parameter does not fit the range
     */
    public CosemDateTime(CosemDate date, CosemTime time, int deviation, ClockStatus... clockStatus) {
        this.date = date;
        this.time = time;

        initFields(deviation, clockStatus);
    }

    /**
     * Constructs a a COSEM Date_Time.
     * 
     * @param date
     *            the date
     * @param time
     *            the time
     * @param deviation
     *            the deviation
     * @param deviationTimeUnit
     *            the unit of the deviation value
     * @param clockStatus
     *            the clock status flags
     * @throws IllegalArgumentException
     *             if a parameter does not fit the range
     */
    public CosemDateTime(CosemDate date, CosemTime time, int deviation, TimeUnit deviationTimeUnit,
            ClockStatus... clockStatus) {
        this.date = date;
        this.time = time;

        initFields((int) TimeUnit.MINUTES.convert(deviation, deviationTimeUnit), clockStatus);
    }

    /**
     * Constructs a a COSEM Date_Time. With a deviation not specified.
     * 
     * @param date
     *            the date
     * @param time
     *            the time
     * @param clockStatus
     *            the clock status flags
     * @throws IllegalArgumentException
     *             if a parameter does not fit the range
     */
    public CosemDateTime(CosemDate date, CosemTime time, ClockStatus... clockStatus) {
        this.date = date;
        this.time = time;

        initFields(0x8000, clockStatus);
    }

    private CosemDateTime(CosemDate date, CosemTime time, byte[] subOctetString) {
        this.date = date;
        this.time = time;
        this.subOctetString = subOctetString;
    }

    public static CosemDateTime decode(byte[] octetString) {
        if (octetString.length != LENGTH) {
            throw new IllegalArgumentException("Arrays has an invalid length.");
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(octetString)) {
            return decode(inputStream);
        } catch (IOException e) {
            // shouldn't occur
            return null;
        }
    }

    private static CosemDateTime decode(InputStream inputStream) throws IOException {
        CosemDate date = CosemDate.decode(inputStream);

        CosemTime time = CosemTime.decode(inputStream);

        byte[] subOctetString = new byte[3];

        if (inputStream.read(subOctetString) != 3) {
            throw new IOException("Stream has an invalid length.");
        }
        return new CosemDateTime(date, time, subOctetString);
    }

    private void initFields(int deviation, ClockStatus... clockStatus) {
        validateDeviation(deviation);

        subOctetString = new byte[3];

        subOctetString[0] = (byte) ((deviation & 0xff00) >> 8);
        subOctetString[1] = (byte) (deviation & 0xff);
        subOctetString[2] = ClockStatus.clockStatusToBitString(clockStatus);
    }

    private void validateDeviation(int deviation) {
        if ((deviation < -720 || deviation > 720) && deviation != DEVIATION_NOT_SPECIFIED) {
            throw new IllegalArgumentException("Deviation is out of range.");
        }
    }

    @Override
    public byte[] encode() {

        ByteBuffer byteBuffer = ByteBuffer.allocate(length());
        byte[] dateOctetString = date.encode();
        byteBuffer.put(dateOctetString);

        byte[] timeOctetString = time.encode();
        byteBuffer.put(timeOctetString);

        byteBuffer.put(this.subOctetString);
        return byteBuffer.array();
    }

    @Override
    public Calendar toCalendar() {

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        date.initCalendar(cal);
        time.initCalendar(cal);

        short deviation = (short) get(Field.DEVIATION);

        Set<ClockStatus> clockStatus = ClockStatus.clockStatusFrom((byte) get(Field.CLOCK_STATUS));
        if (clockStatus.contains(ClockStatus.DAYLIGHT_SAVING_ACTIVE)) {
            // cal.set(Calendar.DST_OFFSET, (int) TimeUnit.MILLISECONDS.convert(-1, TimeUnit.HOURS));
        }

        if (((deviation & 0xFFFF) ^ 0x8000) != 0x0) {
            int timeZoneOffset = (int) TimeUnit.MILLISECONDS.convert(deviation, TimeUnit.MINUTES);
            cal.set(Calendar.ZONE_OFFSET, timeZoneOffset);
        }

        return cal;
    }

    @Override
    public int length() {
        return LENGTH;
    }

    @Override
    public int get(Field field) {
        switch (field) {
        case DEVIATION:
            short deviation = (short) ((this.subOctetString[0] & 0xff) << 8);
            deviation |= this.subOctetString[1] & 0xff;
            return deviation;

        case CLOCK_STATUS:
            return this.subOctetString[2];

        case HOUR:
        case MINUTE:
        case SECOND:
        case HUNDREDTHS:
            return this.time.get(field);

        case YEAR:
        case MONTH:
        case DAY_OF_MONTH:
        case DAY_OF_WEEK:
            return this.date.get(field);

        default:
            // can't occur
            throw new IllegalArgumentException();
        }

    }
}
