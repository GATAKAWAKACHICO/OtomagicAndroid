package jp.bstnhouse.otomagic;

import java.util.ArrayList;

public class UserArtist {
	String user_id;
	String user_name;
	String artist_id;
	String artist_name;
	ArrayList<Integer> artist_nums;
	ArrayList<String> artist_names;
	
	public UserArtist(String user_id, String user_name, String artist_name){
		this.user_id = user_id;
		this.user_name = user_name;
		this.artist_name = artist_name;
	}
	
	public String getUserId(){
		return user_id;
	}
	
	public String getUserName(){
		return user_name;
	}
	
}
