package jp.bstnhouse.otomagic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;

public class Splash extends Activity {
	OtmFacebookConf otm_fb_conf = new OtmFacebookConf();
	Facebook facebook = new Facebook(otm_fb_conf.getOtomagicFacebookId());
	AsyncFacebookRunner m_facebook_runner = new AsyncFacebookRunner (facebook);
	private SharedPreferences mPrefs;
	private String me;
	private String access_token;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        Handler hdl = new Handler();
		hdl.postDelayed(new splashHandler(), 1000);
    }
	
	class splashHandler implements Runnable {
		public void run() {
			/*
	         * Get existing access_token if any
	         */
	        mPrefs = getPreferences(MODE_PRIVATE);
	        access_token = mPrefs.getString("access_token", null);
	        long expires = mPrefs.getLong("access_expires", 0);
	        if(access_token != null) {
	            facebook.setAccessToken(access_token);
	        }
	        
	        if(expires != 0) {
	            facebook.setAccessExpires(expires);
	        }
	        
	        /*
	         * Only call authorize if the access_token has expired.
	         */
	        if(!facebook.isSessionValid()) {
	        	/*facebook SDKにてログイン*/
	    		facebook.authorize(Splash.this, new String[] {}, new DialogListener() {
	        		public void onComplete(Bundle values) {
	        			SharedPreferences.Editor editor = mPrefs.edit();
	                    editor.putString("access_token", facebook.getAccessToken());
	                    editor.putLong("access_expires", facebook.getAccessExpires());
	                    editor.commit();
	                    Log.d("token:", "アクセストークンを取得" + values);
	                    m_facebook_runner.request ("me", new MeRequestListener());
	                    goToHomeActivity();
	        		}

	        		@Override
	        		public void onFacebookError(FacebookError error) {}

	        		@Override
	        		public void onError(DialogError e) {}

	        		@Override
	        		public void onCancel() {}
	        	});
	        }else{
	        	goToHomeActivity();
	        }
		}		
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		/* facebookアプリが端末にインストールされているとこのメソッドに飛ぶ */
        super.onActivityResult(requestCode, resultCode, data);
        facebook.authorizeCallback(requestCode, resultCode, data);
        //goToHomeActivity();
		//m_facebook_runner.request ("me", new MeRequestListener());
    }
	
	public class MeRequestListener implements com.facebook.android.AsyncFacebookRunner.RequestListener
	{
		@Override
		public void onComplete(String response, Object state) {
			// TODO Auto-generated method stub
			try
	        {
	          Log.d ("Facebook", "Friends-Request : response.length(): " + response.length ());
	          Log.d ("Facebook", "Friends-Request : Response: " + response);

	          final JSONObject json = new JSONObject (response);
	          //JSONObject json_name = json.getJSONObject("name");
	          String me_id = json.getString ("id");
	          String me_name = json.getString ("name");
	          mPrefs = getSharedPreferences("ME", MODE_PRIVATE);
	          SharedPreferences.Editor editor = mPrefs.edit();
	          editor.putString("me_id", me_id);
	          editor.putString("me_name", me_name);
	          editor.commit();
	          Log.d("保存",me_name);
	          mPrefs = getSharedPreferences("Config",MODE_PRIVATE);
	          boolean play_background_flg = mPrefs.getBoolean("config_play_background", true);
	          SharedPreferences.Editor editor2 = mPrefs.edit();
	          editor2.putBoolean("config_play_background", play_background_flg);
	          editor2.commit();
	        }
	        catch (JSONException e)
	        {
	          Log.d ("Facebook", "Friends-Request : JSON Error in response: "+ e);
	        }
		}

		@Override
		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub
			
		}

	}
	
	private void goToHomeActivity(){
		/* Home画面に移動 */
		Intent i = new Intent(getApplication(), MainTab.class);
		startActivity(i);
		Splash.this.finish();
	}
}
