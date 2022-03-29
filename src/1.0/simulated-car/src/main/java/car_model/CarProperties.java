package car_model;

public enum CarProperties {
    ENGINE("engine");

    private CarProperties(String feature) {
        this.carProperty = feature;
    }
    
    private final String carProperty;
    
    public String get() {
        return this.carProperty;
    }
}
