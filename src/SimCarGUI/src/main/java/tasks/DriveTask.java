package tasks;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.ditto.things.model.Features;
import org.eclipse.ditto.things.model.Thing;

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
    
    /* During the drive, the charge-level of the battery decrease of 0.5 for each cycle, the wear of the engine increase
     * 1 for each cycle, and the wear of the battery increase 2 for each cycle when the charge-level is less than 70.
     * 10 if its less than 30.
     */
    @Override
    public void run() {
        if(stop) {
            connection.updateCarChargeLevel(5.0);
            connection.updateWearLevel(1, "engine-wear");
            if(connection.retrieveCarChargeLevel() < 70.0) {
                connection.updateWearLevel(2, "battery-wear");
            }
        }
    }
    
    public void stopEngine() {
        stop = false;
        simulationCar.getController().getClientConnection().updateCarEngine(false);
    }
    
    public void startEngine() {
        stop = true;
        simulationCar.getController().getClientConnection().updateCarEngine(true);
    }

}
