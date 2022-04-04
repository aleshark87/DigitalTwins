package tasks;

import application.SimCar;

public class DriveTask implements Runnable{

    private int distanceCounter;
    private boolean stop;
    private SimCar simulationCar;
    
    public DriveTask(SimCar simulation) {
        this.distanceCounter = 0;
        this.stop = true;
        this.simulationCar = simulation;
    }
    
    @Override
    public void run() {
        if(!stop) {
            distanceCounter++;
            simulationCar.getController().getClientConnection().updateCarEngine(distanceCounter);
        }
    }
    
    public void stop() {
        stop = true;
        distanceCounter = 0;
    }
    
    public void start() {
        stop = false;
    }

}
