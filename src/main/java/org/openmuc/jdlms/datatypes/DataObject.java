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
import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Container class holding data about to send to the smart meter or received by the smart meter
 * <p>
 * Either stores a {@link Number}, {@link List} of {@link DataObject}s, a byte array, {@link BitString} or a subtype
 * {@link CosemDateFormat}.
 * </p>
 */
public class DataObject {

    private final Object value;
    private final Type type;

    private DataObject(Object value, Type type) {
        this.value = value;
        this.type = type;
    }

    /**
     * Constructs a empty datum.
     * 
     * <p>
     * COSEM Type {@link Type#NULL_DATA}
     * </p>
     * 
     * @return The data
     */
    public static DataObject newNullData() {
        return new DataObject(null, Type.NULL_DATA);
    }

    /**
     * Constructs a array data.
     * <p>
     * COSEM Type {@link Type#ARRAY}
     * </p>
     * 
     * @param array
     *            The array of values
     * @return The data
     * @throws IllegalArgumentException
     *             If a sub element of array has another data type than the first
     */
    public static DataObject newArrayData(List<DataObject> array) throws IllegalArgumentException {
        if (!array.isEmpty()) {
            Type arrayType = array.get(0).getType();

            int index = 0;
            for (DataObject sub : array) {
                if (sub.getType() != arrayType) {
                    throw new IllegalArgumentException("Array is of type " + arrayType + ", but array at " + index
                            + " is of type " + sub.getType());
                }
                index++;
            }
        }

        return new DataObject(array, Type.ARRAY);
    }

    public static DataObject newCompactArrayData(CompactArray compactArray) {
        return new DataObject(compactArray, Type.COMPACT_ARRAY);
    }

    /**
     * Constructs a structure data.
     * 
     * <p>
     * COSEM Type {@link Type#STRUCTURE}
     * </p>
     * 
     * @param structure
     *            The structure of values
     * @return The data
     * 
     * @see #newStructureData(DataObject...)
     */
    public static DataObject newStructureData(List<DataObject> structure) {
        return new DataObject(structure, Type.STRUCTURE);
    }

    /**
     * Constructs a structure data.
     * 
     * <p>
     * COSEM Type {@link Type#STRUCTURE}
     * </p>
     * 
     * @param element
     *            the structure values.
     * @return The data
     * 
     * @see #newStructureData(List)
     */
    public static DataObject newStructureData(DataObject... element) {
        return newStructureData(Arrays.asList(element));
    }

    /**
     * Constructs a bool data.
     * <p>
     * COSEM Type {@link Type#BOOLEAN}
     * </p>
     * 
     * @param bool
     *            The structure of values
     * @return The data
     */
    public static DataObject newBoolData(boolean bool) {
        return new DataObject(bool, Type.BOOLEAN);
    }

    /**
     * Constructs a bit string data.
     * 
     * <p>
     * COSEM Type {@link Type#BIT_STRING}
     * </p>
     * 
     * @param bitString
     *            The {@link BitString} object holding the bit string
     * @return The data
     */
    public static DataObject newBitStringData(BitString bitString) throws IllegalArgumentException {
        return new DataObject(new BitString(bitString), Type.BIT_STRING);
    }

    /**
     * Constructs a int 32 data.
     * <p>
     * COSEM Type {@link Type#DOUBLE_LONG}
     * </p>
     * 
     * @param int32
     *            he number to store
     * @return The data
     */
    public static DataObject newInteger32Data(int int32) {
        return new DataObject(int32, Type.DOUBLE_LONG);
    }

    /**
     * Constructs a unsigned int 32 data.
     * 
     * <p>
     * COSEM Type {@link Type#DOUBLE_LONG_UNSIGNED}
     * </p>
     * 
     * @param uIn32
     *            he number to store
     * @return The data
     * @throws IllegalArgumentException
     *             if uInt32 is &gt; 2^(32)-1 or negative
     */
    public static DataObject newUInteger32Data(long uIn32) {
        if (uIn32 < 0 || uIn32 > 0xFFFFFFFFL) {
            throw new IllegalArgumentException(MessageFormat.format("Unsigned32 {0} out of range", uIn32));
        }
        return new DataObject(uIn32, Type.DOUBLE_LONG_UNSIGNED);
    }

