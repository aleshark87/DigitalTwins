package connection;

import java.util.Optional;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.protocol.JsonifiableAdaptable;
import org.eclipse.ditto.protocol.ProtocolFactory;

public class UpdateThingProperty {
    
    private DittoClient client;
    private CarClientConnection connection;
    
    public UpdateThingProperty(final DittoClient client, final CarClientConnection connection) {
        this.client = client;
        this.connection = connection;
    }
    
    public void updateCarEngine(final boolean state) {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"io.eclipseprojects.ditto/car/things/twin/commands/modify\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/status/properties/engine\",\n"
                        + "  \"value\": " + state + "\n"
                        + "}").asObject());
        try {
            client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture().join();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void updateCarChargeLevel(final double decrease) {
        Optional<Double> charge_level = connection.getRetrieveProperty().retrieveCarChargeLevel();
        JsonifiableAdaptable jsonifiableAdaptable = null;
        if(charge_level.isPresent()) {
            double newChargeLevel = (charge_level.get() - decrease);
            if(newChargeLevel <= 0.0) {
                if(newChargeLevel < 0.0) {
                    newChargeLevel = 0.0;
                }
                connection.getSimController().getCarSimulation().stopEngine();
            }
            jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                    JsonFactory.readFrom("{\n"
                            + "  \"topic\": \"io.eclipseprojects.ditto/car/things/twin/commands/modify\",\n"
                            + "  \"headers\": {\n"
                            + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                            + "  },\n"
                            + "  \"path\": \"/features/status/properties/charge-level\",\n"
                            + "  \"value\": " + newChargeLevel + "\n"
                            + "}").asObject());
        }
        else {
            jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                    JsonFactory.readFrom("{\n"
                            + "  \"topic\": \"io.eclipseprojects.ditto/car/things/twin/commands/modify\",\n"
                            + "  \"headers\": {\n"
                            + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                            + "  },\n"
                            + "  \"path\": \"/features/status/properties/charge-level\",\n"
                            + "  \"value\": " + "ERROR" + "\n"
                            + "}").asObject());
        }
        try {
            client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture().join();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void updatechargeCarFull() {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"io.eclipseprojects.ditto/car/things/twin/commands/modify\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/status/properties/charge-level\",\n"
                        + "  \"value\": " + 100.0 + "\n"
                        + "}").asObject());
        try {
            client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture().join();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void updateWearLevel(final int increase, final String part) {
        Optional<Integer> wear_level = connection.getRetrieveProperty().retrieveWearLevel(part);
        if(wear_level.isPresent()) {
            int newLevel = wear_level.get() + increase;
            if(newLevel >= 100) {
                connection.getSimController().getCarSimulation().stopEngine();
                if(newLevel > 100) {
                    newLevel = 100;
                }
            }
            JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                    JsonFactory.readFrom("{\n"
                            + "  \"topic\": \"io.eclipseprojects.ditto/car/things/twin/commands/modify\",\n"
                            + "  \"headers\": {\n"
                            + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                            + "  },\n"
                            + "  \"path\": \"/features/wear-time/properties/"+part+"\",\n"
                            + "  \"value\": " + newLevel + "\n"
                            + "}").asObject());
            try {
                client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture().join();
            } catch(Exception e) {
                e.printStackTrace();
            }
            
        }
        else {
            System.out.println("Had problems retrieving the wear_level");
        }
    }
    
    public void updateIndicatorLight(String part, boolean value) {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"io.eclipseprojects.ditto/car/things/twin/commands/modify\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/indicator-light/properties/" + part + "\",\n"
                        + "  \"value\": " + value + "\n"
                        + "}").asObject());
        try {
            connection.sendIndicatorMessage(part);
            client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture().join();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
