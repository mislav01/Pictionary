package com.pictionary.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameThread extends Thread {

    private int playerIndex = 0;
    private ServerWorker onTurn;
    private List<ServerWorker> workerList;
    private final Server server;
    private volatile boolean running = true;

    public GameThread(List<ServerWorker> workerList, Server server) {
        this.workerList = workerList;
        this.server = server;
    }

    public synchronized void threadWait() {
        try {
            this.wait();
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void threadNotify() {
        this.notify();
    }

    public void terminate() throws IOException {
        onTurn.send("gw//clear");
        for (ServerWorker worker : workerList) {
            worker.send("enblBoard//");
            worker.send("enblClear//");
            worker.send("enblReady//");
            worker.send("stopTimer//");
            worker.setIsReady(false);
        }
        running = false;
    }

    @Override
    public void run() {
        try {
            while (running) {

                for (ServerWorker worker : workerList) {
                    worker.send("clrChat");
                }

                server.setSelectedWord(server.getRandomWord());

                onTurn = workerList.get(playerIndex);

                onTurn.send("gw//" + server.getSelectedWord());
                onTurn.send("enblBoard//");
                onTurn.send("enblClear//");
                for (ServerWorker worker : workerList) {
                    if (worker != onTurn) {
                        worker.send("dsblBoard//");
                        worker.send("dsblClear");

                    }
                }

                for (ServerWorker worker : workerList) {
                    worker.send("startTimer//");
                    worker.send("msg//Server: Guess the word with !");
                }

                this.threadWait();

                onTurn.send("gw//clear");
                playerIndex++;
                if (playerIndex >= workerList.size()) {
                    playerIndex = 0;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.WARNING, null, ex);
        }
    }
}
