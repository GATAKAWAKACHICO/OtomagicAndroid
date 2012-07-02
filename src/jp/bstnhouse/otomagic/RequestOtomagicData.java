package jp.bstnhouse.otomagic;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class RequestOtomagicData {
	private ConfOtmApp otm_app_conf = new ConfOtmApp();
	static List<Friend> tmpDataList = new ArrayList<Friend>();
	
	public boolean isStatusOk(String json){
		JSONObject rootObject;
		try {
			rootObject = new JSONObject(json);
			String status = rootObject.getString("status");
			if(status.equals("ok")) {
				return true;
			}else{
				return false;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}	
	}
	
	public String getUserListRequestUrl(List<Friend> dataList){
		String url = otm_app_conf.OTOMAGIC_API_ROOT_URL + otm_app_conf.OTOMAGIC_API_USERS_URL;
  	  	String id = null;
  	  	for(int i = 0; i < dataList.size(); i++){
  		  url += "facebook_id[]=";
  		  id = dataList.get(i).id;
  		  url += id + "&";
  	  	}
		return url;		
	}
	
	public String getUserListRequestUrlLimitedNumber(List<Friend> dataList, int offset_start, int offset_end){
		String url = otm_app_conf.OTOMAGIC_API_ROOT_URL + otm_app_conf.OTOMAGIC_API_USERS_URL;
  	  	String id = null;
  	  	for(int i = offset_start; i < offset_end; i++){
  		  url += "facebook_id[]=";
  		  id = dataList.get(i).id;
  		  url += id + "&";
  	  	}
		return url;		
	}
	
	public List<Friend> getUserList(String json, List<Friend> dataList, String error_msg){
		try {
			JSONArray usersArray = new JSONArray(json);
			if(usersArray.length() != 0){
				for(int i = 0; i < usersArray.length(); i++){
					JSONObject userObject = usersArray.getJSONObject(i);
					JSONObject userObject_ = userObject.getJSONObject("user");
					String id = userObject_.getString("facebook_id");
					String name = userObject_.getString("facebook_name");
					String image_url = "http://graph.facebook.com/"+ id +"/picture";
					dataList.add(new Friend(id, name, image_url, false));
				}
			}else{
				String id = "0000000";
				String name = error_msg;
				String image_url = "a";
				dataList.add(new Friend(id, name, image_url, false));
			}
			return dataList;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Friend> moveUserList(List<Friend> tmpDataList, List<Friend> dataList){
		for (int i = 0; i < tmpDataList.size(); i++){
			dataList.add(tmpDataList.get(i));
		}
		return dataList;
	}
	
	public String getUserArtistRequestUrl(List<UserArtist> dataList){
		String url = otm_app_conf.OTOMAGIC_API_ROOT_URL + otm_app_conf.OTOMAGIC_API_USER_ARTISTS;
		String id = null;
		for (int i = 0; i < dataList.size(); i++){
			if(i == 0){
				url += "users[organizer]=";
				id = dataList.get(i).user_id;
				url += id + "&";
			}else{
				url += "users[ids][]=";
				id = dataList.get(i).user_id;
				url += id + "&";
			}
		}
		return url;
	}
	
	public List<UserArtist> parseApiUserArtist(String json, List<UserArtist> UADataList){
		List<UserArtist> tmpUADataList = new ArrayList<UserArtist>();
		String json_user_id = null;
		String json_user_name = null;
		String json_artist_name = null;
		try {
			JSONObject rootObject = new JSONObject(json);
			JSONArray resultsArray = rootObject.getJSONArray("data");
			for (int i = 0; i < resultsArray.length(); i++){
				JSONArray userArtistArray = resultsArray.getJSONArray(i);
				Log.d("userArtistArray", userArtistArray.toString());
				if(userArtistArray.length() != 0){
					for(int j = 0; j < userArtistArray.length(); j++){
						JSONObject userArtistArrayObject = userArtistArray.getJSONObject(j);
						JSONObject userArtistObject = userArtistArrayObject.getJSONObject("user_artist");
						json_user_id =  userArtistObject.getString("facebook_id");
						json_user_name = userArtistObject.getString("facebook_name");
						json_artist_name = userArtistObject.getString("artist_name");
						tmpUADataList.add(new UserArtist(json_user_id, json_user_name, json_artist_name));
					} 
				}else{
					json_user_id = UADataList.get(i).user_id;
					json_user_name = UADataList.get(i).user_name;
					json_artist_name = "___";
					tmpUADataList.add(new UserArtist(json_user_id, json_user_name, json_artist_name));
				}
			}
			//Log.d("tmpUADataList(0)", tmpUADataList.get(0).user_name.toString() + ":" + tmpUADataList.get(0).artist_name.toString());
			//Log.d("tmpUADataList(1)", tmpUADataList.get(1).user_name.toString() + ":" + tmpUADataList.get(1).artist_name.toString());
			//Log.d("tmpUADataList(2)", tmpUADataList.get(2).user_name.toString() + ":" + tmpUADataList.get(2).artist_name.toString());
			//Log.d("tmpUADataList(3)", tmpUADataList.get(3).user_name.toString() + ":" + tmpUADataList.get(3).artist_name.toString());
			return tmpUADataList;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d("JSONException:", e.toString());
			e.printStackTrace();
			return null;
		}
	}
	
}
