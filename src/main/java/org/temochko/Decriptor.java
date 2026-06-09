package org.temochko;

import java.util.concurrent.BlockingQueue;

public class Decriptor implements Runnable {

    private final BlockingQueue<byte[]> encryptedMessageQueue;
    private final BlockingQueue<Message> decryptedMessageQueue;
    private volatile boolean isRunning = true;
    private final Decrypter decrypter = new Decrypter();

    public Decriptor(BlockingQueue<byte[]> encryptedMessageQueue, BlockingQueue<Message> decryptedMessageQueue) {
        this.encryptedMessageQueue = encryptedMessageQueue;
        this.decryptedMessageQueue = decryptedMessageQueue;
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                byte[] messageToDecrypt = encryptedMessageQueue.take();
                Message decryptedMessage = decript(messageToDecrypt);
                decryptedMessageQueue.put(decryptedMessage);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Message decript(byte[] message) {
        try {
            return decrypter.decrypt(message);
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }
}
