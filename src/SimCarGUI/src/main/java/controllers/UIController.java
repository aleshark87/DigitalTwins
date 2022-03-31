package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

/**
 * The Controller related to the main.fxml GUI.
 *
 */
public final class UIController {
    
    
    @FXML
    private TextArea textArea;
    
    @FXML
    private void initialize() {
        textArea.setStyle("-fx-text-alignment: center;");
    }
    
    @FXML
    public void startBtnClicked() {
        textArea.setText("start btn clicked");
    }
    
    @FXML
    public void stopBtnClicked() {
        textArea.setText("stop btn clicked");
    }
    
    
    
}
