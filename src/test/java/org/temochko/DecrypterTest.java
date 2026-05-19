package org.temochko;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

class DecrypterTest {

    private static final Decrypter SUT_DECRYPTER = new Decrypter();
    private static final Encrypter SUT_ENCRYPTER = new Encrypter();

    @Test
    void shouldDecryptMessage() throws DecoderException {
        Message message = new Message((byte) 0x13, 128, 4, 67, "hello world");

        byte[] encryptedPacket = null;
        try {
            encryptedPacket = SUT_ENCRYPTER.encrypt(message);
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

        Message actual = null;
        try {
            actual = SUT_DECRYPTER.decrypt(encryptedPacket);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (ShortBufferException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }

        org.assertj.core.api.Assertions.assertThat(actual)
                .returns((byte) 0x12, Message::getUniqueIdentifier)
                .returns(128L, Message::getMessageNumber)
                .returns(4, Message::getCommandId)
                .returns(67, Message::getUserId)
                .returns("hello world", Message::getMessageString);
    }

    @Test
    void shouldThrowExceptionWhenFirstCrcIsInvalid() throws Exception {
        Message message = new Message((byte) 0x13, 128L, 4, 67, "hello world");
        byte[] packet = SUT_ENCRYPTER.encrypt(message);

        packet[3] = (byte) (packet[3] + 1);

        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> SUT_DECRYPTER.decrypt(packet)
        );

        org.assertj.core.api.Assertions.assertThat(exception.getMessage()).contains("Checksum does not match");
    }

}