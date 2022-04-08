package lamp;

import java.util.Optional;

import controller.LampSimController;

public class SimLamp {
	private LampSimController controller;
	
	public SimLamp(final LampSimController controller) {
		System.out.println("Lamp Simulation Starting..\n");
		this.controller = controller;
	}
	
	public void switchLamp() {
		Optional<Boolean> lamp_status = controller.getConnection().getRetrieveThing().retrieveLampStatus();
		if(lamp_status.isPresent()) {
			controller.getConnection().getUpdateThing().updateLampStatus(!lamp_status.get());
		}
	}
	
	public LampSimController getController() {
		return this.controller;
	}
}
