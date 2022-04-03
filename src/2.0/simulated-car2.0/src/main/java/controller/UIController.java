package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * The Controller related to the main.fxml GUI.
 *
 */
public final class UIController {
    
    
    @FXML
    private TextArea textArea;
    @FXML
    private Label label_11;
    @FXML
    private Label label_12;
    @FXML
    private Label label_13;
    @FXML
    private Label label_21;
    @FXML
    private Label label_22;
    @FXML
    private Label label_23;
    
    
    
    private CarSimController controller;
    
    @FXML
    private void initialize() {
        clearAllLabels();
        controller = new CarSimController(this);
    }
    
    @FXML
    public void startBtnClicked() {
        controller.getCarSimulation().startEngine();
    }
    
    @FXML
    public void stopBtnClicked() {
        controller.getCarSimulation().stopEngine();
    }
    
    @FXML
    public void chargeBtnClicked() {
        controller.getCarSimulation().chargeCar();
    }
    
    public void updateTextArea(String text) {
        
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                textArea.setText(text);
            }
            
        });
    }
    
    private void clearAllLabels() {
        label_11.setText("");
        label_12.setText("");
        label_13.setText("");
        label_21.setText("");
        label_22.setText("");
        label_23.setText("");
    }
    
    
}