    /**
     * Constructs a byte array data.
     * <p>
     * COSEM Type {@link Type#OCTET_STRING}
     * </p>
     * 
     * @param string
     *            The byte array to store
     * @return The data
     */
    public static DataObject newOctetStringData(byte[] string) {
        return new DataObject(string.clone(), Type.OCTET_STRING);
    }

    /**
     * Constructs a string, encoded as byte array data.
     * 
     * <p>
     * COSEM Type {@link Type#VISIBLE_STRING}
     * </p>
     * 
     * @param string
     *            The byte string to store
     * @return The data
     */
    public static DataObject newVisibleStringData(byte[] string) {
        return new DataObject(string.clone(), Type.VISIBLE_STRING);
    }

    /**
     * Constructs a UTF-8 string, encoded as byte array data.
     * <p>
     * COSEM Type {@link Type#UTF8_STRING}
     * </p>
     * 
     * @param string
     *            The byte string to store
     * @return The data
     */
    public static DataObject newUtf8StringData(byte[] string) {
        return new DataObject(string.clone(), Type.UTF8_STRING);
    }

    /**
     * Constructs a 2 digit BCD number data
     * 
     * <p>
     * COSEM Type {@link Type#BCD}
     * </p>
     * 
     * @param bcd
     *            The BCD number to store
     * @return The data
     */
    public static DataObject newBcdData(byte bcd) throws IllegalArgumentException {
        return new DataObject(bcd, Type.BCD);
    }

    /**
     * Constructs a int 8 data
     * 
     * <p>
     * COSEM Type {@link Type#INTEGER}
     * </p>
     * 
     * @param int8
     *            The number to store
     * @return The data
     */
    public static DataObject newInteger8Data(byte int8) throws IllegalArgumentException {
        return new DataObject(int8, Type.INTEGER);
    }

    /**
     * Constructs a unsigned int 8 data
     * 
     * <p>
     * COSEM Type {@link Type#UNSIGNED}
     * </p>
     * 
     * @param uInt8
     *            The number to store
     * @return The data
     * @throws IllegalArgumentException
     *             if uInt8 &gt; 2^(8)-1 or negative
     */
    public static DataObject newUInteger8Data(short uInt8) throws IllegalArgumentException {
        if (uInt8 < 0 || uInt8 > 0xFF) {
            throw new IllegalArgumentException("Unsigned8 " + uInt8 + " out of range");
        }
        return new DataObject(uInt8, Type.UNSIGNED);
    }

    /**
     * Constructs a int 16 data
     * 
     * <p>
     * COSEM Type {@link Type#LONG_INTEGER}
     * </p>
     * 
     * @param int16
     *            The number to store
     * @return The data
     */
    public static DataObject newInteger16Data(short int16) {
        return new DataObject(int16, Type.LONG_INTEGER);
    }

    /**
     * Constructs a unsigned int 16 data.
     * <p>
     * COSEM Type {@link Type#LONG_UNSIGNED}
     * </p>
     * 
     * @param uInt16
     *            The number to store
     * @return The data
     * @throws IllegalArgumentException
     *             If newVal &gt; 2^(16)-1 or negative
     */
    public static DataObject newUInteger16Data(int uInt16) {
        if (uInt16 < 0 || uInt16 > 0xFFFF) {
            throw new IllegalArgumentException("Unsigned16 " + uInt16 + " out of range");
        }
        return new DataObject(uInt16, Type.LONG_UNSIGNED);
    }

    /**
     * Constructs a int 64 data.
     * <p>
     * COSEM Type {@link Type#LONG64}
     * </p>
     * 
     * @param int64
     *            The number to store
     * @return The data
     */
    public static DataObject newInteger64Data(long int64) {
        return new DataObject(int64, Type.LONG64);
    }

