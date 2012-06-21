package jp.bstnhouse.otomagic;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;

@SuppressWarnings("deprecation")
public class MainTab extends TabActivity{
	public void onCreate(Bundle savedInstanceState) {  
	    super.onCreate(savedInstanceState); 
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.main_tab);  
	  
	    // Resource object to get Drawables  
	    Resources res = getResources();   
	  
	    // The activity TabHost  
	    TabHost tabHost = getTabHost();    
	  
	    // Resusable TabSpec for each tab  
	    TabHost.TabSpec spec;  
	  
	    // Reusable Intent for each tab  
	    Intent intent;  
	  
	    // Create an Intent to launch an Activity   
	    // for the tab (to be reused)  
	    intent = new Intent().setClass(this, ActivityHome.class);  
	  
	    // Initialize a TabSpec for each tab and   
	    // add it to the TabHost  
	    spec = tabHost.newTabSpec("Home")  
	                  .setIndicator("Home",   
	                   res.getDrawable(R.drawable.home_gr))  
	                  .setContent(intent);  
	    tabHost.addTab(spec);  
	  
	    // Do the same for the other tabs  
	    intent = new Intent().setClass(this, ActivityConfig.class);  
	    spec = tabHost.newTabSpec("Config")  
	                  .setIndicator(getString(R.string.config),   
	                   res.getDrawable(R.drawable.wrench2_gr))  
	                  .setContent(intent);  
	    tabHost.addTab(spec);  
	  
	    tabHost.setCurrentTab(0);  
	}
}
