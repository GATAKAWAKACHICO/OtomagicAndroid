package jp.bstnhouse.otomagic;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;

public class OtmFacebookConf {
	private String OTOMAGIC_FACEBOOK_APP_ID = "301955646531244";
	Facebook facebook;
	
	public String getOtomagicFacebookId(){
		return OTOMAGIC_FACEBOOK_APP_ID;
	}
	
	public void otomagicLoginWithFacebook(Activity act, final SharedPreferences mPrefs){
		/*facebook SDKにてログイン*/
		facebook.authorize(act, new String[] {}, new DialogListener() {
    		public void onComplete(Bundle values) {
    			SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString("access_token", facebook.getAccessToken());
                editor.putLong("access_expires", facebook.getAccessExpires());
                editor.commit();
                Log.d("token:", "アクセストークンを取得" + values);
    		}

    		@Override
    		public void onFacebookError(FacebookError error) {}

    		@Override
    		public void onError(DialogError e) {}

    		@Override
    		public void onCancel() {}
    	});
	}
}
