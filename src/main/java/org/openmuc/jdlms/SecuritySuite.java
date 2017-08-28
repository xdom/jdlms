package org.openmuc.jdlms;

import java.util.Map;

import org.openmuc.jdlms.datatypes.DlmsEnumeration;
import org.openmuc.jdlms.internal.DlmsEnumFunctions;

/**
 * This Suite hold the necessary information to authenticate a client to a server and to encrpyt and authenticity
 */
public class SecuritySuite {

    byte[] globalUnicastEncryptionKey;
    byte[] authenticationKey;
    byte[] password;

    private final EncryptionMechanism encryptionMechanism;
    private final AuthenticationMechanism authenticationMechanism;
    private final SecurityPolicy securityPolicy;

    SecuritySuite(byte[] globalUnicastEncryptionKey, byte[] authenticationKey, byte[] password,
            EncryptionMechanism cryptographicAlgorithm, AuthenticationMechanism authenticationLevel,
            SecurityPolicy securityPolicy) {
        this.globalUnicastEncryptionKey = globalUnicastEncryptionKey;
        this.authenticationKey = authenticationKey;
        this.password = password;
        this.encryptionMechanism = cryptographicAlgorithm;
        this.authenticationMechanism = authenticationLevel;
        this.securityPolicy = securityPolicy;
    }

    /**
     * Get the global unicast encryption key.
     * 
     * @return the global unicast encryption key.
     */
    public byte[] getGlobalUnicastEncryptionKey() {
        return globalUnicastEncryptionKey;
    }

    /**
     * Get the authentication key.
     * 
     * @return the authentication key.
     */
    public byte[] getAuthenticationKey() {
        return authenticationKey;
    }

    /**
     * Get the password.
     * <p>
     * This value is <code>null</code> when an authentication level other than LOW is used
     * </p>
     * 
     * @return the password.
     */
    public byte[] getPassword() {
        return password;
    }

    /**
     * Get the encryption mechanism which is used to encrypt the transported data.
     * 
     * @return the encryption mechanism .
     */
    public EncryptionMechanism getEncryptionMechanism() {
        return encryptionMechanism;
    }

    /**
     * Get the authentication mechanism.
     * 
     * @return the authentication mechanism.
     */
    public AuthenticationMechanism getAuthenticationMechanism() {
        return authenticationMechanism;
    }

    /**
     * Get the get security policy.
     * 
     * @return the security policy.
     */
    public SecurityPolicy getSecurityPolicy() {
        return this.securityPolicy;
    }

    /**
     * Creates a new Security Suite builder. With a default config of no authentication and no encryption.
     * 
     * @return the new builder.
     */
    public static SecuritySuiteBuilder builder() {
        return new SecuritySuiteBuilder();
    }

    public static class SecuritySuiteBuilder {

        private AuthenticationMechanism authenticationMechanism;
        private EncryptionMechanism encryptionMechanism;
        private byte[] globalUnicastEncryptionKey;
        private byte[] authenticationKey;
        private byte[] password;
        private SecurityPolicy securityPolicy;

        private SecuritySuiteBuilder() {
            this.authenticationKey = null;
            this.globalUnicastEncryptionKey = null;
            this.password = null;
            this.authenticationMechanism = AuthenticationMechanism.NONE;
            this.encryptionMechanism = EncryptionMechanism.NONE;
        }

        /**
         * Set the security policy for the connection. If this is not set the strongest possible policy is chosen.
         * 
         * @param securityPolicy
         *            the security policy for the connection
         * @return the instance of the builder.
         */
        public SecuritySuiteBuilder setSecurityPolicy(SecurityPolicy securityPolicy) {
            this.securityPolicy = securityPolicy;
            return this;
        }

        /**
         * Set the cryptographic algorithm.
         * 
         * <p>
         * Default value is NONE.
         * </p>
         * 
         * @param encryptionMechanism
         *            the encryption algorithm
         * @return the instance of the builder.
         */
        public SecuritySuiteBuilder setEncryptionMechanism(EncryptionMechanism encryptionMechanism) {
            if (encryptionMechanism == null) {
                throw new IllegalArgumentException("The cryptographic algorithm is not allowed to be null!");
            }
            this.encryptionMechanism = encryptionMechanism;
            return this;
        }

