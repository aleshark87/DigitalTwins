package application;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.DittoClients;
import org.eclipse.ditto.client.changes.ThingChange;
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
        try {
            client.live().startConsumption().toCompletableFuture().get();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        subscribeForMessages();
    }
    
    private void subscribeForMessages() {
        ThingId thingId = ThingId.of("org.eclipse.ditto", "car-01");
        final LiveThingHandle thingId2 = client.live().forId(thingId);
        // Register for *all* messages of a *specific* thing and provide payload as String
        thingId2.registerForMessage("MsgRegistration", "*", String.class, message -> {
            final String subject = message.getSubject();
            final Optional<String> payload = message.getPayload();
            System.out.println(payload.get());
        });
    }
    
    private void subscribeForNotification() {
        /*
        client.twin().registerForThingChanges("my-changes", change -> {
                System.out.println(getMaintenanceStatus(change));
                //Esegue la manutenzione fermandosi quando lo notifica
                if(getMaintenanceStatus(change) == true) {
                    controller.getCarSimulation().maintenance();
                }
                //Se viene notificata la fine della manutenzione, Thing Car riparte
                if(getMaintenanceStatus(change) == false) {
                    controller.getCarSimulation().maintenanceDone();
                    controller.getCarSimulation().startCar();
                }
        });
        */
    }
    
    //Shadowing tempo di manutenzione
    public void updateMaintenanceTime(int time) {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"org.eclipse.ditto/car-01/things/twin/commands/modify\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/status/properties/maintenance_time\",\n"
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
                        + "  \"topic\": \"org.eclipse.ditto/car-01/things/twin/commands/modify\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/status/properties/engine_minutes\",\n"
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
