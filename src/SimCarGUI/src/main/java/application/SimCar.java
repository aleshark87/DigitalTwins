package application;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import controllers.CarSimController;
import tasks.DriveTask;

public class SimCar {
    
    private CarSimController controller;
    private ScheduledExecutorService exec;
    private DriveTask driveTask;
    //private MaintenanceTask maintenanceTask;
    
    public SimCar(CarSimController controller) {
        System.out.println("car simulation starting.\n");
        this.controller = controller;
        driveTask = new DriveTask(this);
        exec = Executors.newSingleThreadScheduledExecutor();
        //Faccio partire i task che regolano la guida e la manutenzione
        exec.scheduleAtFixedRate(driveTask, 0, 3, TimeUnit.SECONDS);
        //exec.scheduleAtFixedRate(maintenanceTask, 0, 3, TimeUnit.SECONDS);
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
