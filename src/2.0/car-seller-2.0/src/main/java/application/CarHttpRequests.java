package application;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Base64;

public class CarHttpRequests {

    private final String usernameColonPassword = "ditto:ditto";
    private final String basicAuthPayload = "Basic " + Base64.getEncoder().encodeToString(usernameColonPassword.getBytes());
    private final String payloadThingCreation = "{\n"
            + "    \"definition\": \"https://raw.githubusercontent.com/aleshark87/WoTModels/main/car.tm.jsonld\"\n"
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
    
}