    /**
     * Constructs a unsigned int 64 data
     * 
     * <p>
     * COSEM Type {@link Type#LONG64_UNSIGNED}
     * </p>
     * 
     * @param uInt64
     *            The number to store
     * @return The data
     * @throws IllegalArgumentException
     *             if uInt64 is negative
     */
    public static DataObject newUInteger64Data(long uInt64) {
        // limited...?
        if (uInt64 < 0) {
            throw new IllegalArgumentException("Unsigned64 " + uInt64 + " out of range");
        }
        return new DataObject(uInt64, Type.LONG64_UNSIGNED);
    }

    /**
     * Constructs a enum data.
     * 
     * <p>
     * COSEM Type {@link Type#ENUMERATE}
     * </p>
     * 
     * @param enumVal
     *            The enum value to store
     * @return The data
     * @throws IllegalArgumentException
     *             if newVal is &gt; 2^(8)-1 or negative
     */
    public static DataObject newEnumerateData(int enumVal) {
        if (enumVal < 0 || enumVal > 0xFF) {
            throw new IllegalArgumentException("Enumeration " + enumVal + " out of range");
        }
        return new DataObject(enumVal, Type.ENUMERATE);
    }

    /**
     * Constructs a 32 bit floating point number data.
     * 
     * <p>
     * COSEM Type {@link Type#FLOAT32}
     * </p>
     * 
     * @param float32
     *            The number to store
     * @return The data
     * 
     */
    public static DataObject newFloat32Data(float float32) {
        return new DataObject(float32, Type.FLOAT32);
    }

    /**
     * Constructs a 64 bit floating point number data.
     * <p>
     * COSEM Type {@link Type#FLOAT64}
     * </p>
     * 
     * @param float64
     *            The number to store
     * @return The data
     * 
     */
    public static DataObject newFloat64Data(double float64) {
        return new DataObject(float64, Type.FLOAT64);
    }

    /**
     * Constructs a calendar datum holding date and time
     * 
     * <p>
     * COSEM Type {@link Type#DATE_TIME}
     * </p>
     * 
     * @param dateTime
     *            The date and time to store
     * @return The data
     */
    public static DataObject newDateTimeData(CosemDateTime dateTime) {
        return new DataObject(dateTime, Type.DATE_TIME);
    }

    /**
     * Constructs a calendar datum holding a date
     * <p>
     * COSEM Type {@link Type#DATE}
     * </p>
     * 
     * @param date
     *            The date store
     * @return The data
     */
    public static DataObject newDateData(CosemDate date) {
        return new DataObject(date, Type.DATE);
    }

    /**
     * Constructs a calendar datum holding a time
     * <p>
     * COSEM Type {@link Type#TIME}
     * </p>
     * 
     * @param time
     *            The time store
     * @return The data
     */
    public static DataObject newTimeData(CosemTime time) {
        return new DataObject(time, Type.TIME);
    }

    /**
     * The type of the current {@link DataObject}.
     * 
     * @return the enumeration type.
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Returns the value.
     * 
     * @param <T>
     *            the type in which the raw data should be cast.
     * @return the typed value.
     * @throws ClassCastException
     *             when the value doesn't match the assigned type.
     * @see #getType()
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue() throws ClassCastException {
        return (T) this.value;
    }

    /**
     * Returns the raw object-value.
     * 
     * @return the raw object-value.
     */
    public Object getRawValue() {
        return this.value;
    }

    /**
     * Is used to determine if the data contains a {@link BitString} object.
     * 
     * @return true if it contains a {@link BitString} object.
     */
    public boolean isBitString() {
        return this.type == Type.BIT_STRING;
    }

    /**
     * Checks if the data of this container is a number
     * 
     * @return true data is a number.
     */
    public boolean isNumber() {
        return type.isNumber();
    }

    /**
     * Checks if the data of this container is of a complex type.
     * <p>
     * A complex container holds one or more sub container of type {@link DataObject} as values.
     * </p>
     * <p>
     * A container is of complex type if {@link DataObject#getType()} returns either {@link Type#ARRAY},
     * {@link Type#STRUCTURE} or {@link Type#COMPACT_ARRAY}.
     * 
     * @return true is the {@link DataObject} holds a {@link List} of {@link DataObject}.
     */
    public boolean isComplex() {
        return type == Type.ARRAY || type == Type.STRUCTURE || type == Type.COMPACT_ARRAY;
    }

