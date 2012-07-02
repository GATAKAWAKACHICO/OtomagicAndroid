package jp.bstnhouse.otomagic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ActivityPlayerArrangeList extends ListActivity{
	private static String[] items={"lorem", "ipsum", "dolor", "sit", "amet",
		"consectetuer", "adipiscing", "elit", "morbi", "vel",
		"ligula", "vitae", "arcu", "aliquet", "mollis",
		"etiam", "vel", "erat", "placerat", "ante",
		"porttitor", "sodales", "pellentesque", "augue", "purus"};
	//private String[] trackNames = {};
	
	private IconicAdapter adapter = null;
	//private PlayListAdapter adapter = null;
	private ArrayList<String> array = new ArrayList<String>(Arrays.asList(items));
	private ArrayList<String> trackNamesArray = new ArrayList<String>();
	private ArrayList<String> artistNamesArray = new ArrayList<String>();
	RequestItunes itunes = new RequestItunes();
	String json;
	String artistName;
	String trackName;
	
	private SQLiteDatabase db;
	private SQLitePlaylistOpenHelper helper2;
	String sql = "";
	private ProgressDialog prog;
	
	static List<PlayList> playList = new ArrayList<PlayList>();

	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.player_arrangelist);
        
        getPlaylist();
        
        array.add("A");
        array.add("B");
        array.add("C");
        
        TouchListView tlv = (TouchListView) getListView();
        adapter = new IconicAdapter();
        setListAdapter(adapter);
 
        tlv.setDropListener(onDrop);
        tlv.setRemoveListener(onRemove);
	}
	
	private Handler mhandler = new Handler(){
		public void handleMessage(Message msg) {
			adapter.notifyDataSetChanged();
			prog.dismiss();
		}
	};
	
	private void getPlaylist(){
		this.prog = ProgressDialog.show(this, getString(R.string.data_loading_title), getString(R.string.arrangelist_load));
		new Thread() {
            public void run() {
            	playList = new ArrayList<PlayList>();
		
            	helper2 = new SQLitePlaylistOpenHelper(getApplicationContext(),"playlist.db",null, 1);
            	db = helper2.getWritableDatabase();
            	sql = "select * from playlist";
            	Cursor c = db.rawQuery(sql, null);
            	boolean isEof = c.moveToFirst();
            	while (isEof) {
            		playList.add(new PlayList(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)));
            		isEof = c.moveToNext();
            	}
            	c.close();
            	db.close();
    	
            	//�v���C���X�g����item�ւ�String�z��ɃA�[�e�B�X�g�����i�[
            	for (int i = 0; i < playList.size(); i++){
            		json = null;
            		json = playList.get(i).json;
            		artistName = playList.get(i).user_profile_img;//�I�u�W�F�N�g����user_profile_img�����ǎd�l��artist_name���i�[����Ă�i�΁j
            		int res_num = itunes.parseApiResultsNum(json);
            		if(res_num != 0){
            			for(int j = 0; j < res_num; j++){
            				trackName = itunes.parseApiTrackName(json, j);
            				if(trackName != null){
            					trackNamesArray.add(trackName);
            					artistNamesArray.add(artistName);
            				}
            			}
            		}
            	}
            	Log.d("trackNamesArray", trackNamesArray.toString());
                Log.d("artistNamesArray", artistNamesArray.toString());
            	mhandler.sendEmptyMessage(0);
            }
		}.start();
	}
	
	private TouchListView.DropListener onDrop = new TouchListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            String item = adapter.getItem(from);
            String item2 = artistNamesArray.get(from);
            //ArrayList�̏���
            trackNamesArray.remove(from);
            trackNamesArray.add(to, item);
            artistNamesArray.remove(from);
            artistNamesArray.add(to, item2);
            
            //UI��Listview�s�̏���
            adapter.remove(item);
            adapter.insert(item, to);
            
            Log.d("trackNamesArray", trackNamesArray.toString());
            Log.d("artistNamesArray", artistNamesArray.toString());
        }
    };
    
    private TouchListView.RemoveListener onRemove = new TouchListView.RemoveListener() {
        @Override
        public void remove(int which) {
        	//ArrayList�̏���
            trackNamesArray.remove(which);
            artistNamesArray.remove(which);
        	
        	//UI��Listview�s�̏���
            adapter.remove(adapter.getItem(which));
        }
    };
    
    class IconicAdapter extends ArrayAdapter<String> {
        IconicAdapter() {
            //super(ActivityPlayerArrangeList.this, R.layout.player_arrangelist_row, array);
        	super(ActivityPlayerArrangeList.this, R.layout.player_arrangelist_row, trackNamesArray);
        }
 
        public View getView(int position, View convertView,
                ViewGroup parent) {
            View row = convertView;
 
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
 
                row = inflater.inflate(R.layout.player_arrangelist_row, parent, false);
            }
 
            TextView song_label = (TextView) row.findViewById(R.id.song_label);
            TextView artist_label = (TextView) row.findViewById(R.id.artist_label);
 
            song_label.setText(trackNamesArray.get(position));
            artist_label.setText(artistNamesArray.get(position));
            return (row);
        }
    }
    
}
