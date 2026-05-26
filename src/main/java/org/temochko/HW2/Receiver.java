package org.temochko.HW2;

import org.temochko.HW1.Encrypter;
import org.temochko.HW1.Message;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class Receiver implements Runnable {
    private final BlockingQueue<byte[]> generatedMessageQueue;
    private volatile boolean isRunning = true;
    private final Encrypter encrypter = new Encrypter();
    private int messageCounter = 1;

    public Receiver(BlockingQueue<byte[]> generatedMessageQueue) {
        this.generatedMessageQueue = generatedMessageQueue;
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    public void run() {
        while (isRunning && !Thread.currentThread().isInterrupted()) {
            receiveMessage();
        }
    }

    private void receiveMessage() {
        try {
            Thread.sleep(500);

            int commandId = new Random().nextInt(3);
            int userId = new Random().nextInt(20);
            String messageString = "thisisaresponse";
            Message fakeMessage = new Message((byte) 1, messageCounter++, commandId, userId, messageString);
            byte[] generatedMessageBytes = encrypter.encrypt(fakeMessage);

            generatedMessageQueue.put(generatedMessageBytes);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
