package controller;

import application.CarClientConnection;
import application.SimCar;

public class RunCarSimulation {
    
    private SimCar carSimulation;
    private CarClientConnection connection;
    
    public RunCarSimulation() {
        connection = new CarClientConnection(this);
        if(connection.isTwinOnline()) {
          //carSimulation = new SimCar(this);
          //carSimulation.startEngine();
        }

    }
    
    public CarClientConnection getClientConnection() {
        return connection;
    }
    
    public SimCar getCarSimulation() {
        return carSimulation;
    }
    
    public static void main(String[] args) {
        new RunCarSimulation();
    }

}
