package car_model;

public enum CarFeatures {
    PARTS_TIME("parts_time"),
    PARTS_MAINTENANCE("parts_maintenance");

    CarFeatures(String feature) {
        this.carFeature = feature;
    }
    
    private final String carFeature;
    
    public String get() {
        return this.carFeature;
    }
}
