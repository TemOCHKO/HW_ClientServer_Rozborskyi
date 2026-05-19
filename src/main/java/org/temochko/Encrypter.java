package org.temochko;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Encrypter {

    private final static String ALGORITHM = "AES";
    private final static String ENCRYPTION_KEY_STRING =  "thisisa128bitkey";

    public Encrypter() {

    }

    public byte[] encrypt(Message message) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, ShortBufferException {
        // ciphering the message
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKey key = new SecretKeySpec(ENCRYPTION_KEY_STRING.getBytes(), ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedMessage = cipher.doFinal(message.getMessageString().getBytes());

        int wlen = 8 + encryptedMessage.length;

        ByteBuffer buffer = ByteBuffer.allocate(16 + wlen + 2);
        buffer.put((byte) 0x13);
        buffer.put((byte) message.getUniqueIdentifier());
        buffer.putLong(message.getMessageNumber());
        buffer.putInt(wlen);

        // 1st Crc
        short firstCrc = Crc16.calculateCrc(buffer.array(), 0, 14);
        buffer.putShort(firstCrc);

        // 2nd table
        buffer.putInt(message.getCommandId());
        buffer.putInt(message.getUserId());

        // put encrypted message in buffer
        buffer.put(encryptedMessage);

        // 2nd Crc
        short secondCrc = Crc16.calculateCrc(buffer.array(), 16, wlen);
        buffer.putShort(secondCrc);

        return buffer.array();
    }
}
