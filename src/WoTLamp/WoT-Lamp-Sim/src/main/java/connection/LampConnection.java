package connection;

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
import org.eclipse.ditto.things.model.ThingId;

import com.neovisionaries.ws.client.WebSocket;
import controller.LampSimController;

public class LampConnection {
	private AuthenticationProvider<WebSocket> authenticationProvider;
    private MessagingProvider messagingProvider;
    private DittoClient client;
    private UpdateThing update;
    private RetrieveThing retrieve;
    private LampSimController controller;
    private boolean connectionStatus = false;
    private boolean twinStatus = false;
    private final String namespace = "projects.wot.ditto";
    private final String id = "lamp";
    
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
    
    public LampConnection(final LampSimController controller) {
        System.out.println("car client connection starting.\n");
        this.controller = controller;
        createAuthProvider();
        createMessageProvider();
        try {
            client = DittoClients.newInstance(messagingProvider)
                    .connect()
                    .toCompletableFuture()
                    .join();
            retrieve = new RetrieveThing(client, namespace, id);
            connectionStatus = true;
        }catch(Exception e){
            System.out.println("Ditto connection not open.");
        };
        if(connectionStatus) {
            if(retrieve.retrieveThing() == 200) {
                twinStatus = true;
            }
        }
        update = new UpdateThing(client, namespace, id);
        subscribeForMessages();
    }
    
    private void subscribeForMessages() {
    	System.out.println("Subscribing for messages");
        try {
            client.live().startConsumption().toCompletableFuture().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ThingId thingId = ThingId.of(namespace, id);
        final LiveThingHandle thingIdLive = client.live().forId(thingId);
        // Register for *all* messages of a *specific* thing and provide payload as String
        thingIdLive.registerForMessage("msg_maintenance", "switch-lamp", String.class, message -> {
            final Optional<String> payload = message.getPayload();
            if(payload.isPresent()) {
            	System.out.println("Message from client: switch-lamp " + payload.get());
            	controller.getLampSim().setLamp(Boolean.parseBoolean(payload.get()));
            }
        });
    }
    
    public RetrieveThing getRetrieveThing() {
		return retrieve;
    }
    
    public UpdateThing getUpdateThing() {
    	return update;
    }
    
    public DittoClient getDittoClient() {
    	return this.client;
    }
    
    public String getNamespace() {
    	return this.namespace;
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
