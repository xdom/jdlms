package org.openmuc.jdlms;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * Signals, that a exception has occurred.
 * 
 * @see FatalJDlmsException
 * @see NonFatalJDlmsException
 */
public abstract class JDlmsException extends IOException {

    private static final long serialVersionUID = -1656925204526742049L;

    private final ExceptionId exceptionId;
    private final Fault assumedFault;

    public JDlmsException(ExceptionId exceptionId, Fault assumedFault, String message) {
        this(exceptionId, assumedFault, message, null);
    }

    public JDlmsException(ExceptionId exceptionId, Fault assumedFault, String message, Throwable cause) {
        super(message, cause);
        this.exceptionId = exceptionId;
        this.assumedFault = assumedFault;
    }

    @Override
    public String getMessage() {
        return MessageFormat.format("{0}: {2} Assumed fault: {1}.", this.exceptionId.name(),
                this.assumedFault.name().toLowerCase(), super.getMessage());
    }

    /**
     * Access the exception ID.
     * 
     * @return the exception ID
     */
    public ExceptionId getExceptionId() {
        return exceptionId;
    }

    /**
     * Gets the assumed fault. If the fault is user, the user has given a wrong connection parameter.
     * 
     * @return the assumed fault.
     */
    public Fault getAssumedFault() {
        return this.assumedFault;
    }

    /**
     * The fault of the exception cause.
     */
    public enum Fault {
        USER,
        SYSTEM
    }

    /**
     * An exception ID.
     */
    public enum ExceptionId {
        // ---------------------GENERAL-ERRORS---------------------

        /**
         * Failed to establish the connection.
         */
        CONNECTION_ESTABLISH_ERROR,

        ILLEGAL_RESPONSE,

        /**
         * Error in authentication.
         */
        AUTHENTICATION_ERROR,

        /**
         * Authentication required.
         */
        AUTHENTICATION_REQUIRED,
        /**
         * E.g. tried to connect with LN referencing, but server only supports SN.
         */
        WRONG_REFERENCING_METHOD,

        /**
         * Unknown error.
         */
        UNKNOWN,

        /**
         * Signals, that the response timed out.
         */
        RESPONSE_TIMEOUT,
        /**
         * Connection has already been closed.
         */
        CONNECTION_ALREADY_CLOSED,

        /**
         * The Connection has never been opened, but still tried to access.
         */
        CONNECTION_CLOSED,
        /**
         * Graceful disconnect error.
         */
        CONNECTION_DISCONNECT_ERROR,
        /**
         * The returned association result is not know by the stack.
         */
        UNKNOWN_ASSOCIATION_RESULT,

        /**
         * GET.request PDU is too large.
         */
        GET_REQUEST_TOO_LARGE,

        // ---------------------jRXTX------------------------------
        /**
         * The specified COM port does not exist.
         */
        JRXTX_NO_SUCH_PORT,
        /**
         * The specified COM port is locked by an other process.
         */
        JRXTX_PORT_IN_USE,

        /**
         * The specified COM port is not a serial port.
         */
        JRXTX_PORT_NOT_SERIAL,

        /**
         * jRXRX is not fully implemented for your system.
         */
        JRXTX_INCOMPATIBLE_TO_OS,

        // -------------------------------------------WRPPER-------------------------------------------

        /**
         * General WrapperHeader error.
         */
        WRAPPER_HEADER_INVALID,

        /**
         * Denotes, that the header version is incompatible to the stack.
         */
        WRAPPER_HEADER_INVALID_VERSION,
        /**
         * Denotes, that the received source and destination address are not as expected.
         */
        WRAPPER_HEADER_INVALID_SRC_DEST_ADDR,
        /**
         * Denotes, that the specified length in the header, does not match the wrapper header.
         */
        WRAPPER_HEADER_INVALID_PAYLOAD_LENGTH,

        // -------------------------------------------HDLC-FRAME---------------------------------------

        /**
         * Denotes, that the start/end of the HDLC frame wasn't 0x7E.
         */
        HDLC_MSG_INVALID_FLAG,

        /**
         * Error, while connecting to HDLC meter.
         */
        HDLC_CONNECTION_ESTABLISH_ERROR,
        /**
         * Error, while closing HDLC connection.
         */
        HDLC_CONNECTION_CLOSE_ERROR,

        /**
         * Error with the HDLC send frame size or queue.
         */
        HDLC_SEND_FRAME_SIZE_EXCEEDED,

        /**
         * Received a unknown send/destination-keypair from remote meter
         */
        HDLC_KEY_PAIR_ERR,

        // -------------------------------------------61056-21--------------------------------------------
        /**
         * Unknown ACK message.
         */
        IEC_21_UNKNOWN_ACK_MSG,
        /**
         * Baud rate change Delay was wrong.
         */
        IEC_21_WRONG_BAUD_RATE_CHANGE_DELAY,

        /**
         * Couldn't connect with IEC 61056-21
         */
        IEC_21_CONNECTION_ESTABLISH_ERROR,

        ;
    }
}
