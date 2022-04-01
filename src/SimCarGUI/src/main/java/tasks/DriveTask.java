package tasks;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.ditto.things.model.Features;
import org.eclipse.ditto.things.model.Thing;

import application.SimCar;

public class DriveTask implements Runnable{

    private boolean stop;
    private SimCar simulationCar;
    
    public DriveTask(SimCar simulation) {
        this.stop = false;
        this.simulationCar = simulation;
    }
    
    /* During the drive, the charge-level of the battery decrease of 0.5 for each cycle, the wear of the engine increase
     * 5 for each cycle, and the wear of the battery increase 5 for each cycle when the charge-level is less than 50. 10 if its less than 30.
     */
    @Override
    public void run() {
        if(stop) {
            System.out.println("update charge-level");
            simulationCar.getController().getClientConnection().updateCarChargeLevel(0.5);
        }
    }
    
    public void stopEngine() {
        stop = false;
        simulationCar.getController().getClientConnection().updateCarEngine(false);
    }
    
    public void startEngine() {
        System.out.println("start engine");
        stop = true;
        simulationCar.getController().getClientConnection().updateCarEngine(true);
    }

}
