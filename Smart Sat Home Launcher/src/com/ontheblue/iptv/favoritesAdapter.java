package com.ontheblue.iptv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
 
public class favoritesAdapter extends BaseAdapter {
    private Context appContext;
 
    // Keep all Images in array
    public JSONArray FavoritesChannels;
    public Drawable img;
    final String TAG = "IPTV favoritesAdapter";
    
 
    // Constructor
    public favoritesAdapter(Context c, IPTV iptv){
        appContext = c;
        iptv.listFavorites();
    	this.FavoritesChannels = iptv.favoritesChannels;
    	Log.i(TAG,"total favoriteschannels is "+this.FavoritesChannels.length());
    }
 
    public int getCount() {
        return this.FavoritesChannels.length();//200;//
    }
 
    public JSONObject getItem(int position) {
    	JSONObject channel = null;
        try {
			channel = FavoritesChannels.getJSONObject(position);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return channel;
    }
    public int getChannelId(int position) {
    	JSONObject channel = null;
    	int res = 0;
        try {
			channel = FavoritesChannels.getJSONObject(position);
			res = (Integer) channel.get("ID");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return res;
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	TextView txtView;
    	if(convertView==null){
    		txtView = new TextView(appContext);
    	}else{
    		txtView = (TextView) convertView;
    	}
    	try {
    		txtView.setText((position+1)+". "+this.getItem(position).getString("Name"));
    		txtView.setTextSize(24);
    		txtView.setPadding(3, 3, 3, 3);
    		txtView.setSingleLine(true);
    		txtView.setEllipsize(null);
    		txtView.setMaxLines(1);
			/*img = remoteImage(this.getItem(position).getString("Logo"));			
			imageView.setImageDrawable(img);*/
		}catch (JSONException e) {
			Log.e(TAG,e.toString());
			e.printStackTrace();
		}	
    	/*ImageView imageView;
    	if(convertView==null){
	        imageView = new ImageView(appContext);
    	}else{
    		imageView = (ImageView) convertView;
    	}
        try {        	
			img = remoteImage(this.getItem(position).getString("Logo"));			
			imageView.setImageDrawable(img);
		}catch (JSONException e) {
			Log.e(TAG,e.toString());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(70, 70));
        return imageView;*/
		return txtView;
    }
    /*
	 * IMAGE Fetch
	 */
	/*private static Drawable remoteImage(String url){
		try{
			InputStream is = (InputStream) new URL(url).getContent();
			Drawable d = Drawable.createFromStream(is, "src name");
			return d;
		}catch (Exception e) {
			System.out.println("Exc="+e);
			return null;
		}
	}*/
 
}
