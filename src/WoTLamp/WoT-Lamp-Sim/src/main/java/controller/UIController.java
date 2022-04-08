package controller;

import javafx.fxml.FXML;

public class UIController {
	
	private LampSimController controller;
	
	@FXML
	private void initialize() {
		controller = new LampSimController(this);
	}
}
