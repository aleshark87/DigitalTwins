package connection;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.ditto.base.model.common.HttpStatus;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.protocol.Adaptable;
import org.eclipse.ditto.protocol.JsonifiableAdaptable;
import org.eclipse.ditto.protocol.ProtocolFactory;

public class RetrieveThing {
	private DittoClient client;
	private final String namespace;
	private final String id;
	
	public RetrieveThing(final DittoClient client, final String namespace, final String id) {
		this.client = client;
		this.namespace = namespace;
		this.id = id;
	}
	
	public int retrieveThing() {
		JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"" + namespace + "/" + id + "/things/twin/commands/retrieve\",\n"
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p.getCode();
	}
	
	public Optional<Boolean> retrieveLampStatus() {
		JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"" + namespace + "/" + id + "/things/twin/commands/retrieve\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/status/properties/lamp-status\"\n"
                        + "}\n").asObject());
        Optional<Boolean> lamp_status = Optional.empty();
        try {
            CompletableFuture<Adaptable> complFuture = client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture();
            var adapt = complFuture.join();
            lamp_status = Optional.of(adapt.getPayload().getValue().get().asBoolean());
        } catch (Exception e) {
            System.out.println("Failed to retrieve lamp_status");
            lamp_status = Optional.empty();
        }
        return lamp_status;
	}
}
