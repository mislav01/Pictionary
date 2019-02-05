package com.pictionary.gui;

import com.jfoenix.controls.JFXTextField;
import com.pictionary.client.Client;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoadScreenController implements Initializable {

    private static final Logger logger = Logger.getLogger(LoadScreenController.class.getName());
    private Client client;

    @FXML
    private JFXTextField txtName;

    @FXML
    private AnchorPane parent;

    @FXML
    private void onBtnNewGameAction(ActionEvent event) {

        if (txtName.getText().isEmpty()) {
            txtName.requestFocus();
        } else {
            try {
                client = new Client("localhost", 1337);
                client.setName(txtName.getText());
                client.start();

                Stage stage = new Stage();
                stage.initStyle(StageStyle.TRANSPARENT);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pictionary/gui/GameScreen.fxml"));

                Parent root = (Parent) loader.load();

                GameScreenController gsc = loader.<GameScreenController>getController();
                gsc.setChatClient(client);
                client.setGameScreenController(gsc);

                Scene scene = new Scene(root);
                scene.setFill(Color.TRANSPARENT);

                stage.setScene(scene);

                Stage thisStage = (Stage) ((Node) (event.getSource())).getScene().getWindow();
                thisStage.close();

                stage.show();
            } catch (IOException e) {
                logger.log(Level.WARNING, e.getMessage());
            }
        }

    }

    @FXML
    private void onBtnLoadAction(ActionEvent event) {
        try {
            ObjectInputStream ois = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(
                            "image.ser")));

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pictionary/gui/GameScreen.fxml"));

            Parent root = (Parent) loader.load();

            GameScreenController gsc = loader.<GameScreenController>getController();
            gsc.setGraphicsContext((ArrayList<String>) ois.readObject());

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);

            Stage thisStage = (Stage) ((Node) (event.getSource())).getScene().getWindow();
            thisStage.close();

            ois.close();
            stage.show();

        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
        }
    }

    @FXML
    private void onBtnExitGameAction(ActionEvent event) {
        Stage stage = (Stage) ((Node) (event.getSource())).getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        MakeDraggable makeDraggable = new MakeDraggable(parent);
    }
}
