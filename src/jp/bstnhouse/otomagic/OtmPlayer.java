package jp.bstnhouse.otomagic;

import java.io.IOException;
import java.net.URL;

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

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class OtmPlayer extends Activity implements OnClickListener{
	private TextView artist_name;
	private TextView song_title_name;
	private TextView like_user_name;
	private ImageView artist_imageview;
	private ProgressDialog prog;
	Itunes itunes = new Itunes();
	private String json = null;
	private String image_url;
	private String artistName;
	private String trackName;
	private Bitmap bitmap;
	private DefaultHttpClient client;
	private HttpEntity entity;
	private MediaPlayer mp;
	private String preview_url;
	private int resNum = 0;
	private AdView adView;
	OtmAdmobConf otm_admob = new OtmAdmobConf();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.player);
        artist_imageview = (ImageView) findViewById(R.id.discImageView);
        artist_name = (TextView) findViewById(R.id.artistNameTextView);
        song_title_name = (TextView) findViewById(R.id.songTitleTextView);
        like_user_name = (TextView) findViewById(R.id.likeUserNameTextView);
        findViewById(R.id.next_button).setOnClickListener(this);
        findViewById(R.id.back_button).setOnClickListener(this);
        
        adView = new AdView(this, AdSize.BANNER, otm_admob.getOtomagicAdmobId());
        LinearLayout layout = (LinearLayout)findViewById(R.id.AdLayout);
        otm_admob.getTestAdView(adView, layout);
        
        doRequest();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {  
        // ボタンIDごとに処理を実装する  
        case R.id.next_button:  
            // 処理
        	if(mp.isPlaying()){
        		mp.stop();
				mp.reset();
				mp.release();
				Log.d("MediaPlayer:", "終了");
        	}
        	doRequest();
        	break;
        case R.id.back_button:
        	if(mp.isPlaying()){
        		mp.seekTo(0);
        	}
        	break;
        }
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(mp.isPlaying()){
			mp.stop();
		}
		Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		if(!mp.isPlaying()){
			try {
				mp.prepare();
				mp.start();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Toast.makeText(this, "onRestart", Toast.LENGTH_SHORT).show();
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
		Toast.makeText(this, "onDestory", Toast.LENGTH_SHORT).show();
	}
	
	private final Handler handler = new Handler(){	 
        /**
         * レスポンス取得でUIを更新する
         */
        public void handleMessage(Message msg) {
            artist_imageview.setImageBitmap(bitmap);
            String bmess = msg.getData().getString("ARTIST_NAME");
            artist_name.setText(bmess);
            String cmess = msg.getData().getString("TRACK_NAME");
            song_title_name.setText(cmess);
            String dmess = msg.getData().getString("PREVIEW_URL");
            like_user_name.setText("Masaki Wakatake likes this.");
            mp = new MediaPlayer();
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
            mp.prepareAsync();
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
					//mp.reset();
					//mp.release();
					Log.d("MediaPlayer:", "終了");
				}
            });
        }
    };

    private void doRequest() {
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
					
					resNum++;
					artistName = itunes.parseApiArtistName(json, resNum);
					trackName = itunes.parseApiResponse(json, resNum);
					image_url = itunes.parseApiTestImageUrl(json, resNum);
					URL url = new URL(image_url);
					bitmap = BitmapFactory.decodeStream(url.openStream());
					preview_url = itunes.parseApiTesPreviewUrl(json, resNum);
					
					try{
						//json = EntityUtils.toString(entity);
				        Message pmess  = handler.obtainMessage();
				        Bundle bundle  = new Bundle();
				        bundle.putString("ARTIST_NAME", artistName);
				        bundle.putString("TRACK_NAME", trackName);
				        bundle.putString("PREVIEW_URL", preview_url);
				        pmess.setData(bundle);
				        handler.sendMessage(pmess);
				    }catch (Exception e) {
		                //例外処理
		            }
				}else{
					//正常に受信できなかった場合
					prog.dismiss();
				}
				return null;
			}
 
        };
 
        this.prog = ProgressDialog.show(this, getString(R.string.data_loading_title), getString(R.string.data_loading));
 
        // 通信の実行
        new Thread() {
            public void run() {
                try{
                    client = new DefaultHttpClient();
                    HttpGet httpMethod       = new HttpGet("http://itunes.apple.com/search?term=放課後ティータイム&country=jp");
                    client.execute(httpMethod,response);
                }catch (Exception e) {
                    Log.e("ERROR", e.toString());
                    prog.dismiss();
                }
                //client.getConnectionManager().shutdown();
            }
 
        }.start();
 
    }
}
