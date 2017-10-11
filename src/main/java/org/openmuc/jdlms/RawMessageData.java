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
package org.openmuc.jdlms;

import static java.text.MessageFormat.format;

import javax.xml.bind.DatatypeConverter;

/**
 * jDLMS raw message data for logging purposes.
 * 
 * @see TcpConnectionBuilder#setRawMessageListener(RawMessageListener)
 */
public class RawMessageData {

    private final MessageSource messageSource;
    private final byte[] message;
    private final Apdu apdu;

    private RawMessageData(MessageSource messageSource, byte[] message, Apdu apdu) {
        this.messageSource = messageSource;
        this.message = message;
        this.apdu = apdu;
    }

    /**
     * Get the source of the message.
     * 
     * @return the source of the message.
     */
    public MessageSource getMessageSource() {
        return this.messageSource;

    }

    /**
     * Get the whole message, which is transmitted between client and server.
     * 
     * @return the message as byte array.
     */
    public byte[] getMessage() {
        return this.message;

    }

    /**
     * Get the APDU.
     * 
     * @return return the APDU.
     */
    public Apdu getApdu() {
        return this.apdu;
    }

    @Override
    public String toString() {
        String pattern2 = "{0}: {1}";
        String pattern = "{0}: {1},\n";
        return new StringBuilder().append(format(pattern, "Source", this.messageSource))
                .append(format(pattern, "Message", HexConverter.toHexString(this.message)))
                .append(format(pattern2, "APDU", this.apdu))
                .toString();
    }

    /**
     * The source of the message.
     */
    public enum MessageSource {
        /**
         * The client has send the message.
         */
        CLIENT,
        /**
         * The server has send the message.
         */
        SERVER
    }

    /**
     * Representation of an APDU.
     */
    public static class Apdu {

        private final CosemPdu cosemPdu;
        private final byte[] acsePdu;

        /**
         * Construct an new APDU.
         * 
         * @param cosemPdu
         *            the COSEM PDU.
         * @param acsePdu
         *            the ACSE PDU.
         */
        public Apdu(CosemPdu cosemPdu, byte[] acsePdu) {
            this.cosemPdu = cosemPdu;
            this.acsePdu = acsePdu;
        }

        /**
         * Get the COSEM pdu.
         * 
         * <p>
         * This is null for a graceful disconnect.
         * </p>
         * 
         * @return the COSEM PDU.
         */
        public CosemPdu getCosemPdu() {
            return this.cosemPdu;

        }

        /**
         * Get the ACSE PDU of the DLMS message.
         * 
         * <p>
         * Note: this may be null.
         * </p>
         * 
         * @return the ACSE PDU.
         */
        public byte[] getAcsePdu() {
            return this.acsePdu;
        }

        @Override
        public String toString() {
            final String formatStr = "{0}:\n{1}\n";
            return new StringBuilder().append('\n')
                    .append(format(formatStr, "ACSE PDU", nullableArrayToString(getAcsePdu())))
                    .append(format(formatStr, "COSEM PDU", getCosemPdu()))
                    .toString();
        }

    }

    private static String nullableArrayToString(byte[] data) {
        return data == null ? "[null]" : DatatypeConverter.printHexBinary(data);
    }

    /**
     * Representation of a raw COSEM PDU.
     */
    public static class CosemPdu {
        private final byte[] cipheredCosemPdu;
        private final byte[] plainCosemPdu;

        public CosemPdu(byte[] cipheredCosemPdu, byte[] plainCosemPdu) {
            this.cipheredCosemPdu = cipheredCosemPdu;
            this.plainCosemPdu = plainCosemPdu;
        }

        /**
         * Get the ciphered COSEM PDU.
         * 
         * <p>
         * NOTE: this may be {@code null} if the transmitted message is no encrypted.
         * </p>
         * 
         * @return the ciphered COSEM PDU as byte array.
         */
        public byte[] getCipheredCosemPdu() {
            return this.cipheredCosemPdu;
        }

        /**
         * Get the plain COSEM PDU
         * 
         * @return returns the plain/unencrypted COSEM PDU.
         */
        public byte[] getPlainCosemPdu() {
            return this.plainCosemPdu;
        }

        @Override
        public String toString() {
            return format("Ciphered COSEM PDU: {0}\nPlain COSEM PDU: {1}", nullableArrayToString(this.cipheredCosemPdu),
                    nullableArrayToString(this.plainCosemPdu));
        }

    }

    /**
     * Constructs a RawMessageDataBuilder.
     * 
     * @return the newly constructed RawRawMessageDataBuilder
     */
    public static RawMessageDataBuilder builder() {
        return new RawMessageDataBuilder();
    }

    public static class RawMessageDataBuilder {
        private MessageSource messageSource;
        private byte[] message;
        private Apdu apdu;

        private RawMessageDataBuilder() {
        }

        /**
         * Set the message source of the message.
         * 
         * @param messageSource
         *            the message source.
         * @return the builder.
         */
        public RawMessageDataBuilder setMessageSource(MessageSource messageSource) {
            this.messageSource = messageSource;
            return this;
        }

        /**
         * Set the complete message, transmitted over the line.
         * 
         * @param message
         *            the copmlete message as byte array.
         * @return the builder.
         */
        public RawMessageDataBuilder setMessage(byte[] message) {
            this.message = message;
            return this;
        }

        /**
         * Set the APDU.
         * 
         * @param apdu
         *            the APDU-
         * @return the builder.
         */
        public RawMessageDataBuilder setApdu(Apdu apdu) {
            this.apdu = apdu;
            return this;
        }

        /**
         * Build a new RawMessageData object form the settings.
         * 
         * @return a new RawMessageData.
         */
        public RawMessageData build() {
            return new RawMessageData(messageSource, message, apdu);
        }

    }

}
