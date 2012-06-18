package jp.bstnhouse.otomagic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.facebook.android.FacebookError;

public class MeRequestListener implements com.facebook.android.AsyncFacebookRunner.RequestListener
{
	@Override
	public void onComplete(String response, Object state) {
		// TODO Auto-generated method stub
		try
        {
          Log.d ("Facebook", "Friends-Request : response.length(): " + response.length ());
          Log.d ("Facebook", "Friends-Request : Response: " + response);

          final JSONObject json = new JSONObject (response);
          //JSONObject json_name = json.getJSONObject("name");
          String name = json.getString ("name");
          Log.d ("Facebook-Friends-Request", "d.length (): " + name);
        }
        catch (JSONException e)
        {
          Log.d ("Facebook", "Friends-Request : JSON Error in response: "+ e);
        }
	}

	@Override
	public void onIOException(IOException e, Object state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFileNotFoundException(FileNotFoundException e, Object state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMalformedURLException(MalformedURLException e, Object state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFacebookError(FacebookError e, Object state) {
		// TODO Auto-generated method stub
		
	}

}
