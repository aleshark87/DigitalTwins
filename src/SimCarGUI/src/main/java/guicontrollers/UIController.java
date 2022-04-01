package guicontrollers;

import controllers.CarSimController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

/**
 * The Controller related to the main.fxml GUI.
 *
 */
public final class UIController {
    
    
    @FXML
    private TextArea textArea;
    private CarSimController controller;
    
    @FXML
    private void initialize() {
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
    
    public void updateTextArea(String text) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                textArea.setText(text);
            }
            
        });
    }
    
    
    
}
