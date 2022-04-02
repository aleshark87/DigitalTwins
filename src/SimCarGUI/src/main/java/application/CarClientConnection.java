package application;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.ditto.base.model.common.HttpStatus;
import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.DittoClients;
import org.eclipse.ditto.client.configuration.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.configuration.WebSocketMessagingConfiguration;
import org.eclipse.ditto.client.live.LiveThingHandle;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.AuthenticationProviders;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.messaging.MessagingProviders;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.protocol.Adaptable;
import org.eclipse.ditto.protocol.JsonifiableAdaptable;
import org.eclipse.ditto.protocol.ProtocolFactory;
import org.eclipse.ditto.things.model.Features;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;

import com.neovisionaries.ws.client.WebSocket;

import controllers.CarSimController;

public class CarClientConnection {
    
    private CarSimController controller;
    private AuthenticationProvider<WebSocket> authenticationProvider;
    private MessagingProvider messagingProvider;
    private DittoClient client;
    private boolean connectionStatus = false;
    private boolean twinStatus = false;
    
    private void createAuthProvider() {
        authenticationProvider = AuthenticationProviders.basic((
                BasicAuthenticationConfiguration
                .newBuilder()
                .username("ditto")
                .password("ditto")
                .build()));
    }
    
    private void createMessageProvider() {
        MessagingConfiguration.Builder builder = WebSocketMessagingConfiguration.newBuilder()
                .endpoint("ws://localhost:8080/ws/2")
                .jsonSchemaVersion(JsonSchemaVersion.V_2)
                .reconnectEnabled(false);
        messagingProvider = MessagingProviders.webSocket(builder.build(), authenticationProvider);
    }
    
    public CarClientConnection(CarSimController controller) {
        System.out.println("car client connection starting.\n");
        this.controller = controller;
        createAuthProvider();
        createMessageProvider();
        try {
            client = DittoClients.newInstance(messagingProvider)
                    .connect()
                    .toCompletableFuture()
                    .join();
            connectionStatus = true;
        }catch(Exception e){
            System.out.println("Ditto connection not open.");
        };
        if(connectionStatus) {
            if(retrieveThing().getCode() == 200) {
                twinStatus = true;
            }
        }
        //subscribeForMessages();
    }
    
    /*
    private void subscribeForMessages() {
        try {
            client.live().startConsumption().toCompletableFuture().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ThingId thingId = ThingId.of("io.eclipseprojects.ditto", "car");
        final LiveThingHandle thingIdLive = client.live().forId(thingId);
        // Register for *all* messages of a *specific* thing and provide payload as String
        thingIdLive.registerForMessage("msg_maintenance", "supervisor.maintenance", String.class, message -> {
            final Optional<String> payload = message.getPayload();
            if(payload.get().equals("DoMaintenance")) {
                controller.getCarSimulation().maintenance();
            }
            else {
                if(payload.get().equals("DoneMaintenance")) {
                    controller.getCarSimulation().maintenanceDone();
                    controller.getCarSimulation().startCar();
                }
            }
        });
    }
    */
    
    //Shadowing Status Engine
    public void updateCarEngine(final boolean state) {
        //System.out.println("state " + state);
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"io.eclipseprojects.ditto/car/things/twin/commands/modify\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/status/properties/engine\",\n"
                        + "  \"value\": " + state + "\n"
                        + "}").asObject());
        client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture().join();
    }
    
    //Shadowing Status Charge-Level
    public void updateCarChargeLevel(final double decrease) {
        Optional<Double> charge_level = retrieveCarChargeLevel();
        JsonifiableAdaptable jsonifiableAdaptable = null;
        if(charge_level.isPresent()) {
            double newChargeLevel = (charge_level.get() - decrease);
            if(newChargeLevel <= 0.0) {
                if(newChargeLevel < 0.0) {
                    newChargeLevel = 0.0;
                }
                this.controller.getCarSimulation().stopEngine();
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
        client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture().join();
    }
    
    public void chargeCarFull() {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"io.eclipseprojects.ditto/car/things/twin/commands/modify\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/status/properties/charge-level\",\n"
                        + "  \"value\": " + 100.0 + "\n"
                        + "}").asObject());
        client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture().join();
    }
    
  //Retrieve Last ChargeLevel value
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
            System.out.println("Failed to retrieve chargelevel");
            engine_status = Optional.empty();
        }
        return engine_status;
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
            client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture().join();
        }catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    /*
     * Shadowing Wear Levels
     */
    public void updateWearLevel(final int increase, final String part) {
        Optional<Integer> wear_level = retrieveWearLevel(part);
        if(wear_level.isPresent()) {
            int newLevel = wear_level.get() + increase;
            if(newLevel >= 100) {
                this.controller.getCarSimulation().stopEngine();
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
            client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture().join();
        }
        else {
            System.out.println("Had problems retrieving the wear_level");
        }
    }
    
    /*
     * Retrieves the wear of the given part.
     */
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
            wear_level = Optional.empty();
            e.printStackTrace();
        }
        return wear_level;
    }
   
    //Controlla se il Twin Car è già stato creato
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
            //System.out.println(p.getCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }
    
    
    
    
    public DittoClient getDittoClient() {
        return this.client;
    }
    
    //Returns True if the client is connected to ditto endpoint, False otherwards.
    public boolean getConnStatus() {
        return this.connectionStatus;
    }
    
  //Returns True if the twin exists is, False otherwards.
    public boolean getTwinStatus() {
        return this.twinStatus;
    }

}
