package org.temochko.HW2and3;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class SocketWrapper {

    private final Socket socket;
    private final BlockingQueue<byte[]> outQueue;
    private final InputStream input;
    private final OutputStream output;

    public SocketWrapper(Socket socket, BlockingQueue<byte[]> outQueue) throws IOException {
        this.socket = socket;
        this.outQueue = outQueue;
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
    }

    public int readHandshake() throws IOException {
        DataInputStream dataIn = new DataInputStream(input);
        return dataIn.readInt();
    }

    public void send(byte[] data) {
        try {
            output.write(data);
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void read() {
        try {
            byte[] buffer = new byte[2048];
            int bytesRead = input.read(buffer);

            if (bytesRead == -1) {
                throw new IOException("Client disconnected");
            }

            byte[] data = new byte[bytesRead];
            System.arraycopy(buffer, 0, data, 0, bytesRead);

            outQueue.put(data);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }
}