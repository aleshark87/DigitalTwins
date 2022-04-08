package controller;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import connection.GUIUpdateTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UIController {
	
	private LampSimController controller;
	private GUIUpdateTask guiUpdateTask;
	
	@FXML
	private Label label;
	
	@FXML
	private void initialize() {
		label.setText("");
		controller = new LampSimController(this);
		guiUpdateTask = new GUIUpdateTask(controller, controller.getConnection().getNamespace());
		ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
		exec.scheduleAtFixedRate(guiUpdateTask, 0, 1, TimeUnit.SECONDS);
	}
	
	@FXML
	private void switchBtnClicked() {
		controller.getLampSim().switchLamp();
	}
	
	public void update(String text) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				label.setText(text);
			}
			
		});
	}
}
