package application;


import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ThingDescription {
	
	private JSONObject jsonObjThing;
	private List<JSONObject> jsonObjFeatures;
	
	/**
	 * 
	 * @param Thing Description viene usato dal Client. La implementazione della lampada fa lo shadowing senza usarlo.
	 */
	
	public ThingDescription(String thingDescription) {
		try {
			this.jsonObjThing = new JSONObject(thingDescription);
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void setFeatureDescription(List<String> featureDescription) {
		this.jsonObjFeatures = new LinkedList<>();
		for(int i = 0; i < featureDescription.size(); i++) {
			try {
				this.jsonObjFeatures.add(new JSONObject(featureDescription.get(i)));
			} catch(JSONException je) {
				je.printStackTrace();
			}
		}
	}
	
	public List<String> getFeatureHref(){
		List<String> featureHrefList = new LinkedList<>();
		JSONArray relArr = jsonObjThing.getJSONArray("links");
		for(int i = 0; i < relArr.length(); i++) {
			JSONObject obj = relArr.getJSONObject(i);
			try {
				if(obj.getString("rel").equals("item")) {
					featureHrefList.add(obj.getString("href"));
				}
			} catch(JSONException je) {
				
			}
		}
		return featureHrefList;
	}
	/**
	 * 
	 * @return List of ReadProperty endpoint and WriteProperty endpoint
	 */
	public List<Pair<String, String>> getAttributeEndPoint() {
		Pair<String, String> readEndPoints = null;
		Pair<String, String> writeEndPoints = null;
		JSONArray opArray = jsonObjThing.getJSONObject("properties").getJSONObject("serialNo").getJSONArray("forms");
		for (int i = 0; i < opArray.length(); i++) {
			  JSONObject obj = opArray.getJSONObject(i);
			  try {
				  if(obj.getString("op").equals("readproperty")) {
					  readEndPoints = Pair.with(obj.getString("href"), obj.getString("htv:methodName"));
				  }
				  if(obj.getString("op").equals("writeproperty")) {
					  writeEndPoints = Pair.with(obj.getString("href"), obj.getString("htv:methodName"));
				  }
			  } catch(JSONException je) {
				  
			  }
		}

		return List.of(readEndPoints, writeEndPoints);
	}
	
	/**
	 * 
	 * @return List of ReadProperty endpoint and WriteProperty endpoint
	 */
	public List<Pair<String, String>> getLampStatusFeatureEndPoint(){
		Pair<String, String> readEndPoints = null;
		Pair<String, String> writeEndPoints = null;
		
		JSONArray opArray = jsonObjFeatures.get(0).getJSONObject("properties").getJSONObject("lamp-status").getJSONArray("forms");
		for(int i = 0; i < opArray.length(); i++) {
			JSONObject obj = opArray.getJSONObject(i);
			  try {
				  if(obj.getString("op").equals("readproperty")) {
					  readEndPoints = Pair.with(obj.getString("href"), obj.getString("htv:methodName"));
				  }
				  if(obj.getString("op").equals("writeproperty")) {
					  writeEndPoints = Pair.with(obj.getString("href"), obj.getString("htv:methodName"));
				  }
			  } catch(JSONException je) {
				  
			  }
		}
		
		return List.of(readEndPoints, writeEndPoints);
	}
	
	/**
	 * 
	 * @return List of URI Variables contained in the Description
	 */
	public List<Map<String, Object>> getURIVariables(){
		//https://github.com/damnhandy/Handy-URI-Templates/issues/73
		Map<String, Object> channel = Map.of("channel", jsonObjThing.getJSONObject("uriVariables").getJSONObject("channel").getString("default"));
		Map<String, Object> timeout = Map.of("timeout",Integer.toString(jsonObjThing.getJSONObject("uriVariables").getJSONObject("timeout").getInt("default")));
		Pair<String, Object> response_required = Pair.with("response-required", jsonObjThing.getJSONObject("uriVariables").getJSONObject("response-required").getBoolean("default"));
		String response = response_required.getValue0().replace("-r", "R");
		Map<String, Object> response_requiredMap = Map.of(response, response_required.getValue1());
		return Stream.of(channel, timeout, response_requiredMap).collect(Collectors.toList());
	}
	
}
