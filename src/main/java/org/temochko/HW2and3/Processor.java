package org.temochko.HW2and3;

import org.temochko.HW1.Message;

import java.util.concurrent.BlockingQueue;

public class Processor implements Runnable {
    private final BlockingQueue<Message> decryptedMessageQueue;
    private final BlockingQueue<Message> responseMessageQueue;
    private Storage storage;

    private volatile boolean isRunning = true;

    public Processor(BlockingQueue<Message> decryptedMessageQueue, BlockingQueue<Message> responseMessageQueue, Storage storage) {
        this.decryptedMessageQueue = decryptedMessageQueue;
        this.responseMessageQueue = responseMessageQueue;
        this.storage = storage;
    }

    public void stop() {
        isRunning = false;
    }
    @Override
    public void run() {
        while (isRunning && !Thread.currentThread().isInterrupted()) {
            try {
                Message decryptedMessage = decryptedMessageQueue.take();

                processMessage(decryptedMessage);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processMessage(Message message) {
        try {
            int commandId = message.getCommandId();
            String messageString = message.getMessageString();
            String response = "";

            try {
                String[] messageParts = messageString.split("/");
                int idProd = Integer.parseInt(messageParts[0]);

                switch (commandId) {
                    case 1:
                        if (messageParts.length > 1) {
                            int prodCount = Integer.parseInt(messageParts[1]);
                            storage.addStock(idProd, prodCount);
                            response = "Added prod successfully";
                        } else {
                            response = "There was an error processing the command";
                        }
                        break;
                    case 2:
                        if (messageParts.length > 1) {
                            int prodCount = Integer.parseInt(messageParts[1]);
                            var prodCountInStorage = storage.getStock(idProd);

                            if (prodCountInStorage - prodCount < 0) {
                                response = "There was an error. Cannot remove this much product";
                                break;
                            }
                            storage.removeStock(idProd, prodCount);
                            response = "Removed successfully";
                            break;
                        }
                    case 3:
                        int countOfProduct = storage.getStock(idProd);
                        if (countOfProduct > 0) {
                            response = "Stock " + countOfProduct;
                            break;
                        } else {
                            response = "There was an error";
                        }
                    default:
                        break;
                }
            } catch (NumberFormatException e) {
                System.err.println(e.getMessage());
            }

            Message responseMessage = new Message(
                    message.getUniqueIdentifier(), message.getMessageNumber(),
                    message.getCommandId(), message.getUserId(), response
            );

            responseMessageQueue.put(responseMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
