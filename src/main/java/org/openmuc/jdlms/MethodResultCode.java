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
 * Enumeration of all possible result code a smart meter can send after a method has been called via an action operation
 */
public enum MethodResultCode implements DlmsEnumeration {
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
     * Client is not authorized to call method.
     */
    SCOPE_OF_ACCESS_VIOLATION(13),
    /**
     * Remote station was not able to create the next PDU of the pending action
     */
    DATA_BLOCK_UNAVAILABLE(14),
    /**
     * Action that needs several PDUs to sent has been canceled
     */
    LONG_ACTION_ABORTED(15),
    /**
     * Client requested the next PDU while there is no action with multiple PDUs pending
     */
    NO_LONG_ACTION_IN_PROGRESS(16),
    /**
     * Reason unknown
     */
    OTHER_REASON(250);

    private int code;

    private MethodResultCode(int code) {
        this.code = code;
    }

    @Override
    public long getCode() {
        return this.code;
    }
}
