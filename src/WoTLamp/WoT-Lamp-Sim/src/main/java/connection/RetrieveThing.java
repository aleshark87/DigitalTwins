package connection;

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
                        + "  \"topic\": \"" + namespace + "/lamp/things/twin/commands/retrieve\",\n"
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
}
