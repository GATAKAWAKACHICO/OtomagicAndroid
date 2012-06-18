package jp.bstnhouse.otomagic;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

//import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class GetFriendImageAsyncTask extends AsyncTask<String, Void, Bitmap>{
	private Context m_context;
	private ImageView m_image_view;
	//AsyncTaskでprogressDialogを作ると落ちる
	//private ProgressDialog m_progress;

	public GetFriendImageAsyncTask (Context context, ImageView image)
	{
	    this.m_context = context;
	    this.m_image_view = image;
	}
	@Override
    protected void onPreExecute ()
    {
      // バックグラウンドの処理前にUIスレッドでダイアログ表示
		/*
      m_progress = new ProgressDialog (this.m_context);
      m_progress.setTitle(this.m_context.getString(R.string.data_loading_title));
      m_progress.setMessage(this.m_context.getString(R.string.data_loading));
      m_progress.show();
      */
    }
	
	@Override
	protected Bitmap doInBackground(String... params) {
		// TODO Auto-generated method stub
		synchronized (m_context){
			//キャッシュより画像データを取得
	        Bitmap image = FriendImageCache.get (params[0]);
	        //キャッシュにデータが存在しない場合はwebより画像データを取得
	        try {
	        	if (image == null){
	       			URL image_url = new URL (params[0]);
	       			InputStream is;
	       			is = image_url.openStream ();
	       			image = BitmapFactory.decodeStream (is);
	       			//取得した画像データをキャッシュに保持
	       			FriendImageCache.set (params[0], image);
	       		}
	       		return image;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
	    }          
	}
	
	@Override
    protected void onPostExecute (Bitmap result)
    {
      // 処理中ダイアログをクローズ
      //m_progress.dismiss();

      m_image_view.setImageBitmap (result);
    }

}
