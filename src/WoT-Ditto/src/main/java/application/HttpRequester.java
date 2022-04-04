package application;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import org.javatuples.Pair;

import com.damnhandy.uri.template.UriTemplate;

public class HttpRequester {

	private final String usernameColonPassword = "ditto:ditto";
    private final String basicAuthPayload = "Basic " + Base64.getEncoder().encodeToString(usernameColonPassword.getBytes());
    private final String payloadThingCreation = "{\n"
            + "    \"definition\": \"https://raw.githubusercontent.com/aleshark87/WoTModels/main/lamp/Lamp.jsonld\"\n"
            + "}";
    private HttpClient client;
    private ThingDescription thingDescript;
    
    public HttpRequester() {
        client = HttpClient.newHttpClient();
        getThing();
        List<Pair<String, String>> headersList = new LinkedList<>(); 
        headersList.add(Pair.with("Accept", "application/td+json"));
        //headersList.add(Pair.with("Authorization", basicAuthPayload));
        
        System.out.println(makeHttpRequest(
        		"https://raw.githubusercontent.com/aleshark87/WoTModels/main/.tm.jsonld", headersList, "GET"));
    }
    
    
    public Pair<Integer, String> makeHttpRequest(String URI, List<Pair<String, String>> headersList, String requestType) {
    	HttpResponse<String> response = null;
        try {
        	HttpRequest.Builder builder = HttpRequest.newBuilder().uri(new URI(URI));	
        	for(int i = 0; i < headersList.size(); i++) {
        		builder = builder.header(headersList.get(i).getValue0(), headersList.get(i).getValue1());
        	}
        	HttpRequest req = builder.GET().build();
        	response = client.send(req, BodyHandlers.ofString());
        } catch(Exception e) {
        	e.printStackTrace();
        	System.out.println("HttpRequest raised Exception.");
        	e.printStackTrace();
        }
        
        return Pair.with(response.statusCode(), response.body());
    }
    public int createThing() {
        int returnCode = -1;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/2/things/projects.wot.ditto:lamp"))
                    .headers("Content-Type", "application/json")
                    .headers("Authorization", basicAuthPayload)
                    .PUT(HttpRequest.BodyPublishers.ofString(payloadThingCreation))
                    .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            System.out.println(response);
            returnCode = response.statusCode();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return returnCode;
    }
    
    public void getThing() {
    	int returnCode = -1;
    	try {
    		HttpRequest req = HttpRequest.newBuilder()
    					.uri(new URI("http://localhost:8080/api/2/things/projects.wot.ditto:lamp"))
    					.headers("Accept", "application/td+json")
                        .headers("Authorization", basicAuthPayload)
                        .GET()
                        .build();
    		HttpResponse<String> response = client.send(req, BodyHandlers.ofString());
    		//System.out.println(response.body());
    		thingDescript = new ThingDescription(response.body());
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    private HttpRequest getRequestGET(String endpoint) {
    	HttpRequest req = null;
    	try {
			req = HttpRequest.newBuilder()
					.uri(new URI("http://localhost:8080/api/2/things/projects.wot.ditto:lamp" + endpoint))
					.headers("Accept", "application/json")
			        .headers("Authorization", basicAuthPayload)
			        .GET()
			        .build();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return req;
    }
    
    private String explodeURI(String uriToExplode) {
    	String result = UriTemplate.fromTemplate("/attributes/serialNo{?channel,timeout}")
    	                           .set("channel", 5)
    	                           .set("timeout", 7)
    	                           .expand();
    	return result;
    }
    
    public void getAttribute() {
    	System.out.println(explodeURI("cacca"));
    	/*
    	if (!(thingDescript == null)) {
    		List<Pair<String,String>> endpoint = thingDescript.getAttributeEndPoint();
        	System.out.println(endpoint);
    	}*/
    	
    	
    	/*
    	HttpRequest req = getRequestGET(endpoint.get(0).getValue0().split("{")[0]);
    	HttpResponse<String> response;
		try {
			response = client.send(req, BodyHandlers.ofString());
			System.out.println(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
    }
    
}
