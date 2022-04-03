package application;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import controller.CarSimController;
import tasks.DriveTask;
import tasks.GUIUpdateTask;

public class SimCar {
    
    private CarSimController controller;
    private DriveTask driveTask;
    private GUIUpdateTask guiUpdateTask;
    
    public SimCar(CarSimController controller) {
        System.out.println("car simulation starting.\n");
        this.controller = controller;
        controller.getClientConnection().getUpdateProperty().updateCarEngine(false);;
        
        driveTask = new DriveTask(this);
        guiUpdateTask = new GUIUpdateTask(this);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        executor.scheduleAtFixedRate(driveTask, 0, 5, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(guiUpdateTask, 0, 500, TimeUnit.MILLISECONDS);
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

    public void chargeCar() {
        Optional<Boolean> engine_status = controller.getClientConnection().getRetrieveProperty().retrieveEngineStatus();
        if(engine_status.isPresent()) {
            if(!engine_status.get()) {
                System.out.println("charging the car...");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("car charged succesfully");
                controller.getClientConnection().getUpdateProperty().updatechargeCarFull();
            }
            else {
                System.out.println("You have to stop your engine for charging the car.");
            }
            
        }
        
    }
    
}
