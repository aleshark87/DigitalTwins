package lamp;

import controller.LampSimController;

public class SimLamp {
	private LampSimController controller;
	
	public SimLamp(final LampSimController controller) {
		System.out.println("Lamp Simulation Starting..\n");
		this.controller = controller;
	}
	
	public void switchLamp() {
		//retrievePropertyAndSwitch
	}
}
