package jp.bstnhouse.otomagic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class MakePlayList {
	String user_id;
	ArrayList<String> user_ids;
	String user_name;
	ArrayList<String> user_names;
	String artist_name;
	ArrayList<String> artist_names;
	RequestItunes itunes = new RequestItunes();
	
	public List<PlayList> initPlayList(List<UserArtist> UADataList){
		List<PlayList> tmpPlayList = new ArrayList<PlayList>();
		for (int i = 0; i < UADataList.size(); i++){
			tmpPlayList.add(new PlayList(
					UADataList.get(i).user_id, 
					UADataList.get(i).user_name, 
					"http://graph.facebook.com/" + UADataList.get(i).user_id + "/picture",
					UADataList.get(i).artist_name, 
					null
					));
		}
		return tmpPlayList;		
	}
	
	public List<PlayList> getPlayList(List<PlayList> playList, String json){
		List<PlayList> tmpPlayList = new ArrayList<PlayList>();
		String artistName = null, trackName = null, imgUrl = null, previewUrl = null, collectionViewUrl = null;
		
		//既存のプレイリストを保存しておく
		/*
		if(playList.size() != 0){
			for (int i = 0; i < playList.size(); i++){
				tmpPlayList.add(playList.get(i));
			}
		}*/
		
		if(json != null){
			//通信して新たなAPIを取得したとき
			//jsonで新たに取得したAPIを保存
			try {
				JSONObject rootObject = new JSONObject(json);
				JSONArray resultsArray = rootObject.getJSONArray("results");
				Log.d("jsonで新たに取得したAPIを保存","");
				/*
				for (int i = 0; i < resultsArray.length(); i++){
					artistName = itunes.parseApiArtistName(json, i);
					trackName = itunes.parseApiTrackName(json, i);
					previewUrl = itunes.parseApiPreviewUrl(json, i);
					imgUrl = itunes.parseApiImageUrl(json, i);
					collectionViewUrl = itunes.parseApiCollectionViewUrl(json, i);
					tmpPlayList.add(new PlayList(
								playList.get(i).user_id, 
								playList.get(i).user_name, 
								playList.get(i).user_profile_img, 
								artistName, 
								trackName,
								previewUrl,
								imgUrl, 
								collectionViewUrl));
				}*/
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return tmpPlayList;
			}
			
		}else{
			//通信せずプレイリストの中の曲を聞くとき
		}		
		return tmpPlayList;		
	}
	
	public synchronized void getPlayList(ArrayList<UserArtist> UAList){
		//Collections.shuffle(UAList);
		
	}
	
	public ArrayList<PlayList> getPlayListFirst(ArrayList<UserArtist> UAList){
		return null;
	}
	
}
