package org.temochko.HW2and3;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Receiver implements Runnable {
    private final Socket clientSocket;
    private final ConcurrentHashMap<Integer, Receiver> activeClients;

    private SocketWrapper socketWrapper;
    private int userId;
    private volatile boolean isRunning = true;

    public Receiver(Socket socket, BlockingQueue<byte[]> encryptedQueue, ConcurrentHashMap<Integer, Receiver> activeClients) {
        this.clientSocket = socket;
        this.activeClients = activeClients;

        try {
            this.socketWrapper = new SocketWrapper(socket, encryptedQueue);
        } catch (Exception e) {
            System.err.println("Failed to create SocketWrapper");
            isRunning = false;
        }
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    public void run() {
        if (!isRunning) return;

        try {
            this.userId = socketWrapper.readHandshake();
            activeClients.put(userId, this);
            System.out.println("User " + userId + " authenticated. Receiver is listening.");

            // 2. THE MESSAGE LOOP
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                receiveMessage();
            }

        } catch (Exception e) {
            System.err.println("Connection lost for User " + userId);
        } finally {
            cleanUp();
        }
    }

    private void receiveMessage() {
        socketWrapper.read();
    }

    public void sendResponse(byte[] encryptedResponse) {
        if (socketWrapper != null) {
            socketWrapper.send(encryptedResponse);
        }
    }

    private void cleanUp() {
        if (userId != 0) {
            activeClients.remove(userId);
        }
        if (socketWrapper != null) {
            socketWrapper.close();
        }
    }
}