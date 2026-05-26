package org.temochko.HW2;

import org.temochko.HW1.Encrypter;
import org.temochko.HW1.Message;

import java.util.concurrent.BlockingQueue;

public class Encriptor implements Runnable {
    private final BlockingQueue<Message> messagesToEncryptQueue;
    private final BlockingQueue<byte[]> encryptedMessageQueue;
    private volatile boolean isRunning = true;
    private final Encrypter encrypter = new Encrypter();

    public Encriptor(BlockingQueue<Message> messagesToEncryptQueue, BlockingQueue<byte[]> encryptedMessageQueue) {
        this.messagesToEncryptQueue = messagesToEncryptQueue;
        this.encryptedMessageQueue = encryptedMessageQueue;
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                Message messageToEncrypt = messagesToEncryptQueue.take();
                var encryptedMessage = encrypt(messageToEncrypt);
                encryptedMessageQueue.put(encryptedMessage);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    byte[] encrypt(Message message) {
        try {
            return encrypter.encrypt(message);
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }
}