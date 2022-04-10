package application;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.javatuples.Pair;
import com.damnhandy.uri.template.UriTemplate;

public class HttpThingRequester {

    private final String basicAuthPayload = "Basic " + Base64.getEncoder().encodeToString(("ditto:ditto").getBytes());
    private HttpClient client;
    private ThingDescription thingDescript;
    private List<Map<String, Object>> uriVariables;
    private String uriBase = "http://localhost:8080/api/2/things/projects.wot.ditto:lamp";
    private String responseThing; 
    
    public HttpThingRequester() {
        client = HttpClient.newHttpClient();
        if(getThingDescription() == 200) {
			System.out.println("Thing Already exists, setting thing description variables");
			setThingDescription();
		}
		else {
			System.out.println("Creating Thing");
			createThing();
			getAndSetThingDescription();
		}
    }
    
    public int createThing() {
    	List<Pair<String, String>> headersList = 
    				Stream.of(Pair.with("Content-Type", "application/json"), Pair.with("Authorization", basicAuthPayload))
    				.collect(Collectors.toList());
    	String body = "{\n"
                + "    \"definition\": \"https://raw.githubusercontent.com/aleshark87/WoTModels/main/lamp/lamp.tm.jsonld\"\n"
                + "}";
        var response = makeHttpRequest(uriBase, false, Optional.empty(), headersList, "PUT", Optional.of(BodyPublishers.ofString(body)));
        if(response.getValue0() == 200 || response.getValue0() == 201 || response.getValue0() == 204) {
        	System.out.println("Thing projects.wot.ditto:lamp created succesfully!");
        }
        return response.getValue0();
    }
    
    public int getThingDescription() {
    	List<Pair<String, String>> headersList = 
				List.of(Pair.with("Accept", "application/td+json"), Pair.with("Authorization", basicAuthPayload));
    	var resp = makeHttpRequest(uriBase, false, Optional.empty(), headersList, "GET", Optional.empty());
    	responseThing = resp.getValue1();
    	return resp.getValue0();
    }
    
    public void getAndSetThingDescription() {
    	getThingDescription();
    	setThingDescription();
    }
    
    public void setThingDescription() {
    	thingDescript = new ThingDescription(responseThing);
    	List<String> listFeatureHref = thingDescript.getFeatureHref();
    	List<String> listFeatureDesc = new LinkedList<>();
    	for(String feat: listFeatureHref) {
    		List<Pair<String, String>> headersList = 
    				List.of(Pair.with("Accept", "application/td+json"), Pair.with("Authorization", basicAuthPayload));
    		var responseFeature = makeHttpRequest((uriBase + feat), false, Optional.empty(), headersList, "GET", Optional.empty());
    		listFeatureDesc.add(responseFeature.getValue1());
    	}
    	thingDescript.setFeatureDescription(listFeatureDesc);
    	uriVariables = thingDescript.getURIVariables();
    }
    
    public Optional<Boolean> getLampStatus() {
    	var endpoint = thingDescript.getLampStatusFeatureEndpoint("lamp-status").get(0);
    	String uri = uriBase + thingDescript.getFeatureHref().get(0) + endpoint.getValue0();
    	List<Pair<String, String>> headersList = List.of(Pair.with("Content-Type", "application/json"), Pair.with("Authorization", basicAuthPayload));
    	var response = makeHttpRequest(
    			uri, true, Optional.empty(), headersList, endpoint.getValue0(), Optional.empty());
    	if(response.getValue0() == 200) {
    		return Optional.of(Boolean.parseBoolean(response.getValue1()));
    	}
    	else {
    		return Optional.empty();
    	}
    }
    
    public int invokeLampSwitchAction(final boolean state) {
    	var endpoint = thingDescript.getActionEndpoint("switch-lamp");
    	List<Pair<String, String>> headersList = List.of(Pair.with("Content-Type", "application/json"), Pair.with("Authorization", basicAuthPayload));
    	String body = String.valueOf(state);
    	Map<String, Object> uriVar = Map.of("timeout", 0);
    	Map<String, Object> uriVar2 = Map.of("responseRequired", false);
    	
    	var response = makeHttpRequest(
    			uriBase + endpoint.getValue0(), true, Optional.of(List.of(uriVar, uriVar2)), headersList, endpoint.getValue1(), Optional.of(BodyPublishers.ofString(body)));
    	return response.getValue0();
    }
    
    private Pair<Integer, String> makeHttpRequest(String URI, boolean explodeURI, Optional<List<Map<String, Object>>> uriVar, 
    												List<Pair<String, String>> headersList, String requestType, Optional<BodyPublisher> body) {
    	HttpResponse<String> response = null;
        try {
        	if(explodeURI) {
        		if(uriVar.isPresent()) {
        			URI = customExplodeURI(URI, uriVar.get());
        		}
        		else {
        			URI = explodeURI(URI);
        		}
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
        			  builder = builder.method("PATCH", body.get());
        		  }
        		  else {
        			  System.out.println("PATCH requests require body");
        		  }
        	  case "POST":
        		  if(body.isPresent()) {
        			  builder = builder.POST(body.get());
        		  }
        		  else {
        			  System.out.println("POST requests require body");
        		  }
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
    	if(uriToExplode.contains("response-required")) {
    		uriToExplode = uriToExplode.replace("-r", "R");
    	}
    	UriTemplate template = UriTemplate.fromTemplate(uriToExplode);
    	for(var uriVariable: uriVariables) {
    		template = template.set(uriVariable);
    	}
    	return template.expand();
    }
   
    private String customExplodeURI(String uriToExplode, List<Map<String, Object>> uriVar) {
    	if(uriToExplode.contains("response-required")) {
    		uriToExplode = uriToExplode.replace("-r", "R");
    	}
    	UriTemplate template = UriTemplate.fromTemplate(uriToExplode);
    	for(var uriVariable: uriVar) {
    		template = template.set(uriVariable);
    	}
    	return template.expand();
    }
}
