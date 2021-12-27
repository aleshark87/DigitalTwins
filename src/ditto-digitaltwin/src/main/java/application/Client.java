package application;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.DittoClients;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.client.configuration.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.configuration.WebSocketMessagingConfiguration;
import org.eclipse.ditto.client.live.LiveThingHandle;
import org.eclipse.ditto.client.management.ThingHandle;
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
import org.eclipse.ditto.things.model.ThingsModelFactory;

import com.neovisionaries.ws.client.WebSocket;

public class Client {
    
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
    
    public Client() {
        createAuthProvider();
        createMessageProvider();
        client = DittoClients.newInstance(messagingProvider)
                .connect()
                .toCompletableFuture()
                .join();
        //createCarThing();
        //searchThings();
        subscribeForNotification();
        Runnable task = new Runnable() {

            @Override
            public void run() {
                searchThings();
            }
            
        };
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(task, 0, 5, TimeUnit.SECONDS);
    }
    /*
     * Non funziona
    private void sendMessage() {
        final LiveThingHandle thingHandle = client.live().forId(ThingId.of("org.eclipse.ditto", "car-01"));
        client.live().message()
        .to(ThingId.of("org.eclipse.ditto", "car-01"))
        .subject("monitoring.building.fireAlert")
        .payload("Roof is on fire")
        .contentType("text/plain")
        .send();
    }*/
    
    private void subscribeForNotification() {
        try {
            client.twin().startConsumption().toCompletableFuture().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Subscribed for Twin events");
        client.twin().registerForThingChanges("my-changes", change -> {
           if (change.getAction() == ChangeAction.UPDATED) {
               int engineMinutes = getFeatureProperties(change.getThing().get(), "status", "engine_minutes");
               checkForMaintenance(engineMinutes);
           }
        });
    }
    //Questo metodo dovrebbe fare parte di un altra classe ?
    private void checkForMaintenance(final int engineMinutes) {
        System.out.println("check for Maintenance");
        if(engineMinutes > 5) {
            //IDEA: fai nuova feature (fin dall'inizio) per segnalare manutenzione
            System.out.println("Has togo into maintenance");
            updateMaintenance();
        }
    }
    
    //Forse in un altra classe ?
    private void updateMaintenance() {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"org.eclipse.ditto/car-01/things/twin/commands/modify\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/status/properties/need_maintenance\",\n"
                        + "  \"value\": true\n"
                        + "}").asObject());
        client.sendDittoProtocol(jsonifiableAdaptable).whenComplete((a, t) -> {
            if (a != null) {
                System.out.println(a);
            }
            if (t != null) {
                System.out.println("sendDittoProtocol: Received throwable as response" + t);
            }
        });
    }
    
    private int getFeatureProperties(final Thing thing, final String featureId, final String propertyId) {
        return thing.getFeatures().get().getFeature(featureId).get().getProperties().get().getValue(propertyId).get().asInt();
    }
    
    private void searchThings() {
        client.twin().search()
        .stream(queryBuilder -> queryBuilder.namespace("org.eclipse.ditto")
           .options(builder -> builder.sort(s -> s.desc("thingId")).size(1))
        )
        .forEach(foundThing -> System.out.println(getFeatureProperties(foundThing, "status", "engine_minutes")));
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
                        "          \"need_maintenance\": false\n" +
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
    
    private void deleteThing(final String thingId) {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"org.eclipse.ditto/" + thingId + "/things/twin/commands/delete\",\n"
                        + "  \"path\": \"/\"\n"
                        + "}").asObject());
        client.sendDittoProtocol(jsonifiableAdaptable).whenComplete((a, t) -> {
            if (a != null) {
                System.out.println("Deleted thing " + thingId);
            }
            if (t != null) {
                System.out.println("Couldn't delete thing " + thingId + ", because " + t);
            }
        });
        }

}
