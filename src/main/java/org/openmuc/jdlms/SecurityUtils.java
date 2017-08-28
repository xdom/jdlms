package org.openmuc.jdlms;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.RFC3394WrapEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.openmuc.jdlms.SecuritySuite.SecurityPolicy;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DlmsEnumeration;
import org.openmuc.jdlms.interfaceclass.method.SecuritySetupMethod;

/**
 * A static utility class that provides security functions.
 * 
 * <p>
 * This class is useful if you may want to change the security setup of the remote meter.
 * </p>
 */
public class SecurityUtils {

    public enum KeyId implements DlmsEnumeration {
        GLOBAL_UNICAST_ENCRYPTION_KEY(0),
        GLOBAL_BROADCAST_ENCRYPTION_KEY(1),
        AUTHENTICATION_KEY(2);

        private final int id;

        private KeyId(int id) {
            this.id = id;
        }

        public int keyId() {
            return id;
        }

        public String keyName() {
            return name();
        }

        @Override
        public long getCode() {
            return keyId();
        }
    }

    /**
     * Returns the method parameter for updating a single key of a DLMS server.
     * 
     * @param masterKey
     *            the master key, also known as KEK
     * @param newKey
     *            the new key to update to the DLMS server
     * @param keyId
     *            the type of key to update
     * @return return {@linkplain MethodParameter} for global key transfer
     * @throws IOException
     *             throws IOException
     */
    public static MethodParameter keyChangeMethodParamFor(byte[] masterKey, byte[] newKey, KeyId keyId)
            throws IOException {
        final byte instance = 0; // current instance
        ObisCode obisCode = new ObisCode(0, 0, 43, 0, instance, 255);

        final byte[] wrappedKey = wrapAesRFC3394Key(masterKey, newKey);

        List<DataObject> keyDataList = Arrays.asList(DataObject.newEnumerateData(keyId.id),
                DataObject.newOctetStringData(wrappedKey));
        DataObject keyData = DataObject.newStructureData(keyDataList);
        DataObject methodParameter = DataObject.newArrayData(Arrays.asList(keyData));

        return new MethodParameter(SecuritySetupMethod.GLOBALE_KEY_TRANSFER, obisCode, methodParameter);
    }

    /**
     * Sets the security policy method parameter
     * 
     * @param securityPolicy
     *            the security policy to set for
     * @return return MethodParameter for security policy
     */
    public static MethodParameter securityActivateMethodParamFor(SecurityPolicy securityPolicy) {
        final byte instance = 0; // current instance
        ObisCode instanceId = new ObisCode(0, 0, 43, 0, instance, 255);

        return new MethodParameter(SecuritySetupMethod.SECURITY_ACTIVATE, instanceId,
                DataObject.newEnumerateData(securityPolicy.getId()));
    }

    /**
     * Encrypts a byte array with a master key with the algorithm AES in mode CBC and no padding.
     * 
     * @param masterKey
     *            the master key for encryption the bytesToCypher
     * @param bytesToCipher
     *            the bytes to cipher
     * @return the bytesToCipher encrypted
     * 
     * @throws GeneralSecurityException
     *             caused by {@link Cipher#doFinal(byte[])} or {@link Cipher#init(int, Key)}
     */
    public static byte[] cipherWithAes128(byte[] masterKey, byte[] bytesToCipher) throws GeneralSecurityException {
        Key secretkeySpec = new SecretKeySpec(masterKey, "AES");
        Cipher cipher;

        try {
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            // should not occur
            throw new RuntimeException(e);
        }

        cipher.init(Cipher.ENCRYPT_MODE, secretkeySpec);

        return cipher.doFinal(bytesToCipher);
    }

    /**
     * This function wraps a key with a kek (key encryption key)
     * 
     * @param kek
     *            the key encryption key for wrapping the key
     * @param key
     *            the key to wrap
     * @return returns a with kek wrapped key
     */
    public static byte[] wrapAesRFC3394Key(byte[] kek, byte[] key) {
        RFC3394WrapEngine rfc3394WrapEngine = new RFC3394WrapEngine(new AESEngine());
        rfc3394WrapEngine.init(true, new KeyParameter(kek));

        return rfc3394WrapEngine.wrap(key, 0, key.length);
    }

    /**
     * This function unwraps a wrapped key with the kek (key encryption key)
     * 
     * @param kek
     *            the key encryption key for unwrapping the wrapped key
     * @param wrappedKey
     *            the wrapped key to unwrap
     * @return returns a unwrapped key
     * @throws InvalidCipherTextException
     *             will thrown if something unexpected is in the wrappedKey
     */
    public static byte[] unwrapAesRFC3394Key(byte[] kek, byte[] wrappedKey) throws InvalidCipherTextException {
        RFC3394WrapEngine rfc3394WrapEngine = new RFC3394WrapEngine(new AESEngine());
        rfc3394WrapEngine.init(false, new KeyParameter(kek));

        return rfc3394WrapEngine.unwrap(wrappedKey, 0, wrappedKey.length);
    }

    /**
     * Generates a random AES 128 key
     * 
     * @return returns a random AES 128 key
     */
    public static byte[] generateAES128Key() {
        byte[] key = new byte[16];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(key);

        return key;
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private SecurityUtils() {
    }

}
