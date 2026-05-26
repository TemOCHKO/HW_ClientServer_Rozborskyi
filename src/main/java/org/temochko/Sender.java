package org.temochko;

import java.util.concurrent.BlockingQueue;

public class Sender implements Runnable {
    private final BlockingQueue<byte[]> generatedMessageQueue;
    private volatile boolean isRunning = true;

    public Sender(BlockingQueue<byte[]> generatedMessageQueue) {
        this.generatedMessageQueue = generatedMessageQueue;
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    public void run() {
        while (isRunning && !Thread.currentThread().isInterrupted()) {
            try {
                byte[] generatedMessage = generatedMessageQueue.take();

                sendMessage(generatedMessage);
            }
            catch (InterruptedException iex) {
                Thread.currentThread().interrupt();
            }
            catch (Exception e) {
                System.err.println(e);
            }

        }
    }

    void sendMessage(byte[] message) {
        System.out.println("Sent a byte message with length " + message.length);
    }
}
