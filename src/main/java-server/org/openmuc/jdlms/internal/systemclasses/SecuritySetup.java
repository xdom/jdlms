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
package org.openmuc.jdlms.internal.systemclasses;

import static org.openmuc.jdlms.AttributeAccessMode.AUTHENTICATED_READ_ONLY;
import static org.openmuc.jdlms.internal.WellKnownInstanceIds.SECURITY_SETUP_ID;

import java.util.List;
import java.util.Map;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemMethod;
import org.openmuc.jdlms.CosemSnInterfaceObject;
import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.IllegalMethodAccessException;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.MethodAccessMode;
import org.openmuc.jdlms.MethodResultCode;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.SecurityUtils;
import org.openmuc.jdlms.SecurityUtils.KeyId;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;
import org.openmuc.jdlms.internal.DataDirectoryImpl;
import org.openmuc.jdlms.internal.DlmsEnumFunctions;
import org.openmuc.jdlms.internal.SecSuiteAccessor;
import org.openmuc.jdlms.internal.ServerConnectionData;

@CosemClass(id = 64, version = 0)
public class SecuritySetup extends CosemSnInterfaceObject {

    @CosemAttribute(id = 2, accessMode = AUTHENTICATED_READ_ONLY)
    private DataObject securityPolicy;

    @CosemAttribute(id = 3, accessMode = AUTHENTICATED_READ_ONLY)
    private DataObject securitySuite;

    @CosemAttribute(id = 4, accessMode = AUTHENTICATED_READ_ONLY)
    private DataObject clientSystemTitle;

    @CosemAttribute(id = 5, accessMode = AUTHENTICATED_READ_ONLY)
    private final DataObject serverSystemTitle;

    @CosemDataDirectory
    private DataDirectoryImpl dataDirectory;

    private final byte[] masterKey;

    private final LogicalDevice logicalDevice;

    public SecuritySetup(LogicalDevice logicalDevice) {
        super(0, SECURITY_SETUP_ID);
        this.logicalDevice = logicalDevice;
        this.masterKey = logicalDevice.getMasterKey();
        this.serverSystemTitle = DataObject.newOctetStringData(logicalDevice.getSystemTitle());
    }

    public DataObject getSecurityPolicy(Long connectionId) {
        ServerConnectionData connectionData = connectionDataFor(connectionId);

        return DataObject.newEnumerateData(connectionData.securitySuite.getSecurityPolicy().getId());
    }

    public DataObject getSecuritySuite(Long connectionId) throws IllegalAttributeAccessException {
        ServerConnectionData connectionData = connectionDataFor(connectionId);
        return DataObject.newEnumerateData(connectionData.securitySuite.getSecurityPolicy().getId());
    }

    private ServerConnectionData connectionDataFor(Long connectionId) {
        return this.dataDirectory.getConnectionData(connectionId);
    }

    public DataObject getClientSystemTitle(Long connectionId) {

        ServerConnectionData connectionData = connectionDataFor(connectionId);

        return DataObject.newOctetStringData(connectionData.clientSystemTitle);
    }

    public DataObject getServerSystemTitle() {
        return this.serverSystemTitle;
    }

    @CosemMethod(id = 2, consumes = Type.ARRAY, accessMode = MethodAccessMode.AUTHENTICATED_ACCESS)
    public void globalKeyTransfer(DataObject keyDatas, Long connectionId) throws IllegalMethodAccessException {
        List<DataObject> keyDataList = keyDatas.getValue();

        if (keyDataList.get(0).getType() != Type.STRUCTURE) {
            throw new IllegalMethodAccessException(MethodResultCode.TYPE_UNMATCHED);
        }

        int clientId = this.dataDirectory.getConnectionData(connectionId).clientId;

        for (DataObject dataObject : keyDataList) {
            updateKey(dataObject, clientId);
        }

    }

    private void updateKey(DataObject keyData, int clientId) throws IllegalMethodAccessException {
        List<DataObject> keyDataL = keyData.getValue();

        if (keyDataL.size() != 2) {
            throw new IllegalMethodAccessException(MethodResultCode.TYPE_UNMATCHED);
        }

        DataObject keyIdDo = keyDataL.get(0);
        DataObject keyWrappedDo = keyDataL.get(1);

        if (keyIdDo.getType() != Type.ENUMERATE || keyWrappedDo.getType() != Type.OCTET_STRING) {
            throw new IllegalMethodAccessException(MethodResultCode.TYPE_UNMATCHED);
        }

        Number keyIdNum = keyIdDo.getValue();

        KeyId keyId = DlmsEnumFunctions.enumValueFrom(keyIdNum.intValue(), KeyId.class);

        if (keyId == KeyId.GLOBAL_BROADCAST_ENCRYPTION_KEY) {
            // broadcast key not supported
            throw new IllegalMethodAccessException(MethodResultCode.OTHER_REASON);
        }

        byte[] wrappedKey = keyWrappedDo.getValue();

        byte[] unwrappedKey;
        try {
            unwrappedKey = SecurityUtils.unwrapAesRFC3394Key(this.masterKey, wrappedKey);
        } catch (InvalidCipherTextException e) {
            throw new IllegalMethodAccessException(MethodResultCode.OTHER_REASON);
        }

        Map<Integer, SecuritySuite> restrictions = this.logicalDevice.getRestrictions();
        SecSuiteAccessor secAccessor = (SecSuiteAccessor) restrictions.get(clientId);
        switch (keyId) {
        case AUTHENTICATION_KEY:
            secAccessor.updateAuthentciationKey(unwrappedKey);
            break;
        case GLOBAL_UNICAST_ENCRYPTION_KEY:
            secAccessor.updateGlobalUnicastEncryptionKey(unwrappedKey);
            break;

        case GLOBAL_BROADCAST_ENCRYPTION_KEY:
        default:
            throw new IllegalMethodAccessException(MethodResultCode.OTHER_REASON);
        }

    }

}
