package org.temochko;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Decrypter {

    private final static String ALGORITHM = "AES";
    private final static String ENCRYPTION_KEY_STRING =  "thisisa128bitkey";

    public Decrypter() {

    }

    public Message decrypt(byte[] messageToDecrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        ByteBuffer buffer = ByteBuffer.wrap(messageToDecrypt);

        // first bytes before the message
        byte magicByte = buffer.get();
        byte uniqueIdentifierByte = buffer.get();
        long messageNumber = buffer.getLong();
        int wlen = buffer.getInt();
        short firstCrc = buffer.getShort();

        // 1st crc
        short checksum = Crc16.calculateCrc(messageToDecrypt, 0, 14);
        validateChecksum(checksum, firstCrc);

        // other 8 bytes of message
        int commandId = buffer.getInt();
        int userId = buffer.getInt();

        // length of encrepted message
        int encryptedPayloadLength = wlen - 8;
        byte[] messageToBeDecrypted = new byte[encryptedPayloadLength];

        // get encrypted message from buffer
        buffer.get(messageToBeDecrypted);

        // decipher
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKey key = new SecretKeySpec(ENCRYPTION_KEY_STRING.getBytes(), ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(messageToBeDecrypted);
        String decryptedString = new String(decryptedBytes);

        // 2nd crc
        short secondCrc = buffer.getShort();
        short checksum2 = Crc16.calculateCrc(messageToDecrypt, 16, wlen);
        validateChecksum(checksum2, secondCrc);

        return new Message(uniqueIdentifierByte, messageNumber, commandId, userId, decryptedString);
    }

    private void validateChecksum(short expectedChecksum, short actualChecksum) {
        if (actualChecksum != expectedChecksum) {
            throw new IllegalArgumentException("Checksum does not match");
        }
    }

}
