package com.pictionary.client;

import com.pictionary.gui.GameScreenController;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Client extends Thread {

    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private ObjectOutputStream serverOut;
    private ObjectInputStream serverIn;
    private GameScreenController gsc;

    public void setGameScreenController(GameScreenController gsc) {
        this.gsc = gsc;
    }

    public ObjectInputStream getServerIn() {
        return serverIn;
    }
    private volatile boolean isConnected = true;
    private String message;

    public Client(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            this.socket = new Socket(serverName, serverPort);
            this.serverOut = new ObjectOutputStream(socket.getOutputStream());
            this.serverIn = new ObjectInputStream(socket.getInputStream());
            while (isConnected && (message = serverIn.readObject().toString()) != null) {
                gsc.sendMessage(message);
            }

        } catch (IOException | ClassNotFoundException | InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendMsg(String msg) throws IOException {
        serverOut.writeObject(msg);
        serverOut.flush();
    }

    public void closeConnection() {
        try {
            sendMsg("stop");
            isConnected = false;
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
