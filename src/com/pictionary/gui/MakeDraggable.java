package com.pictionary.gui;

import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MakeDraggable {

    private Stage stage;
    private double x = 0, y = 0;
    
    public MakeDraggable(AnchorPane parent) {
        
        parent.setOnMousePressed(
                (event) -> {
                    if (!(event.getButton()== MouseButton.PRIMARY)) {
                        x = event.getSceneX();
                        y = event.getSceneY();
                    }

                }
        );

        parent.setOnMouseDragged(
                (event) -> {  
                    if (!(event.getButton()== MouseButton.PRIMARY)) {
                        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setX(event.getScreenX() - x);
                        stage.setY(event.getScreenY() - y);
                    }
                }
        );
    }
}
