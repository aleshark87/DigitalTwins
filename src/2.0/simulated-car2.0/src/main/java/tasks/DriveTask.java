package tasks;

import java.util.Optional;

import application.CarClientConnection;
import application.SimCar;

public class DriveTask implements Runnable{

    private boolean stop;
    private SimCar simulationCar;
    private CarClientConnection connection;
    
    public DriveTask(SimCar simulation) {
        this.stop = false;
        this.simulationCar = simulation;
        this.connection = simulation.getController().getClientConnection();
    }
    
    @Override
    public void run() {
        if(stop) {
            connection.getUpdateProperty().updateCarChargeLevel(5);
            connection.getUpdateProperty().updateWearLevel(1, "engine-wear");
            Optional<Double> chargeLevel = connection.getRetrieveProperty().retrieveCarChargeLevel();
            if(chargeLevel.isPresent()) {
                if(chargeLevel.get() < 50.0) {
                    connection.getUpdateProperty().updateWearLevel(5, "battery-wear");
                }
            }
            controlWears();
        }
    }
    
    private void controlWears() {
        Optional<Integer> engineWear = connection.getRetrieveProperty().retrieveWearLevel("engine-wear");
        Optional<Integer> batteryWear = connection.getRetrieveProperty().retrieveWearLevel("battery-wear");
        if(engineWear.isPresent()) {
            if(engineWear.get() > 50) {
                connection.getUpdateProperty().updateIndicatorLight("engine-indicator", true);
            }
        }
        
        if(batteryWear.isPresent()) {
            if(batteryWear.get() > 50) {
                connection.getUpdateProperty().updateIndicatorLight("battery-indicator", true);
            }
        }
    }
    
    public void stopEngine() {
        stop = false;
        simulationCar.getController().getClientConnection().getUpdateProperty().updateCarEngine(false);
    }
    
    public void startEngine() {
        stop = true;
        simulationCar.getController().getClientConnection().getUpdateProperty().updateCarEngine(true);
    }

}
