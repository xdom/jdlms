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

import static org.openmuc.jdlms.ConformanceSetting.ACTION;
import static org.openmuc.jdlms.ConformanceSetting.BLOCK_TRANSFER_WITH_GET_OR_READ;
import static org.openmuc.jdlms.ConformanceSetting.GET;
import static org.openmuc.jdlms.ConformanceSetting.MULTIPLE_REFERENCES;
import static org.openmuc.jdlms.ConformanceSetting.PARAMETERIZED_ACCESS;
import static org.openmuc.jdlms.ConformanceSetting.READ;
import static org.openmuc.jdlms.ConformanceSetting.SELECTIVE_ACCESS;
import static org.openmuc.jdlms.ConformanceSetting.SET;
import static org.openmuc.jdlms.ConformanceSetting.WRITE;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents a logical device in the physical server/meter.
 */
public class LogicalDevice {

    private static final int LD_NAME_MAX_LENGTH = 16;
    private final int logicalDeviceId;
    private final List<CosemInterfaceObject> cosemObjects;
    private final String logicalDeviceName;
    private final Map<Integer, SecuritySuite> restrictions;
    private Set<ConformanceSetting> conformance;
    private final String manufacturerId;
    @SuppressWarnings("unused")
    private final long deviceId;
    private byte[] systemTitle;
    private byte[] masterKey;

    /**
     * Creates a new Logical Device.
     * 
     * @param logicalDeviceId
     *            logical device id. Id to identify the logical device. Integer greater than 0.
     * 
     * @param logicalDeviceName
     *            The logical device name is defined as an octet-string of up to 16 octets/characters. The first three
     *            octets/characters shall carry the manufacturer identifier. The manufacturer shall ensure that the
     *            logical device name, starting with the three octets/characters identifying the manufacturer and
     *            followed by up to 13 octets/characters, is unique.
     * 
     * @param manufacturerId
     *            unique String ID of <u>three</u> characters. See:
     *            <a href="http://dlms.com/organization/flagmanufacturesids/">DLMS UA FLAG Manufacturers ID</a>
     * 
     * @param deviceId
     *            5 byte device ID.
     * 
     * @throws IllegalArgumentException
     *             if a parameter does not fulfill its requirements.
     */
    public LogicalDevice(int logicalDeviceId, String logicalDeviceName, String manufacturerId, long deviceId) {

        this.manufacturerId = manufacturerId;
        this.deviceId = deviceId;

        if (logicalDeviceName.length() > LD_NAME_MAX_LENGTH) {
            String message = MessageFormat.format("Logical device name length is greater than {0}.",
                    LD_NAME_MAX_LENGTH);
            throw new IllegalArgumentException(message);
        }
        if (logicalDeviceId < 1) {
            String message = "Logical Device ID must be greater than one.";
            throw new IllegalArgumentException(message);
        }

        this.logicalDeviceId = logicalDeviceId;
        this.logicalDeviceName = logicalDeviceName;
        this.cosemObjects = new LinkedList<>();
        this.restrictions = new HashMap<>();

        setSystemTitle(manufacturerId, deviceId);

        setConformance(GET, SET, ACTION, BLOCK_TRANSFER_WITH_GET_OR_READ, MULTIPLE_REFERENCES, READ, WRITE,
                SELECTIVE_ACCESS, PARAMETERIZED_ACCESS);
    }

    private void setSystemTitle(String manufacturerId, long deviceId) {
        final int manIdLength = 3;

        if (manufacturerId.length() != manIdLength) {
            throw new IllegalArgumentException("Manufacterer ID must be three charactes long.");
        }

        this.systemTitle = new byte[8];

        byte[] manufacturerIdBytes = manufacturerId.getBytes(StandardCharsets.US_ASCII);

        for (int i = 0; i < manIdLength; i++) {
            this.systemTitle[i] = manufacturerIdBytes[i];
        }

        for (int i = 0; i < 5; i++) {
            int shift = (4 - i) * 8;
            long v = deviceId >> shift;
            this.systemTitle[i + manIdLength] = (byte) (0xffL & v);
        }
    }

