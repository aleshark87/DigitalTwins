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
        //Faccio partire i task che regolano la guida e la manutenzione
        exec.scheduleAtFixedRate(driveTask, 0, 3, TimeUnit.SECONDS);
        exec.scheduleAtFixedRate(maintenanceTask, 0, 3, TimeUnit.SECONDS);
    }
    
    public void startCar() {
        System.out.println("The car starts.");
        driveTask.start();
    }
    
    public void maintenanceDone() {
        System.out.println("The car finished maintenance.");
        maintenanceTask.stop();
    }
    
    public void maintenance() {
        System.out.println("The car stops.");
        driveTask.stop();
        System.out.println("The car starts maintenance.");
        maintenanceTask.start();
        
    }
    
    public RunCarSimulation getController() {
        return controller;
    }
    
}
