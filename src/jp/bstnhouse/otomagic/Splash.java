package jp.bstnhouse.otomagic;

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
	            Log.d("token:", "アクセストークンある！");
	            m_facebook_runner.request ("me", new MeRequestListener());
	        }else{
	        	Log.d("token:", "アクセストークンないんかい！");
	        }
	        if(expires != 0) {
	            facebook.setAccessExpires(expires);
	        }
	        
	        facebookLoginCheck();
		}		
	}
	
	public void facebookLoginCheck(){
		/*
         * Only call authorize if the access_token has expired.
         */
        if(!facebook.isSessionValid()) {
        	otomagicLoginWithFacebook();
        }else{
        	goToHomeActivity();
        }
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		/* facebookアプリが端末にインストールされているとこのメソッドに飛ぶ */
        super.onActivityResult(requestCode, resultCode, data);
        facebook.authorizeCallback(requestCode, resultCode, data);
        goToHomeActivity();
		m_facebook_runner.request ("me", new MeRequestListener());
    }
	
	private void otomagicLoginWithFacebook(){
		/*facebook SDKにてログイン*/
		facebook.authorize(Splash.this, new String[] {}, new DialogListener() {
    		public void onComplete(Bundle values) {
    			SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString("access_token", facebook.getAccessToken());
                editor.putLong("access_expires", facebook.getAccessExpires());
                editor.commit();
                Log.d("token:", "アクセストークンを取得" + values);
                goToHomeActivity();
    		}

    		@Override
    		public void onFacebookError(FacebookError error) {}

    		@Override
    		public void onError(DialogError e) {}

    		@Override
    		public void onCancel() {}
    	});
	}
	
	private void goToHomeActivity(){
		/* Home画面に移動 */
		Intent i = new Intent(getApplication(), MainTab.class);
		startActivity(i);
		Splash.this.finish();
	}
}
