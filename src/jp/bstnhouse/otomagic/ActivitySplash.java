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

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;

public class ActivitySplash extends Activity {
	ConfFacebook otm_fb_conf = new ConfFacebook();
	Facebook facebook = new Facebook(otm_fb_conf.getOtomagicFacebookId());
	AsyncFacebookRunner m_facebook_runner = new AsyncFacebookRunner (facebook);
	private SharedPreferences mPrefs;
	private String access_token;
	private AlertDialog.Builder alertDialog;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.error_title));
        alertDialog.setMessage(getString(R.string.warn_login_with_facebook));
        alertDialog.setIcon(drawable.stat_notify_error);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
        });
        
        alertDialog.setNegativeButton(getString(R.string.download_facebook_app), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.facebook.katana");
	        	Intent i = new Intent(Intent.ACTION_VIEW,uri);
	        	startActivity(i);
			}
        });
        
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
	        	/*facebook SDK�ɂă��O�C��*/
	    		facebook.authorize(ActivitySplash.this, new String[] {"publish_stream", "email", "user_about_me", "friends_about_me", "user_interests", "friends_interests", "user_likes", "friends_likes"}, new DialogListener() {
	        		public void onComplete(Bundle values) {
	        			SharedPreferences.Editor editor = mPrefs.edit();
	                    editor.putString("access_token", facebook.getAccessToken());
	                    editor.putLong("access_expires", facebook.getAccessExpires());
	                    editor.commit();
	                    Log.d("token:", "�A�N�Z�X�g�[�N�����擾" + values);
	                    m_facebook_runner.request ("me", new MeRequestListener());
	                    goToHomeActivity();
	        		}

	        		@Override
	        		public void onFacebookError(FacebookError error) {}

	        		@Override
	        		public void onError(DialogError e) {}

	        		@Override
	        		public void onCancel() {
	        			alertDialog.show();
	        		}
	        	});
	        }else{
	        	goToHomeActivity();
	        }
		}		
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		/* facebook�A�v�����[���ɃC���X�g�[������Ă���Ƃ��̃��\�b�h�ɔ�� */
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
	          String me_gender = json.getString("gender");
	          mPrefs = getSharedPreferences("ME", MODE_PRIVATE);
	          SharedPreferences.Editor editor = mPrefs.edit();
	          editor.putString("me_id", me_id);
	          editor.putString("me_name", me_name);
	          editor.putString("me_gender", me_gender);
	          editor.commit();
	          Log.d("�ۑ�",me_name + ":" + me_gender);
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
		/* Home��ʂɈړ� */
		Intent i = new Intent(getApplication(), ActivityMainTab.class);
		startActivity(i);
		ActivitySplash.this.finish();
	}
}
