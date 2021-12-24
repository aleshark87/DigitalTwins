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
        Runnable task = new Runnable() {
            int counter = 0;
            
            @Override
            public void run() {
                counter++;
                /*Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        noti.notified(counter);
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();*/
            }
        };
        //ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        //exec.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
        dittoClient.updateCarLocation();
    }
    
}
