package jp.bstnhouse.otomagic;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


public class ActivityConfig extends Activity implements OnClickListener{
	private LinearLayout ImageViewZoomInLayout;
	private Button ImageViewZoomInLayoutCloseButton;
	private ImageView imageViewZoomin;
    private ImageView facebookImageView1;
    private ImageView facebookImageView2;
    private ImageView facebookImageView3;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.app_config);
        
        ImageViewZoomInLayout = (LinearLayout)findViewById(R.id.ImageViewZoomInLayout);
        ImageViewZoomInLayoutCloseButton = (Button)findViewById(R.id.ImageViewZoomInLayoutCloseButton);
        imageViewZoomin = (ImageView)findViewById(R.id.imageViewZoomin);
        
        facebookImageView1 = (ImageView)findViewById(R.id.facebookImageView1);
        facebookImageView2 = (ImageView)findViewById(R.id.facebookImageView2);
        facebookImageView3 = (ImageView)findViewById(R.id.facebookImageView3);
        
        ImageViewZoomInLayoutCloseButton.setOnClickListener(this);
        facebookImageView1.setOnClickListener(this);
        facebookImageView2.setOnClickListener(this);
        facebookImageView3.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {  
			case R.id.ImageViewZoomInLayoutCloseButton:
				ImageViewZoomInLayout.setVisibility(View.GONE);
				Log.v("tap:", "button");
				break;
	        case R.id.facebookImageView1:  
	            Log.v("tap:", "imageview1");
	            imageViewZoomin.setImageResource(R.drawable.screen1_en);
	            ImageViewZoomInLayout.setVisibility(View.VISIBLE);
	            break;
	        case R.id.facebookImageView2:  
	            Log.v("tap:", "imageview2");
	           	imageViewZoomin.setImageResource(R.drawable.screen2_en);
	           	ImageViewZoomInLayout.setVisibility(View.VISIBLE);
	            break;
	        case R.id.facebookImageView3:  
	        	Log.v("tap:", "imageview3");
	        	imageViewZoomin.setImageResource(R.drawable.screen3_en);
	           	ImageViewZoomInLayout.setVisibility(View.VISIBLE);
	            break;
	        default:  
	        	Log.v("tap:", "def");
	            break;
	        } 
	}
}
