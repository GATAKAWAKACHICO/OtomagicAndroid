package jp.bstnhouse.otomagic;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestOtomagicData {
	private OtmAppConf otm_app_conf = new OtmAppConf();
	static List<Friend> tmpDataList = new ArrayList<Friend>();
	
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
}
