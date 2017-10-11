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
package org.openmuc.jdlms.internal.association;

import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.AssociateSourceDiagnostic.AcseServiceUser;
import org.openmuc.jdlms.internal.AssociationResult;
import org.openmuc.jdlms.internal.ConformanceSettingConverter;
import org.openmuc.jdlms.internal.ContextId;
import org.openmuc.jdlms.internal.asn1.cosem.Conformance;

/**
 * This exception is thrown, if the initiate request is bad.
 */
public class AssociatRequestException extends GenericAssociationException {
    private final AcseServiceUser reason;
    private final transient Conformance conformanceSetting;

    public AssociatRequestException(AcseServiceUser reason) {
        this.reason = reason;
        this.conformanceSetting = ConformanceSettingConverter.conformanceFor();
    }

    public AcseServiceUser getReason() {
        return reason;
    }

    @Override
    public APdu getErrorMessageApdu() {
        return new InitiateResponseBuilder(this.conformanceSetting)
                .setContextId(ContextId.LOGICAL_NAME_REFERENCING_NO_CIPHERING)
                .setResult(AssociationResult.REJECTED_PERMANENT)
                .setAssociateSourceDiagnostic(this.reason)
                .build();
    }
}
