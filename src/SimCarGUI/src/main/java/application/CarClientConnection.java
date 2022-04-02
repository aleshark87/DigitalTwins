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
            connectionStatus = true;
        }catch(Exception e){
            System.out.println("Ditto connection not open.");
        };
        if(connectionStatus) {
            if(retrieveThing().getCode() == 200) {
                twinStatus = true;
            }
        }
        updateProperty = new UpdateThingProperty(client, this);
        retrieveProperty = new RetrieveThingProperty(client);
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
