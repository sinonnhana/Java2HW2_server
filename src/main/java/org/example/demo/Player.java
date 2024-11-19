package org.example.demo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Player {
    public Socket socket = null;
    public ObjectInputStream in = null;
    public ObjectOutputStream out = null;
    public String username = null;
    public final Object lock = new Object();

    public Player(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new ObjectOutputStream(socket.getOutputStream());
    }
    public void close() throws IOException {
        socket.close();
        in.close();
        out.close();
    }
}
