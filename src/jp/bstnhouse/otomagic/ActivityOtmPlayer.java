package jp.bstnhouse.otomagic;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActivityOtmPlayer extends Activity implements OnClickListener{
	private TextView artist_name;
	private TextView song_title_name;
	private TextView like_user_name;
	private ImageView artist_imageview;
	private Button back_btn;
	private Button next_btn;
	private Button play_stop_btn;
	private Button see_itunes_btn;
	private Button arrange_btn;
	private AlertDialog.Builder alertDialog;
	private AlertDialog.Builder alertDialog2;
	private ProgressDialog prog;
	private SQLiteDatabase db;
	private SQLiteCheckedDataListOpenHelper helper;
	private SQLitePlaylistOpenHelper helper2;
	String sql = "";
	RequestItunes itunes = new RequestItunes();
	private String json = null;
	String term;
	private String image_url;
	private String artistName;
	private String trackName;
	private Bitmap bitmap;
	private String pl_like_user_id;
	private String pl_like_user_name;
	private String collectionViewUrl;
	private DefaultHttpClient client;
	private HttpEntity entity;
	private MediaPlayer mp;
	private String preview_url;
	
	static List<Friend> checkedDataList = new ArrayList<Friend>();
	static List<UserArtist> UADataList = new ArrayList<UserArtist>();
	static List<PlayList> requestList = new ArrayList<PlayList>();
	static List<PlayList> playList = new ArrayList<PlayList>();
	
	private AdView adView;
	ConfAdmob otm_admob = new ConfAdmob();
	ConfOtmApp otm_conf = new ConfOtmApp();
	RequestOtomagicData req_otm_data = new RequestOtomagicData();
	MakePlayList mpl = new MakePlayList();
	PlayList currentPlayList;
	
	GoogleAnalyticsTracker tracker;
	ConfAnalytics conf_ana = new ConfAnalytics();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.player);
        artist_imageview = (ImageView) findViewById(R.id.discImageView);
        artist_name = (TextView) findViewById(R.id.artistNameTextView);
        song_title_name = (TextView) findViewById(R.id.songTitleTextView);
        like_user_name = (TextView) findViewById(R.id.likeUserNameTextView);
        
        //ボタンたち
        back_btn = (Button) findViewById(R.id.back_button);
        back_btn.setOnClickListener(this);
        next_btn = (Button) findViewById(R.id.next_button);
        next_btn.setOnClickListener(this);
        play_stop_btn = (Button) findViewById(R.id.play_stop_button);
        play_stop_btn.setOnClickListener(this);
        see_itunes_btn = (Button) findViewById(R.id.see_itunes_button);
        see_itunes_btn.setOnClickListener(this);
        arrange_btn = (Button) findViewById(R.id.arrange_button);
        arrange_btn.setOnClickListener(this);
        
        //admobの読み込み
        adView = new AdView(this, AdSize.BANNER, otm_admob.getOtomagicAdmobId());
        LinearLayout layout = (LinearLayout)findViewById(R.id.AdLayout);
        //テスト用
        //otm_admob.getTestAdView(adView, layout);
        //本番用
        otm_admob.getTestAdView(adView, layout);
        
        //GoogleAnalytics
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.startNewSession(conf_ana.OTOMAGIC_ANALITICS_UA, this);
        
        mp = new MediaPlayer();
        
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.warn_title));
        alertDialog.setMessage(getString(R.string.ask_finish));
        alertDialog.setIcon(drawable.stat_notify_error);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				//Intent i = new Intent(getApplication(), SelectFriend.class);
				//startActivity(i);
				ActivityOtmPlayer.this.finish();
			}
        });
        
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub

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
				//Intent i = new Intent(getApplication(), SelectFriend.class);
				//startActivity(i);
				//ActivityOtmPlayer.this.finish();
			}
        });
        
        doOtomagicUserDataRequest();
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
	    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
	        if (event.getAction() == KeyEvent.ACTION_DOWN) {
	            // ここで処理
	        	alertDialog.show();
	            return true;
	        }
	    }

	    return false;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {  
        // ボタンIDごとに処理を実装する  
        case R.id.next_button:  
            // 処理
        		mp.pause();
				mp.reset();
				mp.release();
				mp = null;
				mp = new MediaPlayer();
				Log.d("MediaPlayer:", "終了");
				back_btn.setEnabled(true);
				doItunesSearchRequest();
        	break;
        case R.id.back_button:
        	if(mp.isPlaying()){
        		mp.seekTo(0);
        		Log.d("Language:", getString(R.string.country));
        	}
        	break;
        case R.id.play_stop_button:
        	if(mp.isPlaying()){
        		mp.pause();
        		play_stop_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.play_btn_normal));
        		//play_stop_btn.setText("play");
        		back_btn.setEnabled(false);
        	}else{
        		try {
					//mp.prepare();
					mp.start();
					play_stop_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.stop_btn_normal));
					//play_stop_btn.setText("stop");
					back_btn.setEnabled(true);
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	break;
        case R.id.see_itunes_button:
        	Uri uri = Uri.parse(collectionViewUrl);
        	Intent i = new Intent(Intent.ACTION_VIEW,uri);
        	startActivity(i);
        	//Log.d("itunes","browser launch.");
        	break;
        case R.id.arrange_button:
        	if(mp.isPlaying()){
        		mp.pause();
        		play_stop_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.play_btn_normal));
        	}
        	savePlayListToDB();
        }
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		/*if(mp.isPlaying()){
			mp.stop();
		}*/
		//Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
		tracker.dispatch();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		//Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		if(!mp.isPlaying()){
			try {
				//mp.prepare();
				mp.start();
				play_stop_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.stop_btn_normal));
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//Toast.makeText(this, "onRestart", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mp.isPlaying()){
    		mp.stop();
			mp.reset();
			mp.release();
			Log.d("MediaPlayer:", "終了");
    	}
		tracker.stopSession();
		//Toast.makeText(this, "onDestory", Toast.LENGTH_SHORT).show();
	}
	
	private Handler ahandler = new Handler(){
		public void handleMessage(Message msg) {
			prog.dismiss();
			alertDialog2.show();
		}
	};
	
	private Handler mhandler = new Handler(){
		public void handleMessage(Message msg) {
			prog.dismiss();
			doItunesSearchRequest();
		}
	};
	
	private Handler playListSQLiteHandler = new Handler(){
		public void handleMessage(Message msg) {
			prog.dismiss();
			Intent i = new Intent(getApplication(), ActivityPlayerArrangeList.class);
			startActivity(i);
		}
	};
	
	private final Handler handler = new Handler(){	 
        /**
         * レスポンス取得でUIを更新する
         */
        public void handleMessage(Message msg) {
        	//ディスクイメージ
            artist_imageview.setImageBitmap(bitmap);
            //アーティスト名
            String bmess = msg.getData().getString("ARTIST_NAME");
            artist_name.setText(bmess);
            //トラック名
            String cmess = msg.getData().getString("TRACK_NAME");
            song_title_name.setText(cmess);
            //このアーティストを好きなユーザー名
            String emess = msg.getData().getString("LIKE_USER_NAME");
            like_user_name.setText(emess + getString(R.string.user_like));
            //音楽データのURL
            String dmess = msg.getData().getString("PREVIEW_URL");
            
            Log.d("再生間近", "！！");

            try {
				mp.setDataSource(dmess);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				prog.dismiss();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				prog.dismiss();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				prog.dismiss();
			}
            //mp.prepareAsync();
            
            try {
				mp.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mp.reset();
				mp.release();
				mp = null;
				mp = new MediaPlayer();
				prog.dismiss();
				doItunesSearchRequest();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mp.reset();
				mp.release();
				mp = null;
				mp = new MediaPlayer();
				prog.dismiss();
				doItunesSearchRequest();
			}
            
            mp.setOnPreparedListener(new OnPreparedListener(){
				@Override
				public void onPrepared(MediaPlayer arg0) {
					// TODO Auto-generated method stub
					mp.start();
					prog.dismiss();
				}
            });
            
            mp.setOnCompletionListener(new OnCompletionListener(){
				@Override
				public void onCompletion(MediaPlayer arg0) {
					// TODO Auto-generated method stub
					mp.stop();
					mp.reset();
					mp.release();
					mp = null;
					mp = new MediaPlayer();
					Log.d("MediaPlayer:", "終了");
					doItunesSearchRequest();
				}
            });
            
            mp.setOnErrorListener(new OnErrorListener(){
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					// TODO Auto-generated method stub
					Log.d("setOnErrorListener",String.valueOf(what));
					mp.reset();
					mp.release();
					mp = null;
					mp = new MediaPlayer();
					prog.dismiss();
					ahandler.sendEmptyMessage(0);
					return false;
				}
            });
        }
    };
    
    private void getCheckedDataList(){
    	checkedDataList = null;
    	checkedDataList = new ArrayList<Friend>();
    	UADataList = null;
    	UADataList = new ArrayList<UserArtist>();
    	helper = new SQLiteCheckedDataListOpenHelper(getApplicationContext(),"checkedDataList.db",null, 1);
    	db = helper.getWritableDatabase();
    	sql = "select * from checked_data_list";
    	Cursor c = db.rawQuery(sql, null);
    	boolean isEof = c.moveToFirst();
    	while (isEof) {
    		checkedDataList.add(new Friend(c.getString(0), c.getString(1), null, true));
    		UADataList.add(new UserArtist(c.getString(0), c.getString(1), null));
    		isEof = c.moveToNext();
    	}
    	c.close();
    	db.close();
    	//Collections.shuffle(UADataList);
    }
    
    private void savePlayListToDB(){
    	
    	this.prog = ProgressDialog.show(this, getString(R.string.data_loading_title), getString(R.string.data_loading));
    	
    	new Thread() {
            public void run() {
            	helper2 = new SQLitePlaylistOpenHelper(getApplicationContext(),"playlist.db",null, 1);
            	SQLiteStatement stmt;
            	try{
        			//DBを開く(書き込みモード)
        			db = helper2.getWritableDatabase();
        			//以前にチェックされたユーザのデータを破棄（レコードの削除）
        			db.delete("playlist", "", null);
        			try{
        				db.beginTransaction();
        				stmt = db.compileStatement("INSERT INTO playlist(user_id, user_name, user_profile_img, artist_name, json) VALUES(?, ?, ?, ?, ?);");
        				for (int j = 0; j < playList.size(); j++){
        					stmt.bindString(1, playList.get(j).user_id);
        					stmt.bindString(2, playList.get(j).user_name);
        					stmt.bindString(3, playList.get(j).user_profile_img);
        					stmt.bindString(4, playList.get(j).artist_name);
        					stmt.bindString(5, playList.get(j).json);
        					stmt.executeInsert();
        					Log.d("データ","挿入");
        				}
        				db.setTransactionSuccessful();
        				Log.d("DB保存","完了");
        				playListSQLiteHandler.sendEmptyMessage(0);
        			}catch(Exception e){
        				Log.e("SQLiteException",e.toString());
        				db.close();
        		        ahandler.sendEmptyMessage(0);
        			}finally {
        			    db.endTransaction();
        			    db.close();
        			}
        			db.close();
            	} catch (SQLiteException e){
            		Log.e("SQLiteException",e.toString());
    				db.close();
            	}
            }
        }.start();
    }
    //OTOMAGICのDBからユーザーが好きなアーティストを取得
    private void doOtomagicUserDataRequest(){
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
						ahandler.sendEmptyMessage(0);
					}finally {
		                try {
		                    entity.consumeContent();
		                }
		                catch (IOException e) {
		                    //例外処理
		                }
		            }
					client.getConnectionManager().shutdown();
					Log.d("response:", json);
					
					UADataList = req_otm_data.parseApiUserArtist(json, UADataList);
					if(UADataList != null){
						//データをシャッフル
						for(int i = 0; i < 10; i++){
							Collections.shuffle(UADataList);
						}
						//Log.d("UADataList(0)", UADataList.get(0).user_name.toString() + ":" + UADataList.get(0).artist_name.toString());
						//Log.d("UADataList(1)", UADataList.get(1).user_name.toString() + ":" + UADataList.get(1).artist_name.toString());
						//Log.d("UADataList(2)", UADataList.get(2).user_name.toString() + ":" + UADataList.get(2).artist_name.toString());
						//Log.d("UADataList(3)", UADataList.get(3).user_name.toString() + ":" + UADataList.get(3).artist_name.toString());
						
						requestList = mpl.initPlayList(UADataList);
						//続いてmHandlerにてiTunesAPIにサーチかけるdoItunesRequestメソッドの開始
						mhandler.sendEmptyMessage(0);
					}else{
						prog.dismiss();
						ahandler.sendEmptyMessage(0);
					}
				}else{
					//正常に受信できなかった場合
					prog.dismiss();
					ahandler.sendEmptyMessage(0);
				}
				return null;
			}
 
        };
        
    	this.prog = ProgressDialog.show(this, getString(R.string.data_loading_title), getString(R.string.data_loading_user_friends));
    	 
        // 通信の実行
        new Thread() {
            public void run() {
                try{
                	getCheckedDataList();
                	String req_url = req_otm_data.getUserArtistRequestUrl(UADataList);
                    client = new DefaultHttpClient();
                    HttpGet httpMethod       = new HttpGet(req_url);
                    Log.d("req_url:", req_url);
                    client.execute(httpMethod,response);
                }catch (Exception e) {
                    Log.e("ERROR", e.toString());
                    prog.dismiss();
                    ahandler.sendEmptyMessage(0);
                }
            }
 
        }.start();
    }
    
    private void doItunesSearchRequest() {
    	json = null;
    	Random rnd = new Random();
    	int ran = 0;
    	currentPlayList = new PlayList(null, null, null, null, null);
    	this.prog = ProgressDialog.show(this, getString(R.string.data_loading_title), getString(R.string.data_loading_music));
    	if(requestList.size() != 0){
    		ran = rnd.nextInt(requestList.size());
    		term = requestList.get(ran).artist_name;
    		pl_like_user_id = requestList.get(ran).user_id;
        	pl_like_user_name = requestList.get(ran).user_name;
        	term = term.replaceAll("[ ]","%20");
        	Log.d("term", term);
        	requestList.remove(ran);
        	if(requestList.size() == 0){
        		ran = rnd.nextInt(playList.size());
        		currentPlayList = playList.get(ran);
        	}
    	}else{
    		ran = rnd.nextInt(playList.size());
    		currentPlayList = playList.get(ran);
    	}
    	
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
					
					int p = itunes.parseApiResultsNum(json);
					Random playlist_rnd = new Random();
					p = playlist_rnd.nextInt(p);
					
					artistName = itunes.parseApiArtistName(json, p);
					trackName = itunes.parseApiTrackName(json, p);
					image_url = itunes.parseApiImageUrl(json, p);
					URL url = new URL(image_url);
					bitmap = BitmapFactory.decodeStream(url.openStream());
					preview_url = itunes.parseApiPreviewUrl(json, p);
					collectionViewUrl = itunes.parseApiCollectionViewUrl(json, p);
					Log.d("送信一覧", artistName +":" + trackName +":" + image_url + ":" +preview_url);

					playList.add(new PlayList(pl_like_user_id, pl_like_user_name, artistName, image_url, json));
					Collections.shuffle(playList);
					
					if(artistName == null || trackName == null || preview_url == null){
						mhandler.sendEmptyMessage(0);
					}else{
						try{
							Message pmess  = handler.obtainMessage();
							Bundle bundle  = new Bundle();
							bundle.putString("ARTIST_NAME", artistName);
							bundle.putString("TRACK_NAME", trackName);
							bundle.putString("PREVIEW_URL", preview_url);
							bundle.putString("LIKE_USER_NAME", pl_like_user_name);
							bundle.putString("COLLECTION_URL", collectionViewUrl);
							Log.d("送信し申した","！！");
							pmess.setData(bundle);
							handler.sendMessage(pmess);
						}catch (Exception e) {
							//例外処理
							ahandler.sendEmptyMessage(0);
							Log.d("handlerError:", e.toString());
						}
					}
				}else{
					//正常に受信できなかった場合
					Log.d("Error:", String.valueOf(response.getStatusLine().getStatusCode()));
					prog.dismiss();
					ahandler.sendEmptyMessage(0);
				}
				return null;
			}
 
        };
 
        //this.prog = ProgressDialog.show(this, getString(R.string.data_loading_title), getString(R.string.data_loading_music));
 
        // 通信の実行
        if(requestList.size() != 0){
        	new Thread() {
        		public void run() {
        			try{
        				client = new DefaultHttpClient();
        				HttpGet httpMethod       = new HttpGet("http://itunes.apple.com/search?term="+ term +"&country=" + getString(R.string.country));
        				Log.d("リクエストurl","http://itunes.apple.com/search?term="+ term +"&country=" + getString(R.string.country));
        				client.execute(httpMethod,response);
        			} catch (MalformedURLException e){
        				client.getConnectionManager().shutdown();
        				prog.dismiss();
        				mhandler.sendEmptyMessage(0);
        			} catch (IllegalArgumentException e){
        				client.getConnectionManager().shutdown();
        				prog.dismiss();
        				mhandler.sendEmptyMessage(0);
        			} catch (Exception e) {
        				client.getConnectionManager().shutdown();
        				Log.e("Thread-ERROR", e.toString());
        				prog.dismiss();
        				ahandler.sendEmptyMessage(0);
        			}
        		}
        	}.start();
        }else{
        	new Thread() {
        		public void run() {
        			json = currentPlayList.json;
                	Log.d("currentPlayList:", json);
                	int p = itunes.parseApiResultsNum(json);
        			Random playlist_rnd = new Random();
        			p = playlist_rnd.nextInt(p);
        			
        			artistName = itunes.parseApiArtistName(json, p);
        			trackName = itunes.parseApiTrackName(json, p);
        			image_url = itunes.parseApiImageUrl(json, p);
        			URL url = null;
        			try {
        				url = new URL(image_url);
        			} catch (MalformedURLException e1) {
        				// TODO Auto-generated catch block
        				e1.printStackTrace();
        			}
        			try {
        				bitmap = BitmapFactory.decodeStream(url.openStream());
        			} catch (IOException e1) {
        				// TODO Auto-generated catch block
        				e1.printStackTrace();
        			}
        			preview_url = itunes.parseApiPreviewUrl(json, p);
        			collectionViewUrl = itunes.parseApiCollectionViewUrl(json, p);
        			pl_like_user_name = currentPlayList.user_name;
                	try{
        				Message pmess  = handler.obtainMessage();
        				Bundle bundle  = new Bundle();
        				bundle.putString("ARTIST_NAME", artistName);
        				bundle.putString("TRACK_NAME", trackName);
        				bundle.putString("PREVIEW_URL", preview_url);
        				bundle.putString("LIKE_USER_NAME", pl_like_user_name);
        				bundle.putString("COLLECTION_URL", collectionViewUrl);
        				Log.d("send Message from PlayList","!!");
        				pmess.setData(bundle);
        				//prog.dismiss();
        				handler.sendMessage(pmess);
        			}catch (Exception e) {
        				//例外処理
        				ahandler.sendEmptyMessage(0);
        				Log.d("handlerError:", e.toString());
        			}
        		}
        	}.start();
        }
    }
}
