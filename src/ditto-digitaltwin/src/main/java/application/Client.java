package application;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

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
import org.eclipse.ditto.json.JsonFactory;
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
        searchThings();
        //deleteThing("xdk_53");
    }
    
    private void searchThings() {
        client.twin().search()
        .stream(queryBuilder -> queryBuilder.namespace("org.eclipse.ditto")
           .options(builder -> builder.sort(s -> s.desc("thingId")).size(1))
        )
        .forEach(foundThing -> System.out.println("Found thing: " + foundThing));
    }
    
    private void createCarThing() {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n" +
                        "  \"topic\": \"org.eclipse.ditto/car-01/things/twin/commands/modify\",\n" +
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
                        "          \"engine_hours\": 0,\n" +
                        "          \"brakes_consumption\": 0\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}").asObject());
        client.sendDittoProtocol(jsonifiableAdaptable).whenComplete((a, t) -> {
            if (a != null) {
                System.out.println("sendDittoProtocol: Received adaptable as response: {}" + a);
            }
            if (t != null) {
                System.out.println("sendDittoProtocol: Received throwable as response" + t);
            }
        });
    }
    
    private void createPolicy() {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("").asObject());
        client.sendDittoProtocol(jsonifiableAdaptable).whenComplete((a, t) -> {
            if (a != null) {
                System.out.println("sendDittoProtocol: Received adaptable as response: {}" + a);
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
                System.out.println("sendDittoProtocol: Received adaptable as response: {}" + a);
            }
            if (t != null) {
                System.out.println("sendDittoProtocol: Received throwable as response" + t);
            }
        });
        }

}
