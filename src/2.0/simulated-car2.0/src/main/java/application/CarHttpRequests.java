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

import org.json.JSONArray;
import org.json.JSONObject;

public class CarHttpRequests {

    private final String usernameColonPassword = "ditto:ditto";
    private final String basicAuthPayload = "Basic " + Base64.getEncoder().encodeToString(usernameColonPassword.getBytes());
    private final String payloadThingCreation = "{\n"
            + "    \"definition\": \"https://raw.githubusercontent.com/aleshark87/WoTModels/main/car.jsonld\"\n"
            + "}";
    private HttpClient client;
    
    public CarHttpRequests() {
        client = HttpClient.newHttpClient();
    }
    
    public int createThing() {
        int returnCode = -1;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/2/things/io.eclipseprojects.ditto:car"))
                    .headers("Content-Type", "application/json")
                    .headers("Authorization", basicAuthPayload)
                    .PUT(HttpRequest.BodyPublishers.ofString(payloadThingCreation))
                    .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            System.out.println(response);
            returnCode = response.statusCode();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return returnCode;
    }
    
    public List<String> getFeatureEndpoints(){
        List<JSONObject> jsonObjList = getFeaturesJson();
        System.out.println(jsonObjList.get(0));
        //JSONObject j = (jsonObjList.get(0).getJSONObject("properties").getJSONObject("engine").getJSONArray("forms").getJSONObject(3).getString("href"));
        //System.out.println(j.getString("href"));
        return null;
    }
    
    private List<JSONObject> getFeaturesJson() {
        String baseURI = "http://localhost:8080/api/2/things/io.eclipseprojects.ditto:car/features/";
        List<String> featureURIs = List.of(
                baseURI + "status",
                baseURI + "indicator-light",
                baseURI + "wear-time");
        List<JSONObject> jsonObjList = new LinkedList<>();
        try {
            for(String featureURI: featureURIs) {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(new URI(featureURI))
                        .headers("Accept", "application/td+json")
                        .headers("Authorization", basicAuthPayload)
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(req, BodyHandlers.ofString());
                JSONObject jsonObj = new JSONObject(response.body());
                jsonObjList.add(jsonObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return jsonObjList;
    }
    
}
