package org.temochko.Network;

import org.temochko.Message;

import java.util.Random;

public class TCPClientMain {
    private

    public static void main(String[] args) {
        int numberOfSimulatedUsers = 15;

        System.out.println("Starting sim");

        for (int i = 1; i <= numberOfSimulatedUsers; i++) {
            final int userId = i;

            new Thread(() -> {
                try {
                    StoreClientTCP client = new StoreClientTCP("127.0.0.1", 8080, userId);
                    int choice = new Random().nextInt(1, 4);
                    String messageString = "";
                    switch (choice) {
                        case 1:
                            messageString = "2/5";
                            break;
                        case 2:
                            messageString = "2/5";
                            break;
                        default:
                            messageString = "2";
                            break;
                    }

                    Message request = new Message(
                            (byte) 1,
                            System.currentTimeMillis(),
                            choice,
                            userId,
                            messageString
                    );

                    Message response = client.sendRequest(request);

                    if (response != null) {
                        System.out.println("SUCCESS (User " + userId + "): " + response.getMessageString());
                    }
                } catch (Exception e) {
                    System.err.println("ERROR (User " + userId + "): " + e.getMessage());
                }
            }, "Thread-" + i).start();
        }
    }
}