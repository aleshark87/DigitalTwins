package tasks;

import application.SimCar;

public class DriveTask implements Runnable{

    private boolean stop;
    private SimCar simulationCar;
    
    public DriveTask(SimCar simulation) {
        this.stop = true;
        this.simulationCar = simulation;
    }
    
    /* During the drive, the charge-level of the battery decrease of 0.5 for each cycle, the wear of the engine increase
     * 5 for each cycle, and the wear of the battery increase 5 for each cycle when the charge-level is less than 50. 10 if its less than 30.
     */
    @Override
    public void run() {
        if(!stop) {
            simulationCar.getController().getClientConnection().updateCarChargeLevel(0.5);
            //simulationCar.getController().getClientConnection().updateWearLevel(5, "engine-wear");
            System.out.println("cycle");
            //simulationCar.getController().getClientConnection().updateBatteryWear(increase)
        }
    }
    
    public void stopEngine() {
        stop = true;
        simulationCar.getController().getClientConnection().updateCarEngine(false);
    }
    
    public void startEngine() {
        stop = false;
        simulationCar.getController().getClientConnection().updateCarEngine(true);
    }

}
