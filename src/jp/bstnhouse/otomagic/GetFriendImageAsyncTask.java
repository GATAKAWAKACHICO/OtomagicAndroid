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
import android.view.View;
import android.widget.ImageView;

public class GetFriendImageAsyncTask extends AsyncTask<String, Void, Bitmap>{
	private Context m_context;
	private ImageView m_image_view;
	private String m_tag;
	//AsyncTask��progressDialog�����Ɨ�����
	//private ProgressDialog m_progress;

	public GetFriendImageAsyncTask (Context context, ImageView image)
	{
	    this.m_context = context;
	    this.m_image_view = image;
	    // ImageView �ɐݒ肵���^�O�������o��  
        this.m_tag = m_image_view.getTag().toString(); 
	}
	@Override
    protected void onPreExecute ()
    {
      // �o�b�N�O���E���h�̏����O��UI�X���b�h�Ń_�C�A���O�\��
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
			//�L���b�V�����摜�f�[�^���擾
	        Bitmap image = FriendImageCache.get (params[0]);
	        //�L���b�V���Ƀf�[�^�����݂��Ȃ��ꍇ��web���摜�f�[�^���擾
	        try {
	        	if (image == null){
	       			URL image_url = new URL (params[0]);
	       			InputStream is;
	       			is = image_url.openStream ();
	       			image = BitmapFactory.decodeStream (is);
	       			//�擾�����摜�f�[�^���L���b�V���ɕێ�
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
      // �������_�C�A���O���N���[�Y
      //m_progress.dismiss();
		// �����o�̃^�O�� imageView �ɃZ�b�g�����^�O����v�����  
        // �摜���Z�b�g���� 
        if (this.m_tag.equals(this.m_image_view.getTag())) {  
            if (result != null) {  
                this.m_image_view.setImageBitmap(result);  
                this.m_image_view.setVisibility(View.VISIBLE);  
            }  
        }
    }

}
