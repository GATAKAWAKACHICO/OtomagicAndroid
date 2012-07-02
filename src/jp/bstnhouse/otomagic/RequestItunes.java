package jp.bstnhouse.otomagic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestItunes {
	public void requestMusicApi(){
		
	}
	
	public int parseApiResultsNum(String json){
		JSONObject rootObject;
		JSONArray resultsArray = null;
		try {
			rootObject = new JSONObject(json);
			resultsArray = rootObject.getJSONArray("results");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		return resultsArray.length();
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
	
	public String parseApiTrackName(String json, int resNum){
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
	
	public String parseApiImageUrl(String json, int resNum){
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
	
	public String parseApiPreviewUrl(String json, int resNum){
		String previewUrl = null;
		try {
			JSONObject rootObject = new JSONObject(json);
			JSONArray resultsArray = rootObject.getJSONArray("results");
			JSONObject resultObject = resultsArray.getJSONObject(resNum);
			if(resultObject.has("previewUrl")){
				previewUrl = resultObject.getString("previewUrl");
			}else{
				previewUrl = null;
			}
			//previewUrl = resultObject.getString("previewUrl");
			return previewUrl;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return previewUrl;
		}
	}
	
	public String parseApiCollectionViewUrl(String json, int resNum){
		try {
			JSONObject rootObject = new JSONObject(json);
			JSONArray resultsArray = rootObject.getJSONArray("results");
			JSONObject resultObject = resultsArray.getJSONObject(resNum);
			String collectionViewUrl = resultObject.getString("collectionViewUrl");
			if(resultObject.has("collectionViewUrl")){
				collectionViewUrl = resultObject.getString("collectionViewUrl");
			}else{
				collectionViewUrl = null;
			}
			return collectionViewUrl;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
