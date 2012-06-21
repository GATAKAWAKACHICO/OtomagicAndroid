package jp.bstnhouse.otomagic;

public class Friend{
	String id;
	String name;
	String image_url;
	Boolean check;
	  
	public Friend(String id, String name, String image_url, Boolean check){
		this.id = id;
		this.name = name;
	    this.image_url = image_url;
	    this.check = false;
	}
	  
	public String getName(){
		return name;
	}
	  
	public String getId(){
	   return id;
	}
	  
	public String getImage_url(){
	   return image_url;
	}
	
	public Boolean getCheck(){
		   return check;
	}
	  
	public String toString(){
	   return name + " - " + image_url;
	}
}
