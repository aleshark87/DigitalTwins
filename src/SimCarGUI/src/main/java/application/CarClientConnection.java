package application;

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
import org.eclipse.ditto.things.model.ThingId;

import com.neovisionaries.ws.client.WebSocket;

import controller.CarSimController;

public class CarClientConnection {
    
    private CarSimController controller;
    private AuthenticationProvider<WebSocket> authenticationProvider;
    private MessagingProvider messagingProvider;
    private DittoClient client;
    private UpdateThingProperty updateProperty;
    private RetrieveThingProperty retrieveProperty;
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
            retrieveProperty = new RetrieveThingProperty(client);
            connectionStatus = true;
        }catch(Exception e){
            System.out.println("Ditto connection not open.");
        };
        if(connectionStatus) {
            if(retrieveProperty.retrieveThing().getCode() == 200) {
                twinStatus = true;
            }
        }
        updateProperty = new UpdateThingProperty(client, this);
        
    }

    public void sendIndicatorMessage(String part) {
        //System.out.println("sending message " + part);
        ThingId thingId = ThingId.of("io.eclipseprojects.ditto", "car");
        client.live().message()
                               .to(thingId)
                               .subject("car.maintenance")
                               .payload(part)
                               .contentType("text/plain")
                               .send();
    }
    
    public UpdateThingProperty getUpdateProperty() {
        return this.updateProperty;
    }
    
    public RetrieveThingProperty getRetrieveProperty() {
        return this.retrieveProperty;
    }
    
    public CarSimController getSimController() {
        return this.controller;
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
