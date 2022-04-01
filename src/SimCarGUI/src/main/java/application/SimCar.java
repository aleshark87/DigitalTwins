package application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import controllers.CarSimController;
import tasks.DriveTask;
import tasks.GUIUpdateTask;

public class SimCar {
    
    private CarSimController controller;
    private DriveTask driveTask;
    private GUIUpdateTask guiUpdateTask;
    //private MaintenanceTask maintenanceTask;
    
    public SimCar(CarSimController controller) {
        System.out.println("car simulation starting.\n");
        this.controller = controller;
        controller.getClientConnection().updateCarEngine(false);
        
        driveTask = new DriveTask(this);
        guiUpdateTask = new GUIUpdateTask(this);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        executor.scheduleAtFixedRate(driveTask, 0, 2, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(guiUpdateTask, 0, 1, TimeUnit.SECONDS);
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
    /*
    public ExecutorService getExecutor() {
        return this.executor;
    }*/
    
}