        /**
         * Set the authentication level, a client authenticates to a server.
         * 
         * <p>
         * <b>NOTE:</b> only {@linkplain AuthenticationMechanism#NONE}, {@linkplain AuthenticationMechanism#LOW} and
         * {@linkplain AuthenticationMechanism#HLS5_GMAC} are supported at the moment. <br>
         * If {@linkplain AuthenticationMechanism#LOW} is set, the password must be set.<br>
         * If {@linkplain AuthenticationMechanism#HLS5_GMAC} is set, the authentication key and the encryption key must
         * be set.
         * </p>
         * 
         * <p>
         * Default value is NONE.
         * </p>
         * 
         * @param authenticationMechanism
         *            the authentication mechanism.
         * @return the instance of the builder.
         */
        public SecuritySuiteBuilder setAuthenticationMechanism(AuthenticationMechanism authenticationMechanism) {
            if (authenticationMechanism == null) {
                throw new IllegalArgumentException("The authentication mechanism is not allowed to be null!");
            }
            this.authenticationMechanism = authenticationMechanism;
            return this;
        }

        /**
         * Set the global encryption key.
         * 
         * <table border="1" summary="algo table">
         * <tr>
         * <th>Cryptographic Algorithm</th>
         * <th>Key length (bits)</th>
         * </tr>
         * <tr>
         * <td>{@linkplain EncryptionMechanism#AES_GMC_128}</td>
         * <td>128</td>
         * </tr>
         * </table>
         * 
         * @param key
         *            the global unicast encryption key.
         * @return the instance of the builder.
         */
        public SecuritySuiteBuilder setGlobalUnicastEncryptionKey(byte[] key) {
            this.globalUnicastEncryptionKey = key;
            return this;
        }

        /**
         * Set the password for the authentication level {@linkplain AuthenticationMechanism#LOW}.
         * 
         * <p>
         * <b>NOTE:</b> Sets the authentication level to low.
         * </p>
         * 
         * @param password
         *            the password as byte array.
         * @return the instance of the builder.
         */
        public SecuritySuiteBuilder setPassword(byte[] password) {
            this.password = password;
            return this;
        }

        /**
         * Set the authentication key used mainly for authentication.
         * 
         * <p>
         * The key must have the correct length for authentication level.
         * </p>
         * 
         * @param key
         *            the authentication key.
         * @return the instance of the builder.
         */
        public SecuritySuiteBuilder setAuthenticationKey(byte[] key) {
            this.authenticationKey = key;
            return this;
        }

        /**
         * Build a security setup.
         * 
         * @return a new security setup.
         */
        public SecuritySuite build() {
            if (this.securityPolicy == null) {
                if (this.authenticationMechanism.isHlsMechanism()) {
                    if (this.encryptionMechanism != EncryptionMechanism.NONE) {
                        this.securityPolicy = SecurityPolicy.AUTHENTICATED_AND_ENCRYPTED;
                    }
                    else {
                        this.securityPolicy = SecurityPolicy.AUTHENTICATED;
                    }
                }
                else if (this.encryptionMechanism != EncryptionMechanism.NONE) {
                    this.securityPolicy = SecurityPolicy.ENCRYPTED;
                }
                else {
                    this.securityPolicy = SecurityPolicy.NOTHING;
                }
            }
            validateFields();
            return new SecuritySuiteImpl(saveArrayClone(globalUnicastEncryptionKey), saveArrayClone(authenticationKey),
                    saveArrayClone(password), encryptionMechanism, authenticationMechanism, this.securityPolicy);
        }

        private void validateFields() throws IllegalArgumentException, UnsupportedOperationException {

            validateSecurityPolicy();
            this.encryptionMechanism.checkKeyLength(this.globalUnicastEncryptionKey, this.authenticationKey);
            switch (this.authenticationMechanism) {
            case HLS5_GMAC:
                if ((this.authenticationKey == null || this.globalUnicastEncryptionKey == null)
                        || this.globalUnicastEncryptionKey.length != this.authenticationKey.length) {
                    throw new IllegalArgumentException(
                            "Authentication/Encryption key either not supplied or don't match in length.");
                }
                break;
            case LOW:
                if (this.password == null) {
                    throw new IllegalArgumentException("Password is not set for the security level low.");
                }
                break;
            case NONE:
            default:
                break;
            }
        }

