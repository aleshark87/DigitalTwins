package application;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.javatuples.Pair;

import com.damnhandy.uri.template.UriTemplate;

public class HttpRequester {

    private final String basicAuthPayload = "Basic " + Base64.getEncoder().encodeToString(("ditto:ditto").getBytes());
    private HttpClient client;
    private ThingDescription thingDescript;
    private List<Map<String, Object>> uriVariables;
    private String uriBase = "http://localhost:8080/api/2/things/projects.wot.ditto:lamp";
    
    public HttpRequester() {
        client = HttpClient.newHttpClient();
        //System.out.println(createThing());
        getThingDescription();
        setSerialNumber();
    }
    
    public int createThing() {
    	List<Pair<String, String>> headersList = 
    				Stream.of(Pair.with("Content-Type", "application/json"), Pair.with("Authorization", basicAuthPayload))
    				.collect(Collectors.toList());
    	String body = "{\n"
                + "    \"definition\": \"https://raw.githubusercontent.com/aleshark87/WoTModels/main/lamp/Lamp.jsonld\"\n"
                + "}";
        var response = makeHttpRequest(uriBase, false, headersList, "PUT", Optional.of(BodyPublishers.ofString(body)));
        return response.getValue0();
    }
    
    public int getThingDescription() {
    	List<Pair<String, String>> headersList = 
				Stream.of(Pair.with("Accept", "application/td+json"), Pair.with("Authorization", basicAuthPayload))
				.collect(Collectors.toList());
    	var response = makeHttpRequest(uriBase, false, headersList, "GET", Optional.empty());
    	
    	thingDescript = new ThingDescription(response.getValue1());
    	uriVariables = thingDescript.getURIVariables();
    	return response.getValue0();
    }
    
    public int setSerialNumber() {
    	var endpoint = thingDescript.getAttributeEndPoint().get(1);
    	
    	return 0;
    }
    
    private Pair<Integer, String> makeHttpRequest(String URI, boolean explodeURI, List<Pair<String, String>> headersList, String requestType, Optional<BodyPublisher> body) {
    	HttpResponse<String> response = null;
        try {
        	if(explodeURI) {
        		URI = explodeURI(URI);
        	}
        	HttpRequest.Builder builder = HttpRequest.newBuilder().uri(new URI(URI));	
        	for(int i = 0; i < headersList.size(); i++) {
        		builder = builder.header(headersList.get(i).getValue0(), headersList.get(i).getValue1());
        	}
        	switch(requestType) {
        	  case "GET":
        	    builder = builder.GET();
        	    break;
        	  case "PUT":
        		if(body.isPresent()) {
        			builder = builder.PUT(body.get());
        		}
        		else {
        			System.out.println("PUT requests require body");
        		}
        	    break;
        	  case "PATCH":
        		  if(body.isPresent()) {
        			  //https://stackoverflow.com/questions/58841919/java-11-httprequest-with-patch-method
        			  //builder = builder.PA
        		  }
        	  default:
        		  throw new Exception("Requesttype not found");
        	}
        	HttpRequest req = builder.build();
        	response = client.send(req, BodyHandlers.ofString());
        } catch(Exception e) {
        	System.out.println("HttpRequest raised Exception.");
        	e.printStackTrace();
        }
        
        return Pair.with(response.statusCode(), response.body());
    }
    
    private String explodeURI(String uriToExplode) {
    	UriTemplate template = UriTemplate.fromTemplate(uriToExplode);
    	for(var uriVariable: uriVariables) {
    		template = template.set(uriVariable);
    	}
    	return template.expand();
    }
   
    
}
