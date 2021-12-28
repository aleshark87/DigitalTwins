package application;

public class MaintenanceSupervisor {
    
    private boolean maintenanceStatus;
    private CarsClient client;
    
    public MaintenanceSupervisor(CarsClient client) {
        maintenanceStatus = false;
        this.client = client;
    }
    
    public void checkForMaintenance(final int engineMinutes) {
        if(engineMinutes > 5) {
            maintenanceStatus = true;
            client.updateMaintenance(true);
        }
    }
    
    public void checkForEndMaintenance(final int maintenanceTime) {
        if(maintenanceTime > 10) {
            maintenanceStatus = false;
            client.updateMaintenance(false);
        }
    }
    
    public boolean getMaintenanceStatus() {
        return maintenanceStatus;
    }
    
}
