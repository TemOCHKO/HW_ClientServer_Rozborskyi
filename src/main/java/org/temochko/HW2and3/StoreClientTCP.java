package org.temochko.HW2and3;

import org.temochko.HW1.Decrypter;
import org.temochko.HW1.Encrypter;
import org.temochko.HW1.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class StoreClientTCP {
    private final String host;
    private final int port;
    private final int myUserId;
    private Socket socket;
    private OutputStream out;
    private InputStream in;

    private final Encrypter encrypter = new Encrypter();
    private final Decrypter decrypter = new Decrypter();

    public StoreClientTCP(String host, int port, int userId) {
        this.host = host;
        this.port = port;
        this.myUserId = userId;
    }

    public Message sendRequest(Message requestMessage) {
        while (true) {
            try {
                connectIfNeeded();

                byte[] encryptedData = encrypter.encrypt(requestMessage);

                out.write(encryptedData);
                out.flush();
                System.out.println("Sent encrypted message of " + encryptedData.length + " bytes.");

                byte[] buffer = new byte[2048];
                int bytesRead = in.read(buffer);

                if (bytesRead == -1) {
                    throw new IOException("Server closed the connection gracefully.");
                }

                byte[] receivedData = new byte[bytesRead];
                System.arraycopy(buffer, 0, receivedData, 0, bytesRead);

                return decrypter.decrypt(receivedData);

            } catch (Exception e) {
                System.err.println("Connection lost or server unavailable. Retrying in 3 seconds...");
                closeConnection();

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
    }

    private void connectIfNeeded() throws IOException {
        if (socket == null || socket.isClosed()) {
            System.out.println("Attempting to connect to server at " + host + ":" + port + "...");
            socket = new Socket(host, port);
            out = socket.getOutputStream();
            in = socket.getInputStream();

            // send the handsjake id
            DataOutputStream dataOut = new DataOutputStream(out);
            dataOut.writeInt(myUserId);
            dataOut.flush();

            System.out.println("Connected " + myUserId);
        }
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        } finally {
            socket = null;
            out = null;
            in = null;
        }
    }
}