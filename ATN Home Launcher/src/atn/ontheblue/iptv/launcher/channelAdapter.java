package atn.ontheblue.iptv.launcher;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.AQuery;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class channelAdapter extends BaseAdapter {
    private HomeActivity appContext;
 
    // Keep all Images in array
    public JSONArray MyChannels;
    public Drawable img;
    final String TAG = "IPTV channelAdapter";    
 
    // Constructor
    public channelAdapter(HomeActivity c){
    	if(c.iptv == null){
    		c.iptv=new IPTV(c);
    	}   	
        appContext = c;
        c.iptv.list(this);
    }
 
    public int getCount() {
        return this.MyChannels.length();//200;//
    }
 
    public JSONObject getItem(int position) {
    	JSONObject channel = null;
        try {
			channel = MyChannels.getJSONObject(position);
		} catch (JSONException e) {
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
			Log.e(TAG,"Error Json getChannelId "+e.toString());
			e.printStackTrace();
		}
        return res;
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	ViewHolder holder;
		if(convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(appContext);
			if(appContext.channelsAsIcons==true){
				convertView = inflater.inflate(R.layout.program_info_layout, null);
			}else{
				convertView = inflater.inflate(R.layout.program_info_layout_noico, null);
			}
			holder = new ViewHolder();
			holder.prgIco = (ImageView) convertView.findViewById(R.id.prgIcon);
            holder.tvName = (TextView)convertView.findViewById(R.id.prgName);
            convertView.setTag(holder);
		}else{
            holder = (ViewHolder) convertView.getTag();
		}
		try {    			
			AQuery aq = new AQuery(convertView);
			String thumbnail = this.getItem(position).getString("Logo");			
			aq.id(holder.prgIco).image(thumbnail,false,true);
			holder.tvName.setText(this.getItem(position).getString("Name"));
		}catch (JSONException e) {
			Log.e(TAG,e.toString());
			e.printStackTrace();
		}
		return convertView;    	
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
 
    static class ViewHolder {
        TextView tvName;
        ImageView prgIco;
    }
}
