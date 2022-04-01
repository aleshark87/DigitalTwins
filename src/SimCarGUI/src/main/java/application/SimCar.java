package application;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import controllers.CarSimController;
import tasks.DriveTask;

public class SimCar {
    
    private CarSimController controller;
    public static ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private DriveTask driveTask;
    //private MaintenanceTask maintenanceTask;
    
    public SimCar(CarSimController controller) {
        System.out.println("car simulation starting.\n");
        this.controller = controller;
        controller.getClientConnection().updateCarEngine(false);
        driveTask = new DriveTask(this);
        //Faccio partire i task che regolano la guida e la manutenzione
        exec.scheduleAtFixedRate(driveTask, 0, 3, TimeUnit.SECONDS);
    }
    
    public void startEngine() {
        driveTask.startEngine();
    }
    
    public void stopEngine() {
        driveTask.stopEngine();
    }
    
    public CarSimController getController() {
        return controller;
    }
    
}
