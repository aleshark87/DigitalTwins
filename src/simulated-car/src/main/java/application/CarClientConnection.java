package application;

import java.util.Optional;

import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.DittoClients;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.client.configuration.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.configuration.WebSocketMessagingConfiguration;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.AuthenticationProviders;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.messaging.MessagingProviders;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.protocol.JsonifiableAdaptable;
import org.eclipse.ditto.protocol.ProtocolFactory;
import org.slf4j.Logger;

import com.neovisionaries.ws.client.WebSocket;

public class CarClientConnection {
    
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
    
    public CarClientConnection() {
        createAuthProvider();
        createMessageProvider();
        client = DittoClients.newInstance(messagingProvider)
                .connect()
                .toCompletableFuture()
                .join();
        subscribeForNotification();
        //registerForMessages();
    }
    
    private void subscribeForNotification() {
        try {
            client.twin().startConsumption().toCompletableFuture().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Subscribed for Twin events");
        client.twin().registerForThingChanges("my-changes", change -> {
                System.out.println(change.getAction());
                System.out.println(change.getThing().get().getFeatures().get().getFeature("status"));
        });
    }
    /*
     * Non funziona
    private void registerForMessages() {
        System.out.println("registering for messages");
        client.live().registerForMessage(ALL_THINGS_STRING_MESSAGE, "*", String.class, message -> {
            final String subject = message.getSubject();
            final Optional<String> payload = message.getPayload();
        });
    }*/
    
    
    public void updateCarLocation(final int counter) {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom(
                        "{\n"
                        + "  \"topic\": \"org.eclipse.ditto/car-01/things/twin/commands/modify\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/status\",\n"
                        + "  \"value\": {\n"
                        + "    \"thingId\": \"org.eclipse.ditto:car-01\",\n" 
                        + "    \"properties\": {\n"
                        + "      \"engine_minutes\": " + (counter) + "\n"
                        + "    }\n"
                        + "  }\n"
                        + "}").asObject());
        client.sendDittoProtocol(jsonifiableAdaptable).whenComplete((a, t) -> {
            if (a != null) {
                //System.out.println("sendDittoProtocol: Received adaptable as response: {}" + a);
            }
            if (t != null) {
                //System.out.println("sendDittoProtocol: Received throwable as response" + t);
            }
        });
    }

}
