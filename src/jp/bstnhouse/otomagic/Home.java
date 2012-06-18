package jp.bstnhouse.otomagic;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class Home extends Activity {
    /** Called when the activity is first created. */
	OtmFacebookConf otm_fb_conf = new OtmFacebookConf();
	Facebook facebook = new Facebook(otm_fb_conf.getOtomagicFacebookId());
	AsyncFacebookRunner m_facebook_runner = new AsyncFacebookRunner (facebook);
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        Button start_btn = (Button) findViewById(R.id.start_button);
        start_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getApplication(), SelectFriend.class);
				startActivity(i);
			}
        });
    }
}