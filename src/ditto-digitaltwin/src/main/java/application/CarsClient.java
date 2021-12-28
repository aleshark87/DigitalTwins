package application;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.eclipse.ditto.base.model.common.HttpStatus;
import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.DittoClients;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.client.changes.ThingChange;
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
import com.andrebreves.tuple.Tuple;
import com.andrebreves.tuple.Tuple2;
import com.neovisionaries.ws.client.WebSocket;

public class CarsClient {
    
    public static void main(String[] args) {
        new CarsClient();
    }
    
    private AuthenticationProvider<WebSocket> authenticationProvider;
    private MessagingProvider messagingProvider;
    private DittoClient client;
    private MaintenanceSupervisor supervisor;
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
                                 .stream(queryBuilder -> queryBuilder.namespace("org.eclipse.ditto"))
                                 .collect(Collectors.toList());
        return list.get(0).getFeatures().get().getFeature("status").get().getProperties().get().toString();
    }
    
    private void subscribeForNotification() {
        try {
            client.twin().startConsumption().toCompletableFuture().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Subscribed for Twin events");
        
        client.twin().registerForThingChanges("car-changes", change -> {
           if (change.getAction() == ChangeAction.UPDATED) {
               //Solo per stampare meglio il testo
               var text = isNotARepetition(printThingFeatures());
               if(text.v1()) {
                   System.out.println(text.v2().get());
               }
               //Quando ricevo una notifica che cambia il tempo del motore, controllo se è necessario eseguire manutenzione
        	   if(!supervisor.getMaintenanceStatus() && getChangedPropertiesName(change).equals("engine_minutes")) {
        		   int engineMinutes = getFeatureProperties(change.getThing().get(), "status", "engine_minutes");
                   supervisor.checkForMaintenance(engineMinutes);
        	   }
        	   else {
        	       //Quando ricevo una notifica che cambia il tempo della manutenzione, controllo se è terminata
        		   int maintenanceTime = getFeatureProperties(change.getThing().get(), "status", "maintenance_time");
        		   supervisor.checkForEndMaintenance(maintenanceTime);
        	   }
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
    
    private String getChangedPropertiesName(ThingChange change) {
    	return change.getThing().get().getFeatures().get().getFeature("status").get().getProperties().get().getKeys().get(0).toString();
    }
    
    //Segnala al Thing Car che è necessario eseguire manutenzione
    void updateMaintenance(boolean value) {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"org.eclipse.ditto/car-01/things/twin/commands/modify\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/status/properties/need_maintenance\",\n"
                        + "  \"value\": " + value + "\n"
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
    
    private int getFeatureProperties(final Thing thing, final String featureId, final String propertyId) {
        return thing.getFeatures().get().getFeature(featureId).get().getProperties().get().getValue(propertyId).get().asInt();
    }
    
    //Crea il Thing Car
    private void createCarThing() {
        System.out.println("Creating Twin \"org.eclipse.ditto:car-01\"");
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n" +
                        "  \"topic\": \"org.eclipse.ditto/car-01/things/twin/commands/create\",\n" +
                        "  \"headers\": {},\n" +
                        "  \"path\": \"/\",\n" +
                        "  \"value\": {\n" +
                        "    \"thingId\": \"org.eclipse.ditto:car-01\",\n" +
                        "    \"attributes\": {\n" +
                        "      \"Data\": {\n" +
                        "        \"manufacture_place\": \"Rome\",\n" +
                        "        \"manufacture_date\": \"01-01-2021\"\n" +
                        "      }\n" +
                        "    },\n" +
                        "    \"features\": {\n" +
                        "      \"status\": {\n" +
                        "        \"properties\": {\n" +
                        "          \"engine_minutes\": 0,\n" +
                        "          \"need_maintenance\": false,\n" +
                        "          \"maintenance_time\": 0\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}").asObject());
        client.sendDittoProtocol(jsonifiableAdaptable).whenComplete((a, t) -> {
            if (a != null) {
                System.out.println(a.getPayload().getValue().get());
            }
            if (t != null) {
                System.out.println("sendDittoProtocol: Received throwable as response" + t);
            }
        });
    }
    
    //Controlla se il Twin Car è già stato creato
    private HttpStatus checkIfThingExists() {
    	
    	JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                		+ "  \"topic\": \"org.eclipse.ditto/car-01/things/twin/commands/retrieve\",\n"
                		+ "  \"headers\": {\n"
                		+ "    \"correlation-id\": \"<command-correlation-id>\"\n"
                		+ "  },\n"
                		+ "  \"path\": \"/\"\n"
                		+ "}").asObject());
    	HttpStatus p = null;
    	
        try {
            Adaptable adapt = client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture().get();
            p = adapt.getPayload().getHttpStatus().get();
            if(p.getCode() != 404) {
            	//System.out.println(adapt.getPayload().getValue().get());
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
    	JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                		+ "  \"topic\": \"org.eclipse.ditto/car-01/things/twin/commands/modify\",\n"
                		+ "  \"headers\": {\n"
                		+ "    \"correlation-id\": \"<command-correlation-id>\"\n"
                		+ "  },\n"
                		+ "  \"path\": \"/features/status\",\n"
                		+ "  \"value\": {\n"
                		+ "    \"properties\": {\n"
                		+ "      \"engine_minutes\": 0,\n"
                		+ "      \"need_maintenance\": false,\n"
                		+ "      \"maintenance_time\": 0\n"
                		+ "    }\n"
                		+ "  }\n"
                		+ " }\n"
                		).asObject());
        client.sendDittoProtocol(jsonifiableAdaptable).whenComplete((a, t) -> {
            if (a != null) {
                //System.out.println(a);
            }
            if (t != null) {
                //System.out.println("sendDittoProtocol: Received throwable as response" + t);
            }
        });
    }

}