    /**
     * Checks if the data of this container is a byte array.
     * <p>
     * A container is a byte array if {@link #getType()} returns either {@link Type#OCTET_STRING},
     * {@linkplain Type#VISIBLE_STRING}, {@linkplain Type#BIT_STRING} or {@linkplain Type#UTF8_STRING}.
     * </p>
     * 
     * @return true if the data is a byte array ({@code byte[]}).
     */
    public boolean isByteArray() {
        return type == Type.OCTET_STRING || type == Type.VISIBLE_STRING || type == Type.UTF8_STRING;
    }

    /**
     * Checks if the data of this container is a boolean.
     * 
     * @return <code>true</code> if the data is a boolean.
     */
    public boolean isBoolean() {
        return type == Type.BOOLEAN;
    }

    /**
     * Checks if the data of this container is a {@link CosemDateFormat} object.
     * <p>
     * A container is a calendar if {@link DataObject#getType()} returns either {@link Type#DATE_TIME},
     * {@link Type#DATE} or {@link Type#TIME}.
     * </p>
     * 
     * @return <code>true</code> if the data is a {@link CosemDateFormat}.
     */
    public boolean isCosemDateFormat() {
        return type == Type.DATE || type == Type.DATE_TIME || type == Type.TIME;
    }

    /**
     * Checks if the data of this container is <code>null</code>.
     * 
     * @return <code>true</code> if the data is <code>null</code>.
     */
    public boolean isNull() {
        return type == Type.NULL_DATA;
    }

    /**
     * Returns a string representation of the {@link DataObject}.
     * 
     * @return the string representation of the object.
     */
    @Override
    public String toString() {
        return printType(this, 0);
    }

    private String printType(DataObject resultData, int shiftChars) {
        StringBuilder strBuilder = new StringBuilder();

        String shift;

        if (shiftChars > 0) {

            final String formatStr = MessageFormat.format("%{0}s%s", shiftChars);
            shift = format(formatStr, " ", "|- ");
        }
        else {
            shift = "";
        }

        String message = format("%s Value: ", resultData.getType().name());
        strBuilder.append(shift).append(message);

        if (resultData.isBoolean()) {
            Boolean boolVal = resultData.getValue();
            strBuilder.append(boolVal.toString());
        }
        else if (resultData.isNumber()) {
            Number number = resultData.getValue();
            strBuilder.append(number.toString());
        }
        else if (resultData.getType() == Type.OCTET_STRING) {
            byte[] octetStr = resultData.getValue();
            StringBuilder strBuf = new StringBuilder();
            for (byte b : octetStr) {
                strBuf.append(String.format("%02X ", b));
            }
            strBuilder.append(strBuf.toString()).append(" (hex)");
        }
        else if (resultData.getType() == Type.VISIBLE_STRING) {
            byte[] visStr = resultData.getValue();
            strBuilder.append(new String(visStr));
        }
        else if (resultData.isBitString()) {
            BitString biStr = resultData.getValue();
            strBuilder.append(printHexBinary(biStr.getBitString()));
        }
        else if (resultData.isCosemDateFormat()) {
            CosemDateFormat cosemFormat = resultData.getValue();
            strBuilder.append(cosemFormat.toCalendar().getTime().toString());
        }
        else if (resultData.isComplex() && resultData.getType() != Type.COMPACT_ARRAY) {
            List<DataObject> complex = resultData.getValue();

            strBuilder.append(complex.size()).append(" element(s).");

            for (DataObject data : complex) {
                strBuilder.append('\n').append(printType(data, shiftChars + 3));
            }
            strBuilder.append('\n');
        }
        else {
            return String.format("%sNo string representation for type %s", shift, resultData.getType().name());
        }

        return strBuilder.toString();
    }

