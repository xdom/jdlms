package org.openmuc.jdlms.datatypes;

import java.util.Map;

import org.openmuc.jdlms.internal.DlmsEnumFunctions;

/**
 * A COSEM compact array type.
 */
public class CompactArray {

    private final TypeDesc typeDescription;
    private final byte[] arrayContents;

    public CompactArray(TypeDesc typeDescription, byte[] arrayContents) {
        this.typeDescription = typeDescription;
        this.arrayContents = arrayContents;
    }

    public TypeDesc getTypeDescription() {
        return typeDescription;
    }

    public byte[] getArrayContents() {
        return arrayContents;
    }

    /**
     * The type description of a COSEM Compact Array.
     */
    public static class TypeDesc {
        private final Type type;
        private final Object value;

        public TypeDesc(Object value, Type type) throws IllegalArgumentException {
            if ((type == Type.ARRAY || type == Type.STRUCTURE) && value == null) {
                throw new IllegalArgumentException("For type structure/array the value must be set!");
            }
            this.type = type;
            this.value = value;
        }

        public TypeDesc(Type type) throws IllegalArgumentException {
            this(null, type);
        }

        /**
         * Get the value of the compact array.
         * 
         * <p>
         * The value is null if its not of type {@link Type#ARRAY} or {@link Type#STRUCTURE}.
         * </p>
         * 
         * @param <T>
         *            either {@link Type#ARRAY} or {@link Type#STRUCTURE}
         * 
         * @return the value of the compact array.
         */
        @SuppressWarnings("unchecked")
        public <T> T getValue() {
            return (T) value;
        }

        public Type getType() {
            return this.type;
        }

        /**
         * The types of a type description.
         */
        public enum Type implements DlmsEnumeration {
            ERR_NONE_SELECTED(-1),
            NULL_DATA(0),
            ARRAY(1),
            STRUCTURE(2),
            BOOL(3),
            BIT_STRING(4),
            DOUBLE_LONG(5),
            DOUBLE_LONG_UNSIGNED(6),
            OCTET_STRING(9),
            VISIBLE_STRING(10),
            UTF8_STRING(12),
            BCD(13),
            INTEGER(15),
            LONG_INTEGER(16),
            UNSIGNED(17),
            LONG_UNSIGNED(18),
            LONG64(20),
            LONG64_UNSIGNED(21),
            ENUMERATE(22),
            FLOAT32(23),
            FLOAT64(24),
            DATE_TIME(25),
            DATE(26),
            TIME(27),
            DONT_CARE(255),;

            private static final Map<Long, Type> mapping;

            private long code;

            static {
                mapping = DlmsEnumFunctions.generateEnumMap(Type.class);
            }

            private Type(int value) {
                this.code = value;
            }

            @Override
            public long getCode() {
                return this.code;
            }

            public static Type forValue(long code) {
                return DlmsEnumFunctions.constantFor(mapping, code, Type.ERR_NONE_SELECTED);
            }

        }

    }

    /**
     * The description array of a COSEM Compact Array.
     */
    public static class DescriptionArray {
        private final int numOfeElements;
        private final TypeDesc typeDescription;

        public DescriptionArray(int numOfeElements, TypeDesc typeDescription) {
            this.numOfeElements = numOfeElements;
            this.typeDescription = typeDescription;
        }

        public int getNumOfeElements() {
            return numOfeElements;
        }

        public TypeDesc getTypeDescription() {
            return typeDescription;
        }
    }

}
