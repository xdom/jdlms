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
package org.openmuc.jdlms;

import org.openmuc.jdlms.datatypes.DlmsEnumeration;

/**
 * Enumeration of all possible result codes.
 */
public enum AccessResultCode implements DlmsEnumeration {
    /**
     * Method processed successfully
     */
    SUCCESS(0),
    /**
     * Error on hardware level
     */
    HARDWARE_FAULT(1),
    /**
     * Unknown error, try again
     */
    TEMPORARY_FAILURE(2),
    /**
     * Client does not have the rights to read or write this object
     */
    READ_WRITE_DENIED(3),
    /**
     * Combination of classId, obisCode and attributeId does not exist
     */
    OBJECT_UNDEFINED(4),
    /**
     * Data is not compatible with method called
     */
    OBJECT_CLASS_INCONSISTENT(9),
    /**
     * Object is currently not available at smart meter.
     */
    OBJECT_UNAVAILABLE(11),
    /**
     * Data type is not supported by the remote object
     */
    TYPE_UNMATCHED(12),
    /**
     * Client is not authorized to get or set attribute
     */
    SCOPE_OF_ACCESS_VIOLATED(13),
    /**
     * Remote station was not able to create the next PDU of the pending get or set
     */
    DATA_BLOCK_UNAVAILABLE(14),
    /**
     * Get that needs several PDUs to sent has been canceled
     */
    LONG_GET_ABORTED(15),
    /**
     * Client requested the next PDU while there is no action with multiple PDUs pending
     */
    NO_LONG_GET_IN_PROGRESS(16),
    /**
     * Set that needs several PDUs to sent has been canceled
     */
    LONG_SET_ABORTED(17),
    /**
     * Client requested the next PDU while there is no action with multiple PDUs pending
     */
    NO_LONG_SET_IN_PROGRESS(18),
    /**
     * The block number in the request is not the one expected, or if the next block cannot be delivered.
     */
    DATA_BLOCK_NUMBER_INVALID(19),
    /**
     * Reason unknown
     */
    OTHER_REASON(250);

    private long value;

    private AccessResultCode(long value) {
        this.value = value;
    }

    @Override
    public long getCode() {
        return value;
    }
}
