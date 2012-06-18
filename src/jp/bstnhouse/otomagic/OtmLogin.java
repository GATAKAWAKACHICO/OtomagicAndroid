package jp.bstnhouse.otomagic;

import android.content.SharedPreferences;
import android.util.Log;
import android.app.Activity;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;

public class OtmLogin extends Activity{
	private static String OTOMAGIC_APP_ID = "301955646531244";
	private static Facebook facebook = new Facebook(OTOMAGIC_APP_ID);
	AsyncFacebookRunner m_facebook_runner = new AsyncFacebookRunner (OtmLogin.facebook);
	private static SharedPreferences mPrefs;
	private static String access_token;
	
	public boolean isSharedPreferencesOfAccessToken(){
		mPrefs = getPreferences(MODE_PRIVATE);
        access_token = mPrefs.getString("access_token", null);
        if(access_token != null) {
            facebook.setAccessToken(access_token);
            Log.d("token:", "アクセストークンある！");
            return true;
        }else{
        	Log.d("token:", "アクセストークンないんかい！");
        	return false;
        }
	}
	
	public boolean isAccessTokenExpiresValid(){
		long expires = mPrefs.getLong("access_expires", 0);
        if(expires != 0) {
            facebook.setAccessExpires(expires);
            return true;
        }else{
        	return false;
        }
	}
	
	public boolean isUserLoginFacebookSessionValid(){
		/*
         * Only call authorize if the access_token has expired.
         */
        if(!facebook.isSessionValid()) {
        	return false;
        }else{
        	return true;
        }
	}
}
