package jp.bstnhouse.otomagic;

import android.app.Activity;
import android.widget.LinearLayout;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class ConfAdmob extends Activity{
	private String OTOMAGIC_ADMOB_ID = "a14fd957c8b53d2";
	
	public String getOtomagicAdmobId(){
		return OTOMAGIC_ADMOB_ID;
	}
	
	public void getTestAdView(AdView adView, LinearLayout layout){
        // adView ��ǉ�
        layout.addView(adView);
        AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdRequest.TEST_EMULATOR);// �G�~�����[�^
        //adRequest.addTestDevice("TEST_DEVICE_ID");
        // ��ʓI�ȃ��N�G�X�g���s���čL����ǂݍ���
        adView.loadAd(adRequest);
	}
	
	public void getProductionAdview(AdView adView, LinearLayout layout){
		layout.addView(adView);
		adView.loadAd(new AdRequest());
	}
}
