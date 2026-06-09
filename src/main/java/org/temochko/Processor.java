package org.temochko;

import org.temochko.DTOs.ProductCreateDto;
import org.temochko.Models.Product;
import org.temochko.Models.ProductCriteria;
import org.temochko.Services.IProductService;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Processor implements Runnable {
    private final BlockingQueue<Message> decryptedMessageQueue;
    private final BlockingQueue<Message> responseMessageQueue;
    private IProductService productService;

    private volatile boolean isRunning = true;

    public Processor(BlockingQueue<Message> decryptedMessageQueue, BlockingQueue<Message> responseMessageQueue, IProductService productService) {
        this.decryptedMessageQueue = decryptedMessageQueue;
        this.responseMessageQueue = responseMessageQueue;
        this.productService = productService;
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
                            int stockToAdd = Integer.parseInt(messageParts[1]);
                            productService.addStock(idProd, stockToAdd);
                            response = "Added prod stock " + stockToAdd + " successfully";
                        } else {
                            response = "There was an error processing the command";
                        }
                        break;
                    case 2:
                        if (messageParts.length > 1) {
                            int stockToDelete = Integer.parseInt(messageParts[1]);
                            productService.deleteStock(idProd, stockToDelete);
                            response = "Deleted prod stock " + stockToDelete + " successfully";
                        } else {
                            response = "There was an error processing the command";
                        }
                    case 3:
                        List<Product> products = productService.getAllProducts(new ProductCriteria());
                        int countOfProducts = products.size();
                        if (countOfProducts > 0) {
                            response = "Stock " + countOfProducts;
                            break;
                        } else {
                            response = "There was an error";
                        }
                        break;
                    case 4:
                        if (messageParts.length > 1) {
                            String name = messageParts[1];
                            double price = Double.parseDouble(messageParts[2]);
                            int prodCount = Integer.parseInt(messageParts[3]);
                            productService.createProduct(new ProductCreateDto(name, price, prodCount));
                            response = "Added prod successfully";
                        } else {
                            response = "There was an error processing the command";
                        }
                        break;
                    case 5:
                        productService.deleteProductById(idProd);
                        if (!productService.deleteProductById(idProd)) {
                            response = "There was an error. Cannot remove this much product";
                            break;
                        }
                        response = "Removed successfully";
                        break;
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
