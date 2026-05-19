package org.temochko;

import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

class EncrypterTest {

    private static final Encrypter SUT = new Encrypter();

    @Test
    void shouldEncryptMessage() {
        Message message = new Message((byte) 0x12, 128, 4, 67, "hello world");

        try {
            System.out.println(Hex.encodeHexString(SUT.encrypt(message)));
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (ShortBufferException e) {
            throw new RuntimeException(e);
        }

        try {
            Assertions.assertEquals("1312000000000000008000000018d52a0000000400000043649685268fcaa35258858bed47f72184124d", Hex.encodeHexString(SUT.encrypt(message)));
        } catch (Exception e) {
            throw new TestAbortedException(e.getMessage());
        }
    }
}