package application;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import controller.RunCarSimulation;
import tasks.DriveTask;
import tasks.MaintenanceTask;

public class SimCar {
    
    private RunCarSimulation controller;
    private ScheduledExecutorService exec;
    private DriveTask driveTask;
    private MaintenanceTask maintenanceTask;
    
    public SimCar(RunCarSimulation controller) {
        this.controller = controller;
        driveTask = new DriveTask(this);
        maintenanceTask = new MaintenanceTask(this);
        exec = Executors.newSingleThreadScheduledExecutor();
        //Faccio partire il task che regola la guida
        exec.scheduleAtFixedRate(driveTask, 0, 3, TimeUnit.SECONDS);
    }
    
    public void startEngine() {
        System.out.println("The car starts.");
        driveTask.start();
    }
    
    public void stopEngine() {
        System.out.println("The car stops.");
        driveTask.stop();
    }
    
    public RunCarSimulation getController() {
        return controller;
    }
    
}
