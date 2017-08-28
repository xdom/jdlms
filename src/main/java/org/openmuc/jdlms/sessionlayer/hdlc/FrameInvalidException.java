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
package org.openmuc.jdlms.sessionlayer.hdlc;

import java.util.List;

import org.openmuc.jdlms.sessionlayer.hdlc.FrameRejectReason.RejectReason;

public class FrameInvalidException extends Exception {
    private FrameRejectReason reason;

    FrameInvalidException(String message) {
        super(message);
    }

    FrameInvalidException(String message, FrameRejectReason reason) {
        super(String.format("%s. REASON: %s", message, reasonsToString(reason.rejectReasons())));
        this.reason = reason;
    }

    private static String reasonsToString(List<RejectReason> reasons) {
        StringBuilder builder = new StringBuilder();
        for (RejectReason rejectReason : reasons) {
            builder.append(rejectReason.name()).append(", ");
        }
        return builder.toString();
    }

    public FrameRejectReason rejectReason() {
        return reason;
    }
}
