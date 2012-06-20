package jp.bstnhouse.otomagic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestItunes {
	public void requestMusicApi(){
		
	}
	
	public String parseApiArtistName(String json, int resNum){
		try {
			JSONObject rootObject = new JSONObject(json);
			JSONArray resultsArray = rootObject.getJSONArray("results");
			JSONObject resultObject = resultsArray.getJSONObject(resNum);
			String trackName = resultObject.getString("artistName");
			return trackName;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public String parseApiResponse(String json, int resNum){
		try {
			JSONObject rootObject = new JSONObject(json);
			JSONArray resultsArray = rootObject.getJSONArray("results");
			JSONObject resultObject = resultsArray.getJSONObject(resNum);
			String trackName = resultObject.getString("trackName");
			return trackName;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public int[] parseApiResponse2(){
		int [] a = null;
		return a;
	}
	
	public String parseApiTestImageUrl(String json, int resNum){
		try {
			JSONObject rootObject = new JSONObject(json);
			JSONArray resultsArray = rootObject.getJSONArray("results");
			JSONObject resultObject = resultsArray.getJSONObject(resNum);
			String imgUrl = resultObject.getString("artworkUrl100");
			return imgUrl;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public String parseApiTesPreviewUrl(String json, int resNum){
		try {
			JSONObject rootObject = new JSONObject(json);
			JSONArray resultsArray = rootObject.getJSONArray("results");
			JSONObject resultObject = resultsArray.getJSONObject(resNum);
			String previewUrl = resultObject.getString("previewUrl");
			return previewUrl;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
