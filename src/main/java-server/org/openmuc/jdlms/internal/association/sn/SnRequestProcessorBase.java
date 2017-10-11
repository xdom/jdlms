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
package org.openmuc.jdlms.internal.association.sn;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.BaseNameRange;
import org.openmuc.jdlms.internal.BaseNameRange.Access;
import org.openmuc.jdlms.internal.BaseNameRangeSet;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.ACSEApdu;
import org.openmuc.jdlms.internal.association.AssociationMessenger;
import org.openmuc.jdlms.internal.association.RequestProcessorBase;
import org.openmuc.jdlms.internal.association.RequestProcessorData;

abstract class SnRequestProcessorBase extends RequestProcessorBase {

    protected final BaseNameRangeSet nameRangeSet;

    public SnRequestProcessorBase(AssociationMessenger associationMessenger,
            RequestProcessorData requestProcessorData) {
        super(associationMessenger, requestProcessorData);

        this.nameRangeSet = requestProcessorData.directory.baseNameRangesFor(this.requestProcessorData.logicalDeviceId);
    }

    protected final APdu newAPdu() {
        ACSEApdu acseAPdu = null;
        COSEMpdu cosemPdu = new COSEMpdu();

        return new APdu(acseAPdu, cosemPdu);
    }

    protected final Access accessFor(final int variableName, BaseNameRange intersectingRange)
            throws IllegalAttributeAccessException {
        boolean baseNameExistent = intersectingRange != null;
        if (!baseNameExistent) {
            throw new IllegalAttributeAccessException(AccessResultCode.OBJECT_UNDEFINED);
        }
        return intersectingRange.accessFor(variableName);
    }

}
