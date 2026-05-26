package org.temochko.HW2;

import org.temochko.HW1.Message;

import java.util.concurrent.BlockingQueue;

public class Processor implements Runnable {
    private final BlockingQueue<Message> decryptedMessageQueue;
    private final BlockingQueue<Message> responseMessageQueue;

    private volatile boolean isRunning = true;

    public Processor(BlockingQueue<Message> decryptedMessageQueue, BlockingQueue<Message> responseMessageQueue) {
        this.decryptedMessageQueue = decryptedMessageQueue;
        this.responseMessageQueue = responseMessageQueue;
    }

    public void stop() {
        isRunning = false;
    }
    @Override
    public void run() {
        while (isRunning) {
            try {
                Message decryptedMessage = decryptedMessageQueue.take();

                processMessage(decryptedMessage);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void processMessage(Message message) {
        try {
            Message responseMessage = new Message(
                    message.getUniqueIdentifier(), message.getMessageNumber(),
                    message.getCommandId(), message.getUserId(), "Ok"
            );

            responseMessageQueue.put(message);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
