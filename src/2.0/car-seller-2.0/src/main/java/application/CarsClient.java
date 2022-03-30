package application;

import java.io.IOException;
import java.util.Base64;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.eclipse.ditto.base.model.common.HttpStatus;
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
import org.eclipse.ditto.protocol.Adaptable;
import org.eclipse.ditto.protocol.JsonifiableAdaptable;
import org.eclipse.ditto.protocol.ProtocolFactory;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;

import com.andrebreves.tuple.Tuple;
import com.andrebreves.tuple.Tuple2;
import com.neovisionaries.ws.client.WebSocket;

import car_model.CarFeatures;

public class CarsClient {
    
    public static void main(String[] args) {
        new CarsClient();
    }
    
    private AuthenticationProvider<WebSocket> authenticationProvider;
    private MessagingProvider messagingProvider;
    private DittoClient client;
    private MaintenanceSupervisor supervisor;
    private ThingId thingId = ThingId.of("io.eclipseprojects.ditto", "car");
    private String tmpRepetition = "first";
    
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
    
    public CarsClient() {
        createAuthProvider();
        createMessageProvider();
        client = DittoClients.newInstance(messagingProvider)
                .connect()
                .toCompletableFuture()
                .join();
        
        if(checkIfThingExists().getCode() == 404) {
        	createCarThing();
        }
        else {
        	resetThing();
        }
        
        subscribeForNotification();
        supervisor = new MaintenanceSupervisor(this);
        
    }
    
    

    private String printThingFeatures() {
        List<Thing> list = client.twin()
                                 .search()
                                 .stream(queryBuilder -> queryBuilder.namespace("io.eclipseprojects.ditto"))
                                 .collect(Collectors.toList());
        return list.get(0).getFeatures().get().getFeature(CarFeatures.PARTS_TIME.get()).get().getProperties().get().toString() + " " +
                list.get(0).getFeatures().get().getFeature(CarFeatures.PARTS_MAINTENANCE.get()).get().getProperties().get().toString();
    }
    
    private void subscribeForNotification() {
        try {
            client.twin().startConsumption().toCompletableFuture().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.twin().registerForThingChanges("car-changes", change -> {
           if (change.getAction() == ChangeAction.UPDATED) {
               //Solo per stampare meglio il testo
               var text = isNotARepetition(printThingFeatures());
               if(text.v1()) {
                   System.out.println(text.v2().get());
               }
           }
        });
        //Registrazione agli eventi generati dai modify-command 
        client.twin().registerForFeaturePropertyChanges("parts-changes", CarFeatures.PARTS_TIME.get(), change -> {
            if(change.getPath().getRoot().get().toString().equals("engine")) {
                supervisor.checkForMaintenance(change.getValue().get().asInt());
            }
        });
        client.twin().registerForFeaturePropertyChanges("maintenance-changes", CarFeatures.PARTS_MAINTENANCE.get(), change -> {
            if(change.getPath().getRoot().get().toString().equals("engine")) {
                supervisor.checkForEndMaintenance(change.getValue().get().asInt());
            }
        });
    }
    
    private Tuple2<Boolean, Optional<String>> isNotARepetition(String text) {
        //Is not a repetition
        if(tmpRepetition.equals("first")) {
            tmpRepetition = text;
            return Tuple.of(true, Optional.of(text));
        }
        else {
            //this is a repetition
            if(tmpRepetition.equals(text)) {
                return Tuple.of(false, Optional.empty());
            }
            else {
                tmpRepetition = text;
                return Tuple.of(true, Optional.of(text));
            }
        }
    }
    
    //Segnala al Thing Car che è necessario eseguire manutenzione, oppure che è finita
    void updateMaintenance(boolean value) {
        String payloadString = "";
        if(value) {
            payloadString = "DoMaintenance";
        }
        else {
            payloadString = "DoneMaintenance";
        }
        client.live().message()
                               .to(thingId)
                               .subject("supervisor.maintenance")
                               .payload(payloadString)
                               .contentType("text/plain")
                               .send();
    }
    
    //Crea il Thing Car
    private void createCarThing() {
        System.out.println("Creating Twin \"io.eclipseprojects.ditto:car\"");
        int returnCode = makeHttpRequest();
        if(returnCode == 201) {
            System.out.println("Twin io.eclipse.projects.ditto:car was created succesfully!");
        }
        else {
            System.out.println("Something happened in the creation of the twin.");
        }
    }
    
    private int makeHttpRequest() {
        String usernameColonPassword = "ditto:ditto";
        String basicAuthPayload = "Basic " + Base64.getEncoder().encodeToString(usernameColonPassword.getBytes());
        String data_raw = "{\n"
                + "    \"definition\": \"https://raw.githubusercontent.com/aleshark87/WoTModels/main/car.jsonld\"\n"
                + "}";
        int returnCode = -1;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/2/things/io.eclipseprojects.ditto:car"))
                    .headers("Content-Type", "application/json")
                    .headers("Authorization", basicAuthPayload)
                    .PUT(HttpRequest.BodyPublishers.ofString(data_raw))
                    .build();
            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            System.out.println(response);
            returnCode = response.statusCode();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return returnCode;
    }
    
    //Controlla se il Twin Car è già stato creato
    private HttpStatus checkIfThingExists() {
    	
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
            Adaptable adapt = client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture().get();
            p = adapt.getPayload().getHttpStatus().get();
            if(p.getCode() == 404) {
            	System.out.println(adapt.getPayload().getValue().get());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return p;
    }
    
    //Resetta le caratteristiche del Twin Car ad inizio simulazione
    private void resetThing() {
    //TODO
    }

}
