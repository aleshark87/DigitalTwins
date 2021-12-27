package application;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class LaunchSimCar {

    private LaunchSimCar() { }
    
    static int counter = 0;
    /**
     * @param args unused
     */
    public static void main(final String[] args) {
        CarClientConnection dittoClient = new CarClientConnection();
        Runnable driveTask = new Runnable() {
            int counter = 0;
            
            @Override
            public void run() {
                counter++;
                dittoClient.updateCarLocation(counter);
            };
            
        };
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(driveTask, 0, 5, TimeUnit.SECONDS);
    }
    
}
