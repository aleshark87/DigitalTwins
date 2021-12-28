package controller;

import application.CarClientConnection;
import application.SimCar;

public class SimCarController {
    
    private SimCar carSimulation;
    private CarClientConnection connection;
    
    public SimCarController() {
        carSimulation = new SimCar(this);
        connection = new CarClientConnection(this);
        carSimulation.startCar();
    }
    
    public CarClientConnection getClientConnection() {
        return connection;
    }
    
    public SimCar getCarSimulation() {
        return carSimulation;
    }
    
    public static void main(String[] args) {
        new SimCarController();
    }

}
