package application;

import java.util.Optional;
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
import org.eclipse.ditto.protocol.JsonifiableAdaptable;
import org.eclipse.ditto.protocol.ProtocolFactory;
import org.eclipse.ditto.things.model.ThingId;

import com.neovisionaries.ws.client.WebSocket;

import controller.RunCarSimulation;

public class CarClientConnection {
    
    private RunCarSimulation controller;
    private AuthenticationProvider<WebSocket> authenticationProvider;
    private MessagingProvider messagingProvider;
    private DittoClient client;
    
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
    
    public CarClientConnection(RunCarSimulation controller) {
        this.controller = controller;
        createAuthProvider();
        createMessageProvider();
        client = DittoClients.newInstance(messagingProvider)
                .connect()
                .toCompletableFuture()
                .join();
        subscribeForMessages();
    }
    
    private void subscribeForMessages() {
        try {
            client.live().startConsumption().toCompletableFuture().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ThingId thingId = ThingId.of("org.eclipse.ditto", "car");
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
    //Shadowing tempo di manutenzione
    public void updateMaintenanceTime(int time) {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"org.eclipse.ditto/car/things/twin/commands/modify\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/parts_maintenance/properties/engine\",\n"
                        + "  \"value\": " + time + "\n"
                        + "}").asObject());
        client.sendDittoProtocol(jsonifiableAdaptable).whenComplete((a, t) -> {
            if (a != null) {
                //System.out.println(a);
            }
            if (t != null) {
                System.out.println("sendDittoProtocol: Received throwable as response" + t);
            }
        });
    }

    //Shadowing Tempo Motore
    public void updateCarEngine(final int counter) {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"org.eclipse.ditto/car/things/twin/commands/modify\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/parts_time/properties/engine\",\n"
                        + "  \"value\": " + counter + "\n"
                        + "}").asObject());
        client.sendDittoProtocol(jsonifiableAdaptable).whenComplete((a, t) -> {
            if (a != null) {
                //System.out.println(a);
            }
            if (t != null) {
                System.out.println("sendDittoProtocol: Received throwable as response" + t);
            }
        });
    }

}
