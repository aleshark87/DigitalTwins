package tasks;

import application.SimCar;

public class DriveTask implements Runnable{

    private boolean stop;
    private SimCar simulationCar;
    
    public DriveTask(SimCar simulation) {
        this.stop = true;
        this.simulationCar = simulation;
    }
    
    @Override
    public void run() {
        if(!stop) {
            System.out.println("currently driving");
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
