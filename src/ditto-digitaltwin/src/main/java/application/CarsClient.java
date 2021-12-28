package application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

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

import com.neovisionaries.ws.client.WebSocket;

public class CarsClient {
    
    private AuthenticationProvider<WebSocket> authenticationProvider;
    private MessagingProvider messagingProvider;
    private DittoClient client;
    private MaintenanceSupervisor supervisor;
    
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
        Runnable task = new Runnable() {

            @Override
            public void run() {
                //searchThings();
            }
            
        };
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(task, 0, 3, TimeUnit.SECONDS);
    }
    
    

    private void searchThings() {
        client.twin().search()
        .stream(queryBuilder -> queryBuilder.namespace("org.eclipse.ditto")
           .options(builder -> builder.sort(s -> s.desc("thingId")).size(1))
        )
        .forEach(foundThing -> System.out.println(foundThing.getFeatures().get().getFeature("status").get().getProperties().get()));
    }
    
    private void subscribeForNotification() {
        try {
            client.twin().startConsumption().toCompletableFuture().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Subscribed for Twin events");
        client.twin().registerForThingChanges("my-changes", change -> {
           if (change.getAction() == ChangeAction.UPDATED) {
        	   if(!supervisor.getMaintenanceStatus() && getChangedPropertiesName(change).equals("engine_minutes")) {
        		   int engineMinutes = getFeatureProperties(change.getThing().get(), "status", "engine_minutes");
                   supervisor.checkForMaintenance(engineMinutes);
        	   }
        	   else {
        		   int maintenanceTime = getFeatureProperties(change.getThing().get(), "status", "maintenance_time");
        		   supervisor.checkForEndMaintenance(maintenanceTime);
        	   }
           }
        });
    }
    
    private String getChangedPropertiesName(ThingChange change) {
    	return change.getThing().get().getFeatures().get().getFeature("status").get().getProperties().get().getKeys().get(0).toString();
    }
    
    
    
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
