package jp.bstnhouse.otomagic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SelectFriend extends Activity{
	OtmFacebookConf otm_fb_conf = new OtmFacebookConf();
	Facebook facebook = new Facebook(otm_fb_conf.getOtomagicFacebookId());
	AsyncFacebookRunner m_facebook_runner = new AsyncFacebookRunner (facebook);
	private SharedPreferences mPrefs;
	private String access_token;
	private Friend friend;
	private ImageView profile_imageview;
	private ProgressDialog prog;
	private AlertDialog.Builder alertDialog;
	
	static List<Friend> dataList = new ArrayList<Friend>();
	static FriendAdapter adapter;
	ListView listView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.select_friend);
        
        Button play_btn = (Button) findViewById(R.id.play_button);
        listView = (ListView)findViewById(R.id.friendListView);
        setAdapters();
        facebookAccessTokenCheck();
        play_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getApplication(), OtmPlayer.class);
				startActivity(i);
			}
        });
        
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.error_title));
        alertDialog.setMessage(getString(R.string.error_json));
        alertDialog.setIcon(drawable.stat_notify_error);
        
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getApplication(), MainTab.class);
				startActivity(i);
				SelectFriend.this.finish();
			}
        });
    }
    
    public void facebookAccessTokenCheck(){
    	mPrefs = getPreferences(MODE_PRIVATE);
        access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
        if(access_token != null) {
            facebook.setAccessToken(access_token);
            Log.d("token:", "アクセストークンある！");
            dataList = new ArrayList<Friend>();
            this.prog = ProgressDialog.show(this, getString(R.string.data_loading_title), getString(R.string.data_loading));
            m_facebook_runner.request ("/me/friends", new FriendsRequestListener());
        }else{
        	Log.d("token:", "アクセストークンないんかい！");
        }
        if(expires != 0) {
            facebook.setAccessExpires(expires);
        }
        
        facebookLoginCheck();
    }
    
	public void facebookLoginCheck(){
		/*
         * Only call authorize if the access_token has expired.
         */
        if(!facebook.isSessionValid()) {
        	otomagicLoginWithFacebook();
        }else{
        	
        }
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		/* facebookアプリが端末にインストールされているとこのメソッドに飛ぶ */
        super.onActivityResult(requestCode, resultCode, data);
        facebook.authorizeCallback(requestCode, resultCode, data);
        dataList = new ArrayList<Friend>();
		m_facebook_runner.request ("/me/friends", new FriendsRequestListener());
    }
	
	private void otomagicLoginWithFacebook(){
		/*facebook SDKにてログイン*/
		facebook.authorize(SelectFriend.this, new String[] {}, new DialogListener() {
    		public void onComplete(Bundle values) {
    			SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString("access_token", facebook.getAccessToken());
                editor.putLong("access_expires", facebook.getAccessExpires());
                editor.commit();
                Log.d("token:", "アクセストークンを取得" + values);
                dataList = null;
    		}

    		@Override
    		public void onFacebookError(FacebookError error) {}

    		@Override
    		public void onError(DialogError e) {}

    		@Override
    		public void onCancel() {}
    	});
	}
    
	public class FriendsRequestListener implements com.facebook.android.AsyncFacebookRunner.RequestListener
	{
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

	          //for (int i = 0; i < l; i++)
	          if (d != null){
	        	  for (int i = 0; i < 10; i++)
	        	  {
	        		  JSONObject o = d.getJSONObject (i);

	        		  String id = o.getString ("id");
	        		  String name = o.getString ("name");
	        		  String image_url = "http://graph.facebook.com/"+ id +"/picture";
	        		  dataList.add(new Friend(id, name, image_url, false));
	        	  }
	          
	        	  SelectFriend.this.runOnUiThread (new Runnable () {
	        		  public void run ()
	        		  {
	        			  adapter.notifyDataSetChanged();
	        			  prog.dismiss();
	        		  }
	        	  });
	           }else{
	        	   prog.dismiss();
	        	   handler.sendEmptyMessage(0);
	           }
	        }
	        catch (JSONException e)
	        {
	          Log.d ("Facebook", "Friends-Request : JSON Error in response: "+ e);
	          prog.dismiss();
	          alertDialog.show();
	        }
		}
		
		private final Handler handler = new Handler(){
			public void handleMessage(Message msg) {
				alertDialog.show();
			}
		};

		@Override
		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub
			prog.dismiss();
	        handler.sendEmptyMessage(0);
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			// TODO Auto-generated method stub
			prog.dismiss();
			handler.sendEmptyMessage(0);
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			// TODO Auto-generated method stub
			prog.dismiss();
			handler.sendEmptyMessage(0);
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub
			prog.dismiss();
			handler.sendEmptyMessage(0);
		}

	}
	
    protected void setAdapters(){
        adapter = new FriendAdapter();
        listView.setAdapter(adapter);
    }
    
    protected void addItem(String id, String name){
        dataList.add(new Friend(id, name, "http://graph.facebook.com/"+ id +"/picture", false));
        adapter.notifyDataSetChanged();
    }

    public class FriendAdapter extends BaseAdapter{

    	@Override
    	public int getCount() {
    		// TODO Auto-generated method stub
    		return dataList.size();
    	}

    	@Override
    	public Object getItem(int position) {
    		// TODO Auto-generated method stub
    		return dataList.get(position);
    	}

    	@Override
    	public long getItemId(int position) {
    		// TODO Auto-generated method stub
    		return position;
    	}
    	
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		// TODO Auto-generated method stub
    		TextView textView1;
    		CheckBox checkBox;
    		
    	    View v = convertView;

    	    if(v == null){
    	    	LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	        v = inflater.inflate(R.layout.friend_row, null);
    	    }
    	    friend = (Friend)getItem(position);
    	    if(friend != null){
    	    	textView1 = (TextView) v.findViewById(R.id.friend_name);
    	        textView1.setText(friend.name);
    	        checkBox = (CheckBox) v.findViewById(R.id.friendSelectCheckBox);
    	        final int p = position;
    	        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean isChecked) {
						// TODO Auto-generated method stub
						Log.i("MultipleChoiceListActivity", "p=" + String.valueOf(p) + ", isChecked=" + String.valueOf(isChecked));
						dataList.set(p, friend);
					}
        		});
    	        Log.d("dataList.get(position).check.toString():", dataList.get(position).check.toString());
    	        checkBox.setChecked(dataList.get(position).check);
    	        profile_imageview = (ImageView) v.findViewById(R.id.profile_img);
    	        GetFriendImageAsyncTask task = new GetFriendImageAsyncTask (getApplicationContext (), profile_imageview);
    	        task.execute (friend.image_url);
    	    }
    	    return v;
    	}
    }
}
