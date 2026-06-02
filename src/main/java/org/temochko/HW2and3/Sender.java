package org.temochko.HW2and3;

import org.temochko.HW1.Encrypter;
import org.temochko.HW1.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Sender implements Runnable {
    private final BlockingQueue<Message> responseQueue;
    private final ConcurrentHashMap<Integer, Receiver> activeClients;
    private final Encrypter encrypter = new Encrypter();
    private volatile boolean isRunning = true;

    public Sender(BlockingQueue<Message> responseQueue, ConcurrentHashMap<Integer, Receiver> activeClients) {
        this.responseQueue = responseQueue;
        this.activeClients = activeClients;
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    public void run() {
        while (isRunning && !Thread.currentThread().isInterrupted()) {
            try {
                Message responseMsg = responseQueue.take();
                byte[] encryptedBytes = encrypter.encrypt(responseMsg);
                sendMessage(encryptedBytes, responseMsg.getUserId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("Routing error: " + e.getMessage());
            }
        }
    }

    void sendMessage(byte[] message, int targetUserId) {
        try {
            Receiver clientReceiver = activeClients.get(targetUserId);

            if (clientReceiver != null) {
                clientReceiver.sendResponse(message);

                System.out.println("Sent a byte message with length " + message.length + " to User " + targetUserId);
            } else {
                System.err.println("User " + targetUserId + " disconnected before receiving response.");
            }
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
        }
    }
}