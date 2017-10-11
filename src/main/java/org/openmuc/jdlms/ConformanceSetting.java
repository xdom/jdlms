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

/**
 * Conformance block used to negotiate the communication capabilities, between client and server.
 */
public enum ConformanceSetting {
    // SN
    READ(3),
    WRITE(4),
    UNCONFIRMED_WRITE(5),
    /**
     * Event Notification for Sn connections.
     */
    INFORMTION_REPORT(15),
    /**
     * Selective Access for SN connections.
     */
    PARAMETERIZED_ACCESS(18),

    ATTRIBUTE0_SUPPORTED_WITH_SET(8),
    PRIORITY_MGMT_SUPPORTED(9),
    ATTRIBUTE0_SUPPORTED_WITH_GET(10),
    BLOCK_TRANSFER_WITH_GET_OR_READ(11),
    BLOCK_TRANSFER_WITH_SET_OR_WRITE(12),
    BLOCK_TRANSFER_WITH_ACTION(13),

    /**
     * Allow service list requests.
     */
    MULTIPLE_REFERENCES(14),

    GET(19),
    SET(20),
    SELECTIVE_ACCESS(21),
    EVENT_NOTIFICATION(22),
    ACTION(23),;

    private int index;

    private ConformanceSetting(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

}
