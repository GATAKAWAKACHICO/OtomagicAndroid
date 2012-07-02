package jp.bstnhouse.otomagic;

public class PlayList {
	String user_id;
	String user_name;
	String user_profile_img;
	String artist_name;
	String json;
	
	public PlayList(String user_id, String user_name, String user_profile_img, String artist_name, String json){
		this.user_id = user_id;
		this.user_name = user_name;
		this.user_profile_img = user_profile_img;
		this.artist_name = artist_name;
		this.json = json;
	}
}
