package tasks;

import application.SimCar;

public class MaintenanceTask implements Runnable{
    
    private int timeCounter;
    private boolean stop;
    private SimCar simulationCar;
    
    public MaintenanceTask(SimCar simulation) {
        this.timeCounter = 0;
        this.stop = true;
        this.simulationCar = simulation;
    }
    
    @Override
    public void run() {
        if(!stop) {
            timeCounter++;
            simulationCar.getController().getClientConnection().updateMaintenanceTime(timeCounter);
        }
    }
    
    public void stop() {
        stop = true;
        timeCounter = 0;
        simulationCar.getController().getClientConnection().updateMaintenanceTime(timeCounter);
        simulationCar.getController().getClientConnection().updateCarEngine(0);
    }
    
    public void start() {
        stop = false;
    }

}
