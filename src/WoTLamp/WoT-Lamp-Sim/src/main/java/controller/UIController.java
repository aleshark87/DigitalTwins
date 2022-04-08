package controller;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import connection.GUIUpdateTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class UIController {
	
	private LampSimController controller;
	private GUIUpdateTask guiUpdateTask;
	
	@FXML
	private Label label;
	@FXML
	private ImageView imgView;
	
	@FXML
	private void initialize(){
		controller = new LampSimController(this);
		guiUpdateTask = new GUIUpdateTask(controller);
		ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
		exec.scheduleAtFixedRate(guiUpdateTask, 0, 1, TimeUnit.SECONDS);
	}
	
	@FXML
	private void switchBtnClicked() {
		controller.getLampSim().switchLamp();
	}
	
	public void update(String text, Image image) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				label.setText(text);
				imgView.setImage(image);
			}
			
		});
	}
}