    public String getLogicalDeviceName() {
        return logicalDeviceName;
    }

    public String getManufacturerId() {
        return this.manufacturerId;
    }

    public int getLogicalDeviceId() {
        return logicalDeviceId;
    }

    public byte[] getMasterKey() {
        return masterKey;
    }

    /**
     * Sets the master key.
     * 
     * <p>
     * <b>NOTE:</b> The master key must be 128 bits (16 byte) long.
     * </p>
     * 
     * @param masterKey
     *            the master key.
     * @return the logical device.
     * 
     * @throws IllegalArgumentException
     *             if the key length is not supported.
     */
    public LogicalDevice setMasterKey(byte[] masterKey) {
        int numOfBits = masterKey.length << 3;
        if (numOfBits != 128) {
            throw new IllegalArgumentException("Key length not 128 bits.");
        }

        this.masterKey = masterKey;
        return this;
    }

    /**
     * Adds a restriction to the Logical Device. If there's no restriction set up, the server allows connections from
     * all client IDs.
     * 
     * <p>
     * <b>NOTE:</b> The master key must be set if a key is used in the authentication.
     * </p>
     * 
     * @param clientId
     *            the client ID. Value greater than zero.
     * @param securitySuite
     *            a new restriction/security suite for the logical device.
     * 
     * @return <code>true</code> if the clientId was not set so far, <code>false</code> otherwise.
     * 
     * @throws IllegalArgumentException
     *             if the arguments don't fulfill their requirements.
     * 
     * @see #setMasterKey(byte[])
     */
    public boolean addRestriction(int clientId, SecuritySuite securitySuite) {
        if (clientId < 1) {
            throw new IllegalArgumentException("Client ID must be grater than zero.");
        }

        if (securitySuite == null) {
            throw new IllegalArgumentException("Authentication object must not be null.");
        }

        return this.restrictions.put(clientId, securitySuite) != null;
    }

    /**
     * Register a new COSEM class. It's not allowed to register classes with identical instance IDs (OBIS code). The
     * class must be annotated with CosemClass.
     * 
     * @param cosemObject
     *            a class annotated with {@link CosemClass}.
     * @return the LogicalDevice instance.
     * 
     * @see CosemClass
     */
    public LogicalDevice registerCosemObject(CosemInterfaceObject... cosemObject) {
        return registerCosemObject(Arrays.asList(cosemObject));
    }

    /**
     * Register a new COSEM class. It's not allowed to register classes with identical instance IDs (OBIS code). The
     * class must be annotated with CosemClass.
     * 
     * @param cosemObject
     *            a class annotated with {@link CosemClass}.
     * @return the LogicalDevice instance.
     */
    public LogicalDevice registerCosemObject(List<CosemInterfaceObject> cosemObject) {
        this.cosemObjects.addAll(cosemObject);

        return this;
    }

    public Map<Integer, SecuritySuite> getRestrictions() {
        return this.restrictions;
    }

    List<CosemInterfaceObject> getCosemObjects() {
        return this.cosemObjects;
    }

    public Set<ConformanceSetting> getConformance() {
        return conformance;
    }

    public byte[] getSystemTitle() {
        return systemTitle;
    }

    /**
     * Sets the logical device conformance. No conformance setting is set, the logical device accepts all service
     * requests.
     * 
     * @param conformanceSetting
     *            set the conformance settings.
     * @return the LogicalDevice instance.
     */
    public LogicalDevice setConformance(ConformanceSetting... conformanceSetting) {
        ConformanceSetting[] cs = conformanceSetting;
        if (conformanceSetting.length == 0) {
            cs = ConformanceSetting.values();
        }
        this.conformance = new HashSet<>(Arrays.asList(cs));
        return this;
    }

}
