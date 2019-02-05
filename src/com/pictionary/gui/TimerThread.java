package com.pictionary.gui;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;

public class TimerThread extends Thread {

    private final Label lblTime;
    private boolean running = true;
    private int time = 120;

    public TimerThread(Label lblTime) {
        this.lblTime = lblTime;

    }

    public void terminate() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            Integer value;
            synchronized (lblTime) {
                while (lblTime.getTextFill() == Paint.valueOf("YELLOW")) {
                    try {
                        Platform.runLater(
                                () -> {
                                    lblTime.setText("Paused");
                                }
                        );
                        lblTime.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TimerThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                value = 1;
            }
            if (value != null) {
                Platform.runLater(
                        () -> {
                            lblTime.setText(String.valueOf(time--));
                        }
                );
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TimerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }

}
