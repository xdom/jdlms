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
package org.openmuc.jdlms.internal;

import org.openmuc.jdlms.internal.asn1.cosem.ACTION_Response;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.GET_Response;
import org.openmuc.jdlms.internal.asn1.cosem.Invoke_Id_And_Priority;
import org.openmuc.jdlms.internal.asn1.cosem.SET_Response;

public class PduHelper {

    public static final int INVALID_INVOKE_ID = -1;

    private static int invokeIdFrom(GET_Response pdu) {
        switch (pdu.getChoiceIndex()) {
        case GET_RESPONSE_NORMAL:
            return invokeIdFrom(pdu.get_response_normal.invoke_id_and_priority);

        case GET_RESPONSE_WITH_DATABLOCK:
            return invokeIdFrom(pdu.get_response_with_datablock.invoke_id_and_priority);

        case GET_RESPONSE_WITH_LIST:
            return invokeIdFrom(pdu.get_response_with_list.invoke_id_and_priority);

        default:
            return INVALID_INVOKE_ID;
        }

    }

    private static int invokeIdFrom(SET_Response pdu) {

        switch (pdu.getChoiceIndex()) {
        case SET_RESPONSE_NORMAL:
            return invokeIdFrom(pdu.set_response_normal.invoke_id_and_priority);

        case SET_RESPONSE_WITH_LIST:
            return invokeIdFrom(pdu.set_response_with_list.invoke_id_and_priority);

        case SET_RESPONSE_DATABLOCK:
            return invokeIdFrom(pdu.set_response_datablock.invoke_id_and_priority);

        case SET_RESPONSE_LAST_DATABLOCK:
            return invokeIdFrom(pdu.set_response_last_datablock.invoke_id_and_priority);

        case SET_RESPONSE_LAST_DATABLOCK_WITH_LIST:
            return invokeIdFrom(pdu.set_response_last_datablock_with_list.invoke_id_and_priority);

        default:
            return INVALID_INVOKE_ID;
        }

    }

    private static int invokeIdFrom(ACTION_Response pdu) {
        switch (pdu.getChoiceIndex()) {
        case ACTION_RESPONSE_NORMAL:
            return invokeIdFrom(pdu.action_response_normal.invoke_id_and_priority);

        case ACTION_RESPONSE_WITH_LIST:
            return invokeIdFrom(pdu.action_response_with_list.invoke_id_and_priority);

        case ACTION_RESPONSE_NEXT_PBLOCK:
            return invokeIdFrom(pdu.action_response_next_pblock.invoke_id_and_priority);

        case ACTION_RESPONSE_WITH_PBLOCK:
            return invokeIdFrom(pdu.action_response_with_pblock.invoke_id_and_priority);

        default:
            return INVALID_INVOKE_ID;
        }

    }

    public static int invokeIdFrom(COSEMpdu cosemPdu) {
        switch (cosemPdu.getChoiceIndex()) {
        case ACTION_RESPONSE:
            return invokeIdFrom(cosemPdu.action_response);
        case GET_RESPONSE:
            return invokeIdFrom(cosemPdu.get_response);
        case SET_RESPONSE:
            return invokeIdFrom(cosemPdu.set_response);

        default:
            return INVALID_INVOKE_ID;
        }

    }

    public static int invokeIdFrom(Invoke_Id_And_Priority invokeIdAndPriority) {
        return invokeIdAndPriority.getValue()[0] & 0xf;
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private PduHelper() {
    }

}
