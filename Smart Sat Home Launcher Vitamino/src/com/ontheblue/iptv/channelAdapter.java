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
 
public class channelAdapter extends BaseAdapter {
    private Context appContext;
 
    // Keep all Images in array
    public JSONArray MyChannels;
    public Drawable img;
    final String TAG = "IPTV channelAdapter";
    
 
    // Constructor
    public channelAdapter(Context c, IPTV iptv){
        appContext = c;
        iptv.list();
    	this.MyChannels = iptv.MyChannels;
    	Log.i(TAG,"total channels is "+this.MyChannels.length());
    }
 
    @Override
    public int getCount() {
        return this.MyChannels.length();//200;//
    }
 
    @Override
    public JSONObject getItem(int position) {
    	JSONObject channel = null;
        try {
			channel = MyChannels.getJSONObject(position);
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
			channel = MyChannels.getJSONObject(position);
			res = (Integer) channel.get("ID");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return res;
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
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
    		txtView.setBackgroundColor(0x80808000);
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
