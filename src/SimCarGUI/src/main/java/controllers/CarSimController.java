package controllers;

import application.CarClientConnection;
import application.SimCar;
import guicontrollers.UIController;

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
                System.out.println("Can't start car simulation without twin online");
            }
        }
        else {
            System.out.println("Can't start car simulation without ditto service");
        }
        this.view = view; 
        //carSimulation.startCar();
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
    
    /*
    public static void main(String[] args) {
        new RunCarSimulation();
    }
    */
}
