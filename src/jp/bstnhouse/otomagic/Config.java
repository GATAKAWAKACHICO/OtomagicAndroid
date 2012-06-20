package jp.bstnhouse.otomagic;

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class Config extends Activity{
	private AlertDialog.Builder alertDialog;
	private SharedPreferences mPrefs;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.app_config);
        
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.play_background_radiogroup);
        RadioButton playBgOn = (RadioButton) findViewById(R.id.play_background_on);
        RadioButton playBgOff = (RadioButton) findViewById(R.id.play_background_off);
        mPrefs = getSharedPreferences("Config",MODE_PRIVATE);
        boolean play_background_flg = mPrefs.getBoolean("config_play_background", true);
        if(play_background_flg) {
        	playBgOn.setChecked(play_background_flg);
        }else{
        	playBgOff.setChecked(play_background_flg);
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            // ラジオグループのチェック状態が変更された時に呼び出されます
            // チェック状態が変更されたラジオボタンのIDが渡されます
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = (RadioButton) findViewById(R.id.play_background_on);
                mPrefs = getSharedPreferences("Config",MODE_PRIVATE);
  	          	SharedPreferences.Editor editor = mPrefs.edit();
  	          	boolean play_background_flg;
  	          	if(radioButton.isChecked() == true) {
  	          		// チェックされた状態の時の処理を記述
  	          		play_background_flg = true;
	          		Log.d("PB","ONになりました。");
  	          	}else {
  		  			// チェックされていない状態の時の処理を記述
  		  			play_background_flg = false;
  		  			Log.d("PB","OFFになりました。");
  	          	}
  	          	editor.putBoolean("config_play_background", play_background_flg);
  	          	editor.commit();
            }
        });
        
        Button reflect_btn = (Button) findViewById(R.id.reflect_button);
        reflect_btn.setOnClickListener(new View.OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
    			// TODO Auto-generated method stub
    			alertDialog.show();
    		}
        });
        
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.warn_title));
        alertDialog.setMessage(getString(R.string.config_reflect_ask));
        alertDialog.setIcon(drawable.stat_notify_error);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface arg0, int arg1) {
    			// TODO Auto-generated method stub
    			
    		}
        });
        
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface arg0, int arg1) {
    			// TODO Auto-generated method stub

    		}
        });
    }
}
