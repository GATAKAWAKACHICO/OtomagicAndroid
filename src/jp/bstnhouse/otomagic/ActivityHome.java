package jp.bstnhouse.otomagic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class ActivityHome extends Activity {
    /** Called when the activity is first created. */
	private ProgressDialog prog;
	private AlertDialog.Builder alertDialog2;
	private SharedPreferences mPrefs;
	Map<String, String> requestParams = new HashMap<String, String>();
	private DefaultHttpClient client;
	private HttpEntity entity;
	private String json;
	RequestOtomagicData req_otm_data = new RequestOtomagicData();
	ConfOtmApp otm_conf = new ConfOtmApp();
	ConfFacebook otm_fb_conf = new ConfFacebook();
	Facebook facebook = new Facebook(otm_fb_conf.getOtomagicFacebookId());
	AsyncFacebookRunner m_facebook_runner = new AsyncFacebookRunner (facebook);
	private String access_token;
	private String regist_params;
	
	GoogleAnalyticsTracker tracker;
	ConfAnalytics conf_ana = new ConfAnalytics();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.startNewSession(conf_ana.OTOMAGIC_ANALITICS_UA, this);
        
        Button start_btn = (Button) findViewById(R.id.start_button);
        start_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				tracker.trackEvent(
			            "Clicks",  // Category
			            "Button",  // Action
			            "buttonToSelectFriend", // Label
			            77);       // Value
				doOtmUserLoginRequest();
			}
        });
        
        alertDialog2 = new AlertDialog.Builder(this);
        alertDialog2.setTitle(getString(R.string.error_title));
        alertDialog2.setMessage(getString(R.string.error_json));
        alertDialog2.setIcon(drawable.stat_notify_error);
        alertDialog2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
        });
    }
    
    @Override
    protected void onPause() {
      super.onPause();
      //  追跡の結果の送信
      tracker.dispatch();
    }
    
    @Override
    protected void onDestroy() {
      super.onDestroy();
      // Stop the tracker when it is no longer needed.
      tracker.stopSession();
    }
    
    private Handler ahandler = new Handler(){
		public void handleMessage(Message msg) {
			prog.dismiss();
			alertDialog2.show();
		}
	};
	
	private Handler mhandler = new Handler(){
		public void handleMessage(Message msg) {
			getFacebookAccessTokenFromSharedPr();
		}
	};
	
    private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			prog.dismiss();
			Intent i = new Intent(getApplication(), ActivitySelectFriend.class);
			startActivity(i);
			ActivityHome.this.finish();
		}
	};
    
	private void getFacebookAccessTokenFromSharedPr(){
    	mPrefs = getPreferences(MODE_PRIVATE);
        access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
        if(access_token != null) {
            facebook.setAccessToken(access_token);
            m_facebook_runner.request ("/me/music", new LikeRequestListener());
        }
        
        if(expires != 0) {
            facebook.setAccessExpires(expires);
        }
        
        /*
         * Only call authorize if the access_token has expired.
         */
        if(!facebook.isSessionValid()) {

            facebook.authorize(this, new String[] {"publish_stream", "email", "user_about_me", "friends_about_me", "user_interests", "friends_interests", "user_likes", "friends_likes"}, new DialogListener() {
                @Override
                public void onComplete(Bundle values) {
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("access_token", facebook.getAccessToken());
                    editor.putLong("access_expires", facebook.getAccessExpires());
                    editor.commit();
                    m_facebook_runner.request ("/me/music", new LikeRequestListener());
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
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebook.authorizeCallback(requestCode, resultCode, data);
    }
	
    public class LikeRequestListener implements com.facebook.android.AsyncFacebookRunner.RequestListener{

		@Override
		public void onComplete(String response, Object state) {
			// TODO Auto-generated method stub
			try
	        {
	          Log.d ("Facebook", "Friends-Request : response.length(): " + response.length ());
	          Log.d ("Facebook", "Friends-Request : Response: " + response);
	          
	          final JSONObject json = new JSONObject (response);
	          JSONArray d = json.getJSONArray ("data");
	          int l = (d != null ? d.length () : 0);
	          Log.d ("Facebook-Friends-Request", "d.length (): " + l);
	          mPrefs = getSharedPreferences("ME",MODE_PRIVATE);
	          String me_id = mPrefs.getString("me_id", null);
	          regist_params = "artists_data[facebook_id]=" + me_id;
	          
	          if (d != null){
	        	  for (int i = 0; i < d.length(); i++)
	        	  {
	        		  JSONObject o = d.getJSONObject (i);

	        		  String artist_name = o.getString ("name");
	        		  artist_name = artist_name.replaceAll("[ ]","%20");
	        		  regist_params += "&artists_data[artists][]=" + artist_name;
	        	  }
	        	  doOtmArtistRegistRequest(regist_params);
	           }else{
	        	   prog.dismiss();
	        	   ahandler.sendEmptyMessage(0);
	           }
	        }
	        catch (JSONException e)
	        {
	          Log.d ("Facebook", "Friends-Request : JSON Error in response: "+ e);
	          prog.dismiss();
	          ahandler.sendEmptyMessage(0);
	        }
		}

		@Override
		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    
    private void doOtmUserLoginRequest(){
    	this.prog = ProgressDialog.show(this, getString(R.string.data_loading_title), getString(R.string.data_loading));
    	json = null;
    	
    	final ResponseHandler<String> response = new ResponseHandler<String>(){
			@Override
			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				// TODO Auto-generated method stub
				if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
					//正常に受信できた場合
					entity  =  response.getEntity();
					//InputStream in = entity.getContent();
					
					try{
						json = EntityUtils.toString(entity);
					}catch (ParseException e){
						Log.e("ERROR", "ParseException");
					}finally {
		                try {
		                    entity.consumeContent();
		                }
		                catch (IOException e) {
		                    //例外処理
		                }
		            }
					client.getConnectionManager().shutdown();
					Log.d("responce", json);
					if(req_otm_data.isStatusOk(json) == true){
						mhandler.sendEmptyMessage(0);
					}else{
	                    ahandler.sendEmptyMessage(0);
					}
				}else{
					//正常に受信できなかった場合
                    ahandler.sendEmptyMessage(0);
                    Log.e("OTM",response.getStatusLine().toString());
				}
				return null;
			}
    	};
    	
    	new Thread() {
    		public void run() {
    			 mPrefs = getSharedPreferences("ME",MODE_PRIVATE);
    			 String me_id = mPrefs.getString("me_id", null);
	        	 String me_name = mPrefs.getString("me_name", null);
	        	 String me_gender = mPrefs.getString("me_gender", null);
	        	 requestParams = new HashMap<String, String>();
	        	 requestParams.put("user[facebook_id]", me_id);
	        	 requestParams.put("user[facebook_name]", me_name);
	        	 requestParams.put("user[facebook_gender]", me_gender);
	        	 List<NameValuePair> params = new ArrayList<NameValuePair>();
	        	 for (Map.Entry<String, String> entry : requestParams.entrySet()) {
	                 params.add(new BasicNameValuePair((String) entry.getKey(), (String) entry.getValue()));
	             }
    			try{
    				client = new DefaultHttpClient();
    				String url = otm_conf.OTOMAGIC_API_ROOT_URL + otm_conf.OTOMAGIC_API_USERS_LOGIN;
    				HttpPost httpMethod       = new HttpPost(url);
    				httpMethod.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
    				Log.d("リクエストurl", url);
    				client.execute(httpMethod,response);
    			} catch (MalformedURLException e){
    				client.getConnectionManager().shutdown();
    				prog.dismiss();
    				ahandler.sendEmptyMessage(0);
    				Log.e("Error:", e.toString());
    			} catch (IllegalArgumentException e){
    				client.getConnectionManager().shutdown();
    				prog.dismiss();
    				ahandler.sendEmptyMessage(0);
    				Log.e("Error:", e.toString());
    			} catch (Exception e) {
    				client.getConnectionManager().shutdown();
    				Log.e("Thread-Error", e.toString());
    				prog.dismiss();
    				ahandler.sendEmptyMessage(0);
    			}
    		}
    	}.start();
    }
    
    private void doOtmArtistRegistRequest(final String params){
    	json = null;
    	
    	final ResponseHandler<String> response = new ResponseHandler<String>(){
			@Override
			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				// TODO Auto-generated method stub
				if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
					//正常に受信できた場合
					entity  =  response.getEntity();
					//InputStream in = entity.getContent();
					
					try{
						json = EntityUtils.toString(entity);
					}catch (ParseException e){
						Log.e("ERROR", "ParseException");
					}finally {
		                try {
		                    entity.consumeContent();
		                }
		                catch (IOException e) {
		                    //例外処理
		                }
		            }
					client.getConnectionManager().shutdown();
					Log.d("responce", json);
					if(req_otm_data.isStatusOk(json) == true){
						handler.sendEmptyMessage(0);
					}else{
	                    ahandler.sendEmptyMessage(0);
					}
				}else{
					//正常に受信できなかった場合
                    ahandler.sendEmptyMessage(0);
                    Log.e("OTM",response.getStatusLine().toString());
				}
				return null;
			}
    	};
    	
    	new Thread() {
    		public void run() {
   			try{
   				client = new DefaultHttpClient();
   				String url = otm_conf.OTOMAGIC_API_ROOT_URL + otm_conf.OTOMAGIC_API_USER_ARTISTS_REGIST
   						+ params;
   				HttpGet httpMethod       = new HttpGet(url);
   				Log.d("リクエストurl", url);
   				client.execute(httpMethod,response);
   			} catch (MalformedURLException e){
   				client.getConnectionManager().shutdown();
   				prog.dismiss();
   				ahandler.sendEmptyMessage(0);
   				Log.e("Error:", e.toString());
   			} catch (IllegalArgumentException e){
   				client.getConnectionManager().shutdown();
   				prog.dismiss();
   				ahandler.sendEmptyMessage(0);
   				Log.e("Error:", e.toString());
   			} catch (Exception e) {
   				client.getConnectionManager().shutdown();
   				Log.e("Thread-Error", e.toString());
   				prog.dismiss();
   				ahandler.sendEmptyMessage(0);
   			}
    		}
    	}.start();
    }
}