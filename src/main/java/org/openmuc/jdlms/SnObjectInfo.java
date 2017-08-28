package org.openmuc.jdlms;

import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

/**
 * Object info for the short name addressing.
 */
public class SnObjectInfo implements Serializable {
    private final int baseName;
    private final int classId;
    private final int version;
    private final MethodIdOffsetPair firstMethodIdOffsetPair;

    /**
     * Create a new short name object info.
     * 
     * @param baseName
     *            the base name (SN Address).
     * @param classId
     *            the class id of the COSEM object.
     * @param version
     *            the version of the COSEM object.
     * 
     * @see #SnObjectInfo(int, int, int, int, int)
     */
    public SnObjectInfo(int baseName, int classId, int version) {
        this(baseName, classId, version, null);
    }

    /**
     * 
     * Create a new short name object info.
     * 
     * @param baseName
     *            the base name (SN Address).
     * @param classId
     *            the class id of the COSEM object.
     * @param version
     *            the version of the COSEM object.
     * 
     * @param firstMethodIndex
     *            the method index of the first method in the class. For Assoication SN version = 2, this is 3.
     * @param firstMethodOffset
     *            the offset of the first method to the baseName. This must be a multiple of 0x08. For Assoication SN
     *            version = 2, this is 0x30.
     */
    public SnObjectInfo(int baseName, int classId, int version, int firstMethodIndex, int firstMethodOffset) {
        this(baseName, classId, version, new MethodIdOffsetPair(firstMethodIndex, firstMethodOffset));
    }

    private SnObjectInfo(int baseName, int classId, int version, MethodIdOffsetPair firstMethodIdOffsetPair) {
        this.baseName = baseName;
        this.classId = classId;
        this.version = version;
        this.firstMethodIdOffsetPair = firstMethodIdOffsetPair;
    }

    /**
     * Get the method id, method offset pair.
     * 
     * @return the methodIdOffsetPair.
     */
    public MethodIdOffsetPair getFirstMethodIdOffsetPair() {
        return this.firstMethodIdOffsetPair;
    }

    /**
     * Get the base name (SN address) of a COSEM objec.
     * 
     * @return the base name.
     */
    public int getBaseName() {
        return baseName;
    }

    /**
     * Get the class COSEM ID.
     * 
     * @return the class ID.
     */
    public int getClassId() {
        return this.classId;
    }

    /**
     * Get the COSEM class version.
     * 
     * @return the version.
     */
    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return MessageFormat.format("Base Name: {0}; Class ID: {1}; Version: {2}", this.baseName, this.classId,
                this.version);
    }

    /**
     * Retrieve the LN to SN mapping from the current connection.
     * 
     * @param connection
     *            a DlmsConnection using short naming.
     * @return the LN -&gt; SN mapping.
     * @throws IOException
     *             if the connection is no a short name connection or an error occurs.
     */
    public static Map<ObisCode, SnObjectInfo> retrieveLnSnMappingFrom(DlmsConnection connection) throws IOException {
        if (!(connection instanceof DlmsSnConnection)) {
            throw new IOException("This operation in only available if you're using an SN connection.");
        }

        DlmsSnConnection snConnection = (DlmsSnConnection) connection;

        return snConnection.getLatestObjectInfoMapping();
    }
}
