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
    private CarSimController controller = new CarSimController(this);
    
    @FXML
    private void initialize() {
        
    }
    
    @FXML
    public void startBtnClicked() {
        textArea.setText("start btn clicked");
    }
    
    @FXML
    public void stopBtnClicked() {
        textArea.setText("stop btn clicked");
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
