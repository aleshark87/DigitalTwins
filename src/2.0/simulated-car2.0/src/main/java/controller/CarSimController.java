package controller;

import car.SimCar;
import connection.CarClientConnection;

public class CarSimController {
    
    private SimCar carSimulation;
    private CarClientConnection connection;
    private UIController view;
    
    public CarSimController(UIController view) {
        connection = new CarClientConnection(this);
        if(connection.getConnStatus()) {
            if(connection.getTwinStatus()) {
                carSimulation = new SimCar(this);
            }
            else {
                String errorMsg = "Can't start car simulation without twin online";
                System.out.println(errorMsg);
                //view.updateTextArea(errorMsg);
            }
        }
        else {
            String errorMsg = "Can't start car simulation without ditto service";
            System.out.println(errorMsg);
            //view.updateTextArea(errorMsg);
        }
        this.view = view; 
    }
    
    public CarClientConnection getClientConnection() {
        return connection;
    }
    
    public SimCar getCarSimulation() {
        return carSimulation;
    }
    
    public UIController getView() {
        return this.view;
    }
    
}
