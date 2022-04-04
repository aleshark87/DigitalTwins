package application;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ThingDescription {
	
	private JSONObject jsonObj;
	
	public ThingDescription(String thingDescription) {
		try {
			this.jsonObj = new JSONObject(thingDescription);
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return List of ReadProperty endpoint and WriteProperty endpoint
	 */
	public List<Pair<String, String>> getAttributeEndPoint() {
		Pair<String, String> readEndPoints = null;
		Pair<String, String> writeEndPoints = null;
		JSONArray opArray = jsonObj.getJSONObject("properties").getJSONObject("serialNo").getJSONArray("forms");
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

		return Stream.of(readEndPoints, writeEndPoints).collect(Collectors.toList());
	}
	
	/**
	 * 
	 * @return List of URI Variables contained in the Description
	 */
	public List<Map<String, Object>> getURIVariables(){
		Map<String, Object> channel = Map.of("channel", jsonObj.getJSONObject("uriVariables").getJSONObject("channel").getString("default"));
		Map<String, Object> timeout = Map.of("timeout",Integer.toString(jsonObj.getJSONObject("uriVariables").getJSONObject("timeout").getInt("default")));
		Map<String, Object> response_required = Map.of("response-required", jsonObj.getJSONObject("uriVariables").getJSONObject("response-required").getBoolean("default"));
		return Stream.of(channel, timeout, response_required).collect(Collectors.toList());
	}
}
