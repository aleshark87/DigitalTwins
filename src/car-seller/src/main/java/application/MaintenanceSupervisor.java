package application;

public class MaintenanceSupervisor {
    
    private final int engineNeedMaintenance = 3;
    private final int maintenanceDuration = 3;
    private boolean maintenanceStatus;
    private CarsClient client;
    
    public MaintenanceSupervisor(CarsClient client) {
        maintenanceStatus = false;
        this.client = client;
    }
    
    public void checkForMaintenance(final int engineMinutes) {
        if(engineMinutes >= engineNeedMaintenance) {
            maintenanceStatus = true;
            client.updateMaintenance(true);
        }
    }
    
    public void checkForEndMaintenance(final int maintenanceTime) {
        if(maintenanceTime >= maintenanceDuration) {
            maintenanceStatus = false;
            client.updateMaintenance(false);
        }
    }
    
    public boolean getMaintenanceStatus() {
        return maintenanceStatus;
    }
    
}