        private void validateSecurityPolicy() {
            if ((securityPolicy == SecurityPolicy.ENCRYPTED
                    || securityPolicy == SecurityPolicy.AUTHENTICATED_AND_ENCRYPTED)
                    && encryptionMechanism == EncryptionMechanism.NONE) {
                throw new IllegalArgumentException("Select a cryptographical algorithm to encrypt messages.");
            }
            else if ((securityPolicy == SecurityPolicy.AUTHENTICATED
                    || securityPolicy == SecurityPolicy.AUTHENTICATED_AND_ENCRYPTED)
                    && !authenticationMechanism.isHlsMechanism()) {
                throw new IllegalArgumentException("Select a HLS authentication, to authenticate messages.");
            }
        }

    }

    private static byte[] saveArrayClone(byte[] data) {
        return data != null ? data.clone() : null;
    }

    public static SecuritySuite newSecuritySuiteFrom(SecuritySuite securitySuite) {
        return new SecuritySuiteImpl(saveArrayClone(securitySuite.globalUnicastEncryptionKey),
                saveArrayClone(securitySuite.authenticationKey), saveArrayClone(securitySuite.password),
                securitySuite.encryptionMechanism, securitySuite.authenticationMechanism, securitySuite.securityPolicy);
    }

    /**
     * The security policy.
     */
    public enum SecurityPolicy {
        /**
         * No encryption and authentication.
         */
        NOTHING(0),
        /**
         * All messages to be authenticated.
         */
        AUTHENTICATED(1),
        /**
         * All messages to be encrypted.
         */
        ENCRYPTED(2),
        /**
         * All messaged to be authenticated and encrypted.
         */
        AUTHENTICATED_AND_ENCRYPTED(3);

        private final int id;

        private SecurityPolicy(int id) {
            this.id = id;
        }

        public String keyName() {
            return name();
        }

        public boolean isAuthenticated() {
            return this == SecurityPolicy.AUTHENTICATED || this == AUTHENTICATED_AND_ENCRYPTED;
        }

        public boolean isEncrypted() {
            return this == SecurityPolicy.ENCRYPTED || this == AUTHENTICATED_AND_ENCRYPTED;
        }

        public int getId() {
            return this.id;
        }
    }

    /**
     * Cryptographic algorithm identify the algorithm for which a derived secret symmetrical key will be used.
     */
    public enum EncryptionMechanism implements DlmsEnumeration {
        /**
         * Do not encrypt transport.
         */
        NONE(-1, -1),

        /**
         * Use AES-128.
         */
        AES_GMC_128(0, 128);

        private final int id;
        private final int keyLength;

        private static final Map<Long, EncryptionMechanism> idMap;

        static {
            idMap = DlmsEnumFunctions.generateEnumMap(EncryptionMechanism.class);
        }

        private EncryptionMechanism(int id, int keyLength) {
            this.id = id;
            this.keyLength = keyLength;
        }

        private void checkKeyLength(byte[] encryptionKey, byte[] authenticationKey) {
            if (this == NONE) {
                return;
            }

            if (encryptionKey == null) {
                throw new IllegalArgumentException("The key is not allowed to be null.");
            }

            if (authenticationKey != null && authenticationKey.length != encryptionKey.length) {
                throw new IllegalArgumentException("Authentication key length does not match encryption key length.");
            }

            if (encryptionKey.length * 8 != this.keyLength) {
                throw new IllegalArgumentException("The key has an invalid length.");
            }
        }

        @Override
        public long getCode() {
            return this.id;
        }

        /**
         * Returns the EncryptionMechanism that corresponds to the given ID. Throws an IllegalArgumentException if no
         * EncryptionMechanism with the given ID exists.
         * 
         * @param id
         *            the ID
         * @return the EncryptionMechanism that corresponds to the given ID
         */
        public static EncryptionMechanism getInstance(long id) {
            EncryptionMechanism enumInstance = idMap.get(id);
            if (enumInstance == null) {
                throw new IllegalArgumentException("invalid ID: " + id);
            }
            return enumInstance;
        }

    }

}
