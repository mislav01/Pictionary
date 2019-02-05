package com.pictionary.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends Thread {

    private final int serverPort;
    private volatile int numberOfReadyPlayers = 0;
    private ServerSocket serverSocket = null;
    private volatile ArrayList<ServerWorker> workerList = new ArrayList<>();
    private volatile ArrayList<String> wordList = new ArrayList<>();
    private String selectedWord;
    private GameThread gth;
    private boolean isPaused = false;
    private int points;

    public int getPoints() {
        return points;
    }

    public boolean getIsPaused() {
        return isPaused;
    }

    public void setIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public GameThread getGth() {
        return gth;
    }

    public void setGth(GameThread gth) {
        this.gth = gth;
    }

    public String getSelectedWord() {
        return selectedWord;
    }

    public void setSelectedWord(String selectedWord) {
        this.selectedWord = selectedWord;
    }

    public Server(int serverPort, ArrayList<String> wordList, int points) {
        this.serverPort = serverPort;
        this.wordList = wordList;
        this.points = points;
    }

    public List<ServerWorker> getWorkerList() {
        return workerList;
    }

    public int getNumberOfReadyPlayers() {
        return numberOfReadyPlayers;
    }

    public void setNumberOfReadyPlayers(int numberOfReadyPlayers) {
        this.numberOfReadyPlayers++;
    }

    @Override
    public void run() {

        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Server listening on port: " + serverPort);
            System.out.println("Number of points: " + points);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getRandomWord() {
        Random rnd = new Random();
        return wordList.get(rnd.nextInt(wordList.size()));
    }

    void startGth() {
        gth.start();
    }
}
