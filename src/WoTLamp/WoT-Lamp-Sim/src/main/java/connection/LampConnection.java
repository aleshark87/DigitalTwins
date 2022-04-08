package connection;

import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.DittoClients;
import org.eclipse.ditto.client.configuration.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.configuration.WebSocketMessagingConfiguration;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.AuthenticationProviders;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.messaging.MessagingProviders;

import com.neovisionaries.ws.client.WebSocket;
import controller.LampSimController;

public class LampConnection {
	private AuthenticationProvider<WebSocket> authenticationProvider;
    private MessagingProvider messagingProvider;
    private DittoClient client;
    //private UpdateThingProperty updateProperty;
    private RetrieveThing retrieveProperty;
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
            retrieveProperty = new RetrieveThing(client, namespace, id);
            connectionStatus = true;
        }catch(Exception e){
            System.out.println("Ditto connection not open.");
        };
        if(connectionStatus) {
            if(retrieveProperty.retrieveThing() == 200) {
                twinStatus = true;
            }
        }
        //updateProperty = new UpdateThingProperty(client, this);
        
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
