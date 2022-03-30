package controller;

import application.CarClientConnection;
import application.SimCar;

public class RunCarSimulation {
    
    private SimCar carSimulation;
    private CarClientConnection connection;
    
    public RunCarSimulation() {
        //carSimulation = new SimCar(this);
        connection = new CarClientConnection(this);
        //carSimulation.startEngine();;
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
