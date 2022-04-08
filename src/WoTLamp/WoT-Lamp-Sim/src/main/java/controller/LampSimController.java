package controller;

import connection.LampConnection;
import lamp.SimLamp;

public class LampSimController {
	private SimLamp lampSimulation;
	private LampConnection connection;
	private UIController view;
	
	public LampSimController(final UIController view) {
		connection = new LampConnection(this);
        if(connection.getConnStatus()) {
            if(connection.getTwinStatus()) {
                lampSimulation = new SimLamp(this);
            }
            else {
                String errorMsg = "Can't start lamp simulation without twin online";
                System.out.println(errorMsg);
                //view.updateTextArea(errorMsg);
            }
        }
        else {
            String errorMsg = "Can't start lamp simulation without ditto service";
            System.out.println(errorMsg);
            //view.updateTextArea(errorMsg);
        }
        this.view = view; 
	}
	
	public LampConnection getConnection() {
		return this.connection;
	}
	
	public UIController getView() {
		return this.view;
	}
	
	public SimLamp getLampSim() {
		return this.lampSimulation;
	}
}
