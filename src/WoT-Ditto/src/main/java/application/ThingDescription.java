package application;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
			  if(obj.getString("op").equals("readproperty")) {
				  readEndPoints = Pair.with(obj.getString("href"), obj.getString("htv:methodName"));
			  }
			  if(obj.getString("op").equals("writeproperty")) {
				  writeEndPoints = Pair.with(obj.getString("href"), obj.getString("htv:methodName"));
			  }
		}
		List<Pair<String, String>> list = new LinkedList<>();
		list.add(readEndPoints); list.add(writeEndPoints);
		return list;
	}
}
