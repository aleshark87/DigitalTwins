package connection;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.protocol.JsonifiableAdaptable;
import org.eclipse.ditto.protocol.ProtocolFactory;

public class UpdateThing {
	private DittoClient client;
	private final String namespace;
	private final String id;
	
	public UpdateThing(final DittoClient client, final String namespace, final String id) {
		this.client = client;
		this.namespace = namespace;
		this.id = id;
	}
	
	public void updateLampStatus(final boolean status) {
        JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n"
                        + "  \"topic\": \"" + namespace + "/" + id + "/things/twin/commands/modify\",\n"
                        + "  \"headers\": {\n"
                        + "    \"correlation-id\": \"<command-correlation-id>\"\n"
                        + "  },\n"
                        + "  \"path\": \"/features/status/properties/lamp-status\",\n"
                        + "  \"value\": " + status + "\n"
                        + "}").asObject());
        try {
            client.sendDittoProtocol(jsonifiableAdaptable).toCompletableFuture().join();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
