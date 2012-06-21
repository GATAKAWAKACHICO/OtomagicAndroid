package jp.bstnhouse.otomagic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.net.ParseException;
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
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ActivitySelectFriend extends Activity{
	RequestOtomagicData req_otm_data = new RequestOtomagicData();
	ConfOtmApp otm_app_conf = new ConfOtmApp();
	ConfFacebook otm_fb_conf = new ConfFacebook();
	Facebook facebook = new Facebook(otm_fb_conf.getOtomagicFacebookId());
	AsyncFacebookRunner m_facebook_runner = new AsyncFacebookRunner (facebook);
	private Button play_btn;
	private SharedPreferences mPrefs;
	private String access_token;
	private String json = null;
	private DefaultHttpClient client;
	private HttpEntity entity;
	private Friend friend;
	private SQLiteDatabase db;
	private SubOpenHelper helper;
	ExecutorService execService;
	private ProgressDialog prog;
	private AlertDialog.Builder alertDialog;
	private AlertDialog.Builder alertDialog2;
	
	static List<Friend> dataList = new ArrayList<Friend>();
	static List<Friend> checkedDataList = new ArrayList<Friend>();;
	static FriendAdapter adapter;
	ListView listView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.select_friend);
        
        final Button play_btn = (Button) findViewById(R.id.play_button);
        listView = (ListView)findViewById(R.id.friendListView);
        
        //ArrayAdapter初期化
        setAdapters();
        //facebookのアクセストークン取得
        getFacebookAccessTokenFromSharedPr();
        
        dataList = new ArrayList<Friend>();
        this.prog = ProgressDialog.show(this, getString(R.string.data_loading_title), getString(R.string.data_loading));
        
        play_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				checkedDataList = null;
				checkedDataList = new ArrayList<Friend>();
				//play_btn.setEnabled(false);
				boolean checked;
				for (int i = 0; i < dataList.size(); i++){
					checked = dataList.get(i).check;
					if (checked){
						checkedDataList.add(new Friend(dataList.get(i).id, dataList.get(i).name, dataList.get(i).image_url, true));
					}
				}
				if(checkedDataList.size() == 0){
					play_btn.setEnabled(true);
					alertDialog2.show();
				}else{
					Log.d("checked",checkedDataList.toString());
					prog.show();
					SaveCheckedDataAllTransaction();
				}
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
				//SelectFriend.this.finish();
			}
        });
        
        alertDialog2 = new AlertDialog.Builder(this);
        alertDialog2.setTitle(getString(R.string.error_title));
        alertDialog2.setMessage(getString(R.string.error_no_check));
        alertDialog2.setIcon(drawable.stat_notify_error);
        alertDialog2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
        });
    }
    
    //エラー時にAlertDialogを表示させるためのHandler
    private final Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			alertDialog.show();
		}
	};
	
    //DBにチェックされたユーザを保存するためのハンドラ
    private Handler mhandler = new Handler() {
        public void handleMessage(Message msg){
        	saveCheckedDataToDB();
            prog.dismiss();
        }
    };
    
    protected void saveCheckedDataToDB() {
    	Intent i = new Intent(getApplication(), ActivityOtmPlayer.class);
    	helper = new SubOpenHelper(getApplicationContext(),"checkedDataList.db",null, 1);
    	SQLiteStatement stmt;
		try{
			//DBを開く(書き込みモード)
			db = helper.getWritableDatabase();
			//以前にチェックされたユーザのデータを破棄（レコードの削除）
			db.delete("checked_data_list", "", null);
			
			try{
				db.beginTransaction();
				stmt = db.compileStatement("INSERT INTO checked_data_list(facebook_id, name) VALUES(?, ?);");
				Log.d("stmt","INSERT INTO checked_data_list(facebook_id, name) VALUES(?, ?);");
				for (int j = 0; j < checkedDataList.size(); j++){
					stmt.bindString(1, checkedDataList.get(j).id);
					stmt.bindString(2, checkedDataList.get(j).name);
					stmt.executeInsert();
					Log.d("データ","挿入");
				}
				db.setTransactionSuccessful();
				Log.d("DB保存","完了");
				startActivity(i);
			}catch(Exception e){
				Log.e("SQLiteException",e.toString());
				db.close();
		        handler.sendEmptyMessage(0);
			}finally {
			    db.endTransaction();
			    db.close();
			}
		}catch(SQLiteException e){
			Log.e("SQLiteException",e.toString());
			db.close();
	        handler.sendEmptyMessage(0);
		}
    }
    
    public void SaveCheckedDataAllTransaction(){
    	new Thread() {
            public void run() {
                try{
                	//ロード中の表示
                	mhandler.sendEmptyMessage(0);
                }catch (Exception e) {
                    Log.e("ERROR", e.toString());
                    prog.dismiss();
                    handler.sendEmptyMessage(0);
                }
            }
        }.start();  	
    }
    
    @Override
    public void onPause(){
    	super.onPause();
	}
    
    @Override
    protected void onRestart() {
    	super.onRestart();
	}
	 
	@Override
	protected void onResume() {
	  super.onResume();
	}
	 
    private void getFacebookAccessTokenFromSharedPr(){
    	mPrefs = getPreferences(MODE_PRIVATE);
        access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
        if(access_token != null) {
            facebook.setAccessToken(access_token);
            m_facebook_runner.request ("/me/friends", new FriendsRequestListener());
        }
        
        if(expires != 0) {
            facebook.setAccessExpires(expires);
        }
        
        /*
         * Only call authorize if the access_token has expired.
         */
        if(!facebook.isSessionValid()) {

            facebook.authorize(this, new String[] {}, new DialogListener() {
                @Override
                public void onComplete(Bundle values) {
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("access_token", facebook.getAccessToken());
                    editor.putLong("access_expires", facebook.getAccessExpires());
                    editor.commit();
                    m_facebook_runner.request ("/me/friends", new FriendsRequestListener());
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
	        	  for (int i = 0; i < d.length(); i++)
	        	  {
	        		  JSONObject o = d.getJSONObject (i);

	        		  String id = o.getString ("id");
	        		  String name = o.getString ("name");
	        		  String image_url = "http://graph.facebook.com/"+ id +"/picture";
	        		  dataList.add(new Friend(id, name, image_url, false));
	        	  }
	        	  //リストにユーザー自身を追加
	        	  mPrefs = getSharedPreferences("ME",MODE_PRIVATE);
	        	  String me_id = mPrefs.getString("me_id", null);
	        	  String me_name = mPrefs.getString("me_name", null);
	        	  String image_url = "http://graph.facebook.com/"+ me_id +"/picture";
	        	  dataList.add(new Friend(me_id, me_name, image_url, false));
	        	  Log.d("me",me_name);
	        	  
	        	  //OTOMAGICサーバにユーザーを問い合わせるためのURL作成
	        	  String url = req_otm_data.getUserListRequestUrl(dataList);
	        	  //上で作ったURLからユーザー問い合わせリクエスト送信
	        	  doOtomagicUserRequest(url);
	        	  
	           }else{
	        	   prog.dismiss();
	        	   handler.sendEmptyMessage(0);
	           }
	        }
	        catch (JSONException e)
	        {
	          Log.d ("Facebook", "Friends-Request : JSON Error in response: "+ e);
	          prog.dismiss();
	          handler.sendEmptyMessage(0);
	        }
		}
		
		//OTOMAGICサーバにユーザーを問い合わせる
		private void doOtomagicUserRequest(final String url) {
	    	json = null;
	        final ResponseHandler<String> response = new ResponseHandler<String>(){

				@Override
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
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
						dataList = null;
						dataList = new ArrayList<Friend>();
						dataList = req_otm_data.getUserList(json, dataList, getString(R.string.error_no_friend));
						
						arrangeDataList();
					}else{
						//正常に受信できなかった場合
						prog.dismiss();
	                    handler.sendEmptyMessage(0);
	                    Log.e("OTM",response.getStatusLine().toString());
					}
					return null;
				}
	 
	        };
	 
	        // 通信の実行
	        new Thread() {
	            public void run() {
	                try{
	                    client = new DefaultHttpClient();
	                    HttpGet httpMethod       = new HttpGet(url);
	                    client.execute(httpMethod,response);
	                }catch (Exception e) {
	                    Log.e("ERROR", e.toString());
	                    prog.dismiss();
	                    handler.sendEmptyMessage(0);
	                }
	                //client.getConnectionManager().shutdown();
	            }
	 
	        }.start(); 
	    }
		
		private void arrangeDataList(){
			//アルファベット順でソート
      	  Collections.sort(dataList, new Comparator<Friend>(){
				@Override
				public int compare(Friend lhs, Friend rhs) {
					// TODO Auto-generated method stub
					return lhs.getName().compareTo(rhs.getName());
				}
            });

      	  ActivitySelectFriend.this.runOnUiThread (new Runnable () {
      		  public void run ()
      		  {
      			  adapter.notifyDataSetChanged();
      			  prog.dismiss();
      		  }
      	  });
		}

		@Override
		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub
			Log.d ("Facebook", "Friends-Request : IO Error in response: "+ e);
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
    	
    	public class ViewHolder { 
    	    TextView textView1;
    	    ImageView profile_imageview;
    	    CheckBox checkBox;
    	}
    	
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		// TODO Auto-generated method stub
    	    View v = convertView;
    	    ViewHolder holder;

    	    if(v == null){
    	    	LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	        v = inflater.inflate(R.layout.friend_row, null);
    	        
    	    }else{
    	    	holder = (ViewHolder) v.getTag();
    	    }
    	    friend = (Friend)getItem(position);
    	    if(friend != null){
    	    	/*profile_imageview = (ImageView) v.findViewById(R.id.profile_img);
    	    	textView1 = (TextView) v.findViewById(R.id.friend_name);
    	        checkBox = (CheckBox) v.findViewById(R.id.friendSelectCheckBox);*/
    	    	holder = new ViewHolder();
    	        holder.profile_imageview = (ImageView) v.findViewById(R.id.profile_img);
    	        holder.textView1 = (TextView) v.findViewById(R.id.friend_name);
    	        holder.checkBox = (CheckBox) v.findViewById(R.id.friendSelectCheckBox);
    	        v.setTag(holder);
    	    	holder.textView1.setText(friend.name);
    	        final int p = position;
    	        holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean isChecked) {
						// TODO Auto-generated method stub
						Log.i("MultipleChoiceListActivity", "p=" + String.valueOf(p) + ", isChecked=" + String.valueOf(isChecked));
						friend = dataList.get(p);
						friend.check = isChecked;
						dataList.set(p, friend);
					}
        		});
    	        holder.checkBox.setChecked(dataList.get(position).check);
    	        // 画像を非表示  
    	        //holder.profile_imageview.setVisibility(View.GONE);
    	        holder.profile_imageview.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.user_icon_def));
    	        // このタグを AsyncTask で使う。
    	        holder.profile_imageview.setTag(friend.image_url);
    	        GetFriendImageAsyncTask task = new GetFriendImageAsyncTask (getApplicationContext (), holder.profile_imageview);
    	        task.execute (friend.image_url);
    	    }
    	    return v;
    	}
    }
}
