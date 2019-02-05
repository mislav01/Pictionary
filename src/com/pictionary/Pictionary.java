package com.pictionary;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Pictionary extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        //Reflection reflection = new Reflection();
        //reflection.doReflection();

        Parent root = FXMLLoader.load(getClass().getResource("/com/pictionary/gui/LoadScreen.fxml"));
        stage.initStyle(StageStyle.TRANSPARENT);

        Scene scene = new Scene(root);
        //scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
