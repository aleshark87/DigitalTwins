package lamp;

import java.util.Optional;

import controller.LampSimController;

public class SimLamp {
	
	private LampModel model;
	private LampSimController controller;
	
	public SimLamp(final LampSimController controller) {
		System.out.println("Lamp Simulation Starting..\n");
		this.controller = controller;
		Optional<Boolean> lamp_status = controller.getConnection().getRetrieveThing().retrieveLampStatus();
		if(lamp_status.isPresent()) {
			model = new LampModel(lamp_status.get());
		}
	}
	
	public void switchLamp() {
		boolean lamp_status = model.getLampStatus();
		model.setLampStatus(!lamp_status);
		controller.getConnection().getUpdateThing().updateLampStatus(!lamp_status);
	}
	
	public void setLamp(boolean state) {
		model.setLampStatus(state);
		controller.getConnection().getUpdateThing().updateLampStatus(state);
	}
	
	public LampSimController getController() {
		return this.controller;
	}
}
