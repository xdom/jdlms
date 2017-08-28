package org.openmuc.jdlms;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.junit.Test;

public class SecurityUtilsTest {

    private static final byte[] KEY;
    private static final byte[] MASTER_KEY;
    private static final byte[] WRAPPED_KEY_TEST;

    static {
        final String masterKeyString = "000102030405060708090a0b0c0d0e0f";
        final String keyString = "00112233445566778899aabbccddeeff";
        final String wrappedKeyExpectedString = "1fa68b0a8112b447aef34bd8fb5a7b829d3e862371d2cfe5";

        KEY = HexConverter.fromShortHexString(keyString);
        MASTER_KEY = HexConverter.fromShortHexString(masterKeyString);
        WRAPPED_KEY_TEST = HexConverter.fromShortHexString(wrappedKeyExpectedString);
    }

    @Test
    public void aesRFC3394KeyWrapTest() throws Exception {
        byte[] warppedKey = SecurityUtils.wrapAesRFC3394Key(MASTER_KEY, KEY);
        assertArrayEquals(WRAPPED_KEY_TEST, warppedKey);
    }

    @Test
    public void aesRFC3394KeyWrapTestSymmetry() throws Exception {
        byte[] wrappedKey = SecurityUtils.wrapAesRFC3394Key(MASTER_KEY, KEY);
        assertArrayEquals(WRAPPED_KEY_TEST, wrappedKey);

        byte[] unwrapKey = SecurityUtils.unwrapAesRFC3394Key(MASTER_KEY, wrappedKey);

        assertArrayEquals(KEY, unwrapKey);
    }

    @Test
    public void aesRFC3394KeyUnwrapTest() throws Exception {
        try {
            byte[] unwrappedKey = SecurityUtils.unwrapAesRFC3394Key(MASTER_KEY, WRAPPED_KEY_TEST);
            assertArrayEquals(KEY, unwrappedKey);

        } catch (InvalidCipherTextException e) {
            fail(e.getMessage());
        }
    }

}