    public enum Type {
        /**
         * Null.
         * <p>
         * See {@linkplain DataObject#newNullData()}
         * </p>
         */
        NULL_DATA(),
        /**
         * Array.
         * <p>
         * See {@linkplain DataObject#newArrayData(List)}
         * </p>
         */
        ARRAY(),
        /**
         * Structure.
         * <p>
         * See {@linkplain DataObject#newStructureData(List)}
         * </p>
         */
        STRUCTURE(),
        /**
         * Bool.
         * <p>
         * See {@linkplain DataObject#newBoolData(boolean)}
         * </p>
         */
        BOOLEAN(),
        /**
         * Bit String.
         * <p>
         * See {@linkplain DataObject#newBitStringData(BitString)}
         * </p>
         */
        BIT_STRING(),
        /**
         * Integer 32.
         * <p>
         * See {@linkplain DataObject#newInteger32Data(int)}
         * </p>
         */
        DOUBLE_LONG(true),
        /**
         * Unsigned integer 32.
         * <p>
         * See {@linkplain DataObject#newUInteger32Data(long)}
         * </p>
         */
        DOUBLE_LONG_UNSIGNED(true),
        /**
         * Octet String.
         * <p>
         * See {@linkplain DataObject#newOctetStringData(byte[])}
         * </p>
         */
        OCTET_STRING(),
        /**
         * UTF-8 String.
         * <p>
         * See {@linkplain DataObject#newUtf8StringData(byte[])}
         * </p>
         */
        UTF8_STRING(),
        /**
         * Visible String.
         * <p>
         * See {@linkplain DataObject#newVisibleStringData(byte[])}
         * </p>
         */
        VISIBLE_STRING(),
        /**
         * BCD.
         * <p>
         * See {@linkplain DataObject#newBcdData(byte)}
         * </p>
         */
        BCD(true),
        /**
         * Integer 8.
         * <p>
         * See {@linkplain DataObject#newInteger8Data(byte)}
         * </p>
         */
        INTEGER(true),
        /**
         * Integer 16.
         * <p>
         * See {@linkplain DataObject#newInteger16Data(short)}
         * </p>
         */
        LONG_INTEGER(true),
        /**
         * Unsigned integer 8.
         * <p>
         * See {@linkplain DataObject#newUInteger8Data(short)}
         * </p>
         */
        UNSIGNED(true),
        /**
         * Unsigned integer 16.
         * <p>
         * See {@linkplain DataObject#newUInteger16Data(int)}
         * </p>
         */
        LONG_UNSIGNED(true),
        /**
         * Compact array.
         * 
         * <p>
         * See {@linkplain DataObject#newCompactArrayData(CompactArray)}
         * </p>
         */
        COMPACT_ARRAY(),
        /**
         * Integer 64.
         * <p>
         * See {@linkplain DataObject#newInteger64Data(long)}
         * </p>
         */
        LONG64(true),
        /**
         * Unsigned integer 64.
         * <p>
         * See {@linkplain DataObject#newUInteger64Data(long)}
         * </p>
         */
        LONG64_UNSIGNED(true),
        /**
         * Enum.
         * <p>
         * See {@linkplain DataObject#newEnumerateData(int)}
         * </p>
         */
        ENUMERATE(true),
        /**
         * Float 32.
         * <p>
         * See {@linkplain DataObject#newFloat32Data(float)}
         * </p>
         */
        FLOAT32(true),
        /**
         * Float 64.
         * <p>
         * See {@linkplain DataObject#newFloat64Data(double)}
         * </p>
         */
        FLOAT64(true),
        /**
         * Date Time.
         * <p>
         * See {@linkplain DataObject#newDateData(CosemDate)}
         * </p>
         */
        DATE_TIME(),
        /**
         * Date.
         * <p>
         * See {@linkplain DataObject#newDateData(CosemDate)}
         * </p>
         */
        DATE(),
        /**
         * Time.
         * <p>
         * See {@linkplain DataObject#newTimeData(CosemTime)}
         * </p>
         */
        TIME(),
        DONT_CARE();

        private boolean isNumber;

        private Type() {
            this(false);
        }

        private Type(boolean isNumber) {
            this.isNumber = isNumber;
        }

        private boolean isNumber() {
            return isNumber;
        }
    }

}
