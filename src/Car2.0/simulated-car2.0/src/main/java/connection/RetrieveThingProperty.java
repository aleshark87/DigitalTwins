package connection;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.ditto.base.model.common.HttpStatus;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.protocol.Adaptable;
import org.eclipse.ditto.protocol.JsonifiableAdaptable;
import org.eclipse.ditto.protocol.ProtocolFactory;

public class RetrieveThingProperty {
    private DittoClient client;
    
    public RetrieveThingProperty(final DittoClient client) {
        this.client = client;
    }
    
    public HttpStatus retrieveThing() {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"io.eclipseprojects.ditto/car/things/twin/commands/retrieve\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/\"\n"
                        + "}\n"
                        + "").asObject());
        HttpStatus p = null;
        
        try {
            Adaptable adapt = client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture().join();
            p = adapt.getPayload().getHttpStatus().get();
            System.out.println(adapt.getPayload().getValue().get());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }
    
    public Optional<Double> retrieveCarChargeLevel() {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"io.eclipseprojects.ditto/car/things/twin/commands/retrieve\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/status/properties/charge-level\"\n"
                        + "}\n").asObject());
        Optional<Double> charge_level = Optional.empty();
        try {
            CompletableFuture<Adaptable> complFuture = client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture();
            var adapt = complFuture.join();
            charge_level = Optional.of(adapt.getPayload().getValue().get().asDouble());
            //System.out.println(charge_level);
        } catch (Exception e) {
            System.out.println("Failed to retrieve chargelevel");
            charge_level = Optional.empty();
        }
        return charge_level;
    }
    
    public Optional<Boolean> retrieveEngineStatus(){
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"io.eclipseprojects.ditto/car/things/twin/commands/retrieve\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/status/properties/engine\"\n"
                        + "}\n").asObject());
        Optional<Boolean> engine_status = Optional.empty();
        try {
            CompletableFuture<Adaptable> complFuture = client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture();
            var adapt = complFuture.join();
            engine_status = Optional.of(adapt.getPayload().getValue().get().asBoolean());
            //System.out.println(charge_level);
        } catch (Exception e) {
            System.out.println("Failed to retrieve enginestatus");
            engine_status = Optional.empty();
        }
        return engine_status;
    }
    
    @SuppressWarnings("unused")
    private Optional<Boolean> retrieveIndicatorLight(String part){
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"io.eclipseprojects.ditto/car/things/twin/commands/retrieve\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/indicator-light/properties/" + part + "\"\n"
                        + "}\n").asObject());
        Optional<Boolean> indicator_light = Optional.empty();
        try {
            CompletableFuture<Adaptable> complFuture = client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture();
            var adapt = complFuture.join();
            indicator_light = Optional.of(adapt.getPayload().getValue().get().asBoolean());
        } catch(Exception e) {
            System.out.println("Failed to retrieve indicatorlight");
            indicator_light = Optional.empty();
        }
        return indicator_light;
    }
    
    public Optional<Integer> retrieveWearLevel(String part) {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"io.eclipseprojects.ditto/car/things/twin/commands/retrieve\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/wear-time/properties/"+part+"\"\n"
                        + "}\n").asObject());
        Optional<Integer> wear_level = Optional.empty();
        try {
            CompletableFuture<Adaptable> complFuture = client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture();
            var adapt = complFuture.join();
            wear_level = Optional.of(adapt.getPayload().getValue().get().asInt());
        } catch(Exception e) {
            System.out.println("Failed to retrieve wearlevel");
            wear_level = Optional.empty();
        }
        return wear_level;
    }
}
