package org.temochko;

import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.nio.ByteBuffer;
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

    @Test
    void shouldConstructValidPacket() throws Exception {
        Message original = new Message((byte) 0x20, 128L, 4, 67, "hello world");
        byte[] encryptedPacket = SUT.encrypt(original);

        ByteBuffer buffer = ByteBuffer.wrap(encryptedPacket);

        // magic byte
        org.assertj.core.api.Assertions.assertThat(buffer.get()).isEqualTo((byte) 0x13);
        // unique id
        org.assertj.core.api.Assertions.assertThat(buffer.get()).isEqualTo((byte) 0x20);
        // message num
        org.assertj.core.api.Assertions.assertThat(buffer.getLong()).isEqualTo(128L);

        int wlen = buffer.getInt();
        org.assertj.core.api.Assertions.assertThat(wlen).isGreaterThan(8);

        buffer.getShort();

        org.assertj.core.api.Assertions.assertThat(buffer.getInt()).isEqualTo(4);
        org.assertj.core.api.Assertions.assertThat(buffer.getInt()).isEqualTo(67);
    }

    @Test
    void shouldHandleBigMessageText() throws Exception {
        String largeText = "hello".repeat(50000);
        Message original = new Message((byte) 0x05, 50L, 2, 3, largeText);

        byte[] encryptedPacket = SUT.encrypt(original);

        org.assertj.core.api.Assertions.assertThat(encryptedPacket.length).isGreaterThan(10000);
    }
}