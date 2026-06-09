package org.temochko.Network;

import org.temochko.*;
import org.temochko.Repositories.IProductRepository;
import org.temochko.Repositories.SqlLiteProductRepository;
import org.temochko.Services.IProductService;
import org.temochko.Services.ProductService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class StoreServerTCP {
    static final int PORT = 8080;

    private final BlockingQueue<byte[]> encryptedQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Message> decryptedQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Message> responseQueue = new LinkedBlockingQueue<>();

    // id to connection
    private final ConcurrentHashMap<Integer, Receiver> activeClients = new ConcurrentHashMap<>();

    private final IProductRepository productRepository = new SqlLiteProductRepository("products.db");
    private final IProductService service = new ProductService(productRepository);

    public void start() {
        new Thread(new Decriptor(encryptedQueue, decryptedQueue), "DecriptorThread").start();
        new Thread(new Processor(decryptedQueue, responseQueue, service), "ProcessorThread").start();

        new Thread(new Sender(responseQueue, activeClients), "RouterThread").start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("TCP Server listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection made");

                Receiver receiver = new Receiver(clientSocket, encryptedQueue, activeClients);
                new Thread(receiver).start();
            }
        } catch (IOException e) {
            System.err.println("Server socket failure: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        StoreServerTCP server = new StoreServerTCP();
        server.start();
    }
}
