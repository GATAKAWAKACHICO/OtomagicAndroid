package jp.bstnhouse.otomagic;

import android.app.Activity;
import android.widget.LinearLayout;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class ConfAdmob extends Activity{
	private AdView adView;
	private String OTOMAGIC_ADMOB_ID = "a14fd957c8b53d2";
	
	public String getOtomagicAdmobId(){
		return OTOMAGIC_ADMOB_ID;
	}
	
	public void getTestAdView(AdView adView, LinearLayout layout){
        // adView を追加
        layout.addView(adView);
        AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdRequest.TEST_EMULATOR);// エミュレータ
        //adRequest.addTestDevice("TEST_DEVICE_ID");
        // 一般的なリクエストを行って広告を読み込む
        adView.loadAd(adRequest);
	}
}
