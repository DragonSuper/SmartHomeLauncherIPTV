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
 
public class favoritesAdapter extends BaseAdapter {
	private HomeActivity appContext;
 
    // Keep all Images in array
    public JSONArray FavoritesChannels;
    public Drawable img;
    final String TAG = "IPTV favoritesAdapter";    
 
    // Constructor
    public favoritesAdapter(HomeActivity c){        
        if(c.iptv==null){
        	c.iptv=new IPTV(c);
        }
        appContext = c;
        IPTV.listFavorites(this);
    }
 
    public int getCount() {
        return this.FavoritesChannels.length();//200;//
    }
 
    public JSONObject getItem(int position) {
    	JSONObject channel = null;
        try {
			channel = FavoritesChannels.getJSONObject(position);
		} catch (JSONException e) {
			Log.e(TAG,e.toString());
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
			Log.e(TAG,e.toString());
			e.printStackTrace();
		}
        return res;
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	FavViewHolder holder;
		if(convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(appContext);
			if(appContext.channelsAsIcons==true){
				convertView = inflater.inflate(R.layout.program_info_layout, null);
			}else{
				convertView = inflater.inflate(R.layout.program_info_layout_noico, null);
			}
			holder = new FavViewHolder();
            holder.prgIco = (ImageView) convertView.findViewById(R.id.prgIcon);
            holder.tvName = (TextView)convertView.findViewById(R.id.prgName);
            convertView.setTag(holder);
		}else{
            holder = (FavViewHolder) convertView.getTag();
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
    static class FavViewHolder {
        TextView tvName;
        ImageView prgIco;
    }
}
