package connection;

import controller.LampSimController;
import javafx.scene.image.Image;

public class GUIUpdateTask implements Runnable{

    private LampSimController controller;
    
    public GUIUpdateTask(LampSimController controller) {
        this.controller = controller;
    }
    
    @Override
    public void run() {
    	Image image;
    	boolean lamp_status = controller.getConnection().getRetrieveThing().retrieveLampStatus().get();
    	if(lamp_status) {
    		image = new Image("icons/lamp_on.png");
    	}
    	else {
    		image = new Image("icons/lamp_off.png");
    	}
        String featureProperty = "Lamp Status: " + lamp_status;
        controller.getView().update(featureProperty, image); 
    }
    
    
}
