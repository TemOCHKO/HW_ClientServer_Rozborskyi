package org.temochko;

import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.Test;
import org.temochko.HW1.Decrypter;
import org.temochko.HW1.Encrypter;
import org.temochko.HW1.Message;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void shouldThrowExceptionWhenFirstCrcIsNotValid() throws Exception {
        Message message = new Message((byte) 0x13, 128L, 4, 67, "hello world");
        byte[] packet = SUT_ENCRYPTER.encrypt(message);

        packet[3] = (byte) (packet[3] + 1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SUT_DECRYPTER.decrypt(packet)
        );

        org.assertj.core.api.Assertions.assertThat(exception.getMessage()).contains("Checksum does not match");
    }

    @Test
    void shouldHandleEmptyMessageString() throws Exception {
        Message original = new Message((byte) 0x01, 1L, 100, 200, "");

        byte[] encryptedPacket = SUT_ENCRYPTER.encrypt(original);
        Message decrypted = SUT_DECRYPTER.decrypt(encryptedPacket);

        org.assertj.core.api.Assertions.assertThat(decrypted.getMessageString()).isEmpty();
    }

    @Test
    void shouldFailWhenPayloadIsChanged() throws Exception {
        Message original = new Message((byte) 0x12, 128L, 4, 67, "hello, worrld!");
        byte[] tamperedPacket = SUT_ENCRYPTER.encrypt(original);

        tamperedPacket[25] = (byte) (tamperedPacket[25] ^ 0xFF);

        Exception exception = assertThrows(
                Exception.class,
                () -> SUT_DECRYPTER.decrypt(tamperedPacket)
        );

        org.assertj.core.api.Assertions.assertThat(exception).isInstanceOfAny(
                BadPaddingException.class,
                IllegalArgumentException.class
        );
    }
}