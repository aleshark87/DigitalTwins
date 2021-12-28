package application;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import controller.SimCarController;
import tasks.DriveTask;
import tasks.MaintenanceTask;

public class SimCar {
    
    private SimCarController controller;
    private ScheduledExecutorService exec;
    private DriveTask driveTask;
    private MaintenanceTask maintenanceTask;
    
    public SimCar(SimCarController controller) {
        this.controller = controller;
        exec = Executors.newSingleThreadScheduledExecutor();
        driveTask = new DriveTask(this);
        maintenanceTask = new MaintenanceTask(this);
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
    
    public SimCarController getController() {
        return controller;
    }
    
}
