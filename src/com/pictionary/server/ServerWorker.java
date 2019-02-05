package com.pictionary.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerWorker extends Thread {

    private List<ServerWorker> workerList;

    private final Socket clientSocket;
    private final Server server;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private boolean isReady = false;
    private int points = 0;
    private volatile boolean isConnected = true;

    public void setIsReady(boolean isReady) {
        this.isReady = isReady;
    }

    public ServerWorker(Server server, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    public boolean getIsReady() {
        return isReady;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public void run() {
        try {
            this.inputStream = new ObjectInputStream(clientSocket.getInputStream());
            this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            String message;

            workerList = server.getWorkerList();

            while ((message = inputStream.readObject().toString()) != null && isConnected) {
                String[] tokens = message.split("//");
                if ("name".equals(tokens[0]) && tokens.length == 2) {
                    this.setName(tokens[1]);
                    System.out.println("Player " + this.getName() + " connected!");

                    for (ServerWorker worker : workerList) {
                        worker.send("tblClear");
                        for (ServerWorker worker1 : workerList) {
                            worker.send("con//" + worker1.getName() + "//" + worker1.getPoints());
                            //worker.send("msg//Server: " + worker1.getName() + " connected.");
                        }
                    }
                } else if ("ready".equals(tokens[0]) && tokens.length == 1) {
                    isReady = true;
                    server.setNumberOfReadyPlayers(1);
                    System.out.println("Player " + this.getName() + " is ready!");

                    for (ServerWorker worker : workerList) {
                        worker.send("msg//Server: " + this.getName() + " is ready!");
                    }

                    if (server.getNumberOfReadyPlayers() == server.getWorkerList().size()) {
                        for (ServerWorker worker : workerList) {
                            worker.send("clrChat");
                            worker.send("msg//Server: Everyone is ready. The game is about to start.");
                        }
                        Thread.sleep(5000);
                        server.setGth(new GameThread(workerList, server));
                        server.startGth();
                    }
                } else if ("stop".equals(tokens[0]) && tokens.length == 1) {
                    server.getWorkerList().remove(this);

                    for (ServerWorker worker : workerList) {
                        worker.send("tblClear");
                        for (ServerWorker worker1 : workerList) {
                            worker.send("con//" + worker1.getName() + "//" + worker1.getPoints());
                        }
                    }
                    clientSocket.close();
                    isConnected = false;
                } else if ("gw".equals(tokens[0]) && tokens.length == 2) {
                    if (!server.getIsPaused()) {
                        for (ServerWorker worker : workerList) {
                            worker.send("msg//Server: " + this.getName() + " guessed: " + tokens[1]);
                        }
                        if (server.getSelectedWord().equals(tokens[1])) {
                            this.givePoint();
                            if (points == server.getPoints()) {
                                for (ServerWorker worker : workerList) {
                                    worker.send("msg//" + this.getName() + " WON!\n Press ready for the next game.");
                                }
                                server.getGth().terminate();
                            } else {
                                server.getGth().threadNotify();
                            }
                        }
                    } else {
                        this.send("msg//Server: " + " Game is paused. Please wait.");
                    }

                } else if ("pause".equals(tokens[0]) && tokens.length == 1) {
                    server.setIsPaused(true);
                    for (ServerWorker worker : workerList) {
                        worker.send("pauseTimer//");
                    }
                } else if ("unpause".equals(tokens[0]) && tokens.length == 1) {
                    server.setIsPaused(false);
                    for (ServerWorker worker : workerList) {
                        worker.send("unpauseTimer//");
                    }
                } else {
                    for (ServerWorker worker : workerList) {
                        if (!(worker == this)) {
                            worker.send(message);
                        }
                    }
                }
            }
        } catch (IOException | InterruptedException | ClassNotFoundException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void send(String msg) throws IOException {
        outputStream.writeObject(msg);
        outputStream.flush();
    }

    public void givePoint() throws IOException {
        points++;

        for (ServerWorker worker : workerList) {
            worker.send("tblClear");
            for (ServerWorker worker1 : workerList) {
                worker.send("con//" + worker1.getName() + "//" + worker1.getPoints());
            }
        }
    }
}
