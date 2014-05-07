package atn.ontheblue.iptv.launcher;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;


import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class IPTV {
	//private AQuery aq;
	
	//http://api.arabtvnet.tv/channels?email=aminhamzeh89%40gmail.com&password=cf5b05d8d1cba4bd643cc8c1e949e02d&package=15
	final static String TAG = "IPTV Class";
	/*private String email= "otbotr@gmail.com";
	private String drowp= "e10adc3949ba59abbe56e057f20f883e";*/
	private static String email= "aminhamzeh89@gmail.com";
	private static String drowp= "cf5b05d8d1cba4bd643cc8c1e949e02d";
	private static String pass= "5501160377m";
	//http://api.arabtvnet.tv/channels?email=aiham%40ontheblue.se&password=e10adc3949ba59abbe56e057f20f883e&package=15
	//private static String email= "aiham@ontheblue.se";
	//private static String pass= "123456";
	//private static String drowp= "e10adc3949ba59abbe56e057f20f883e";
	public static int Pack=15;
	public static String Name="";
	public static String Status="";
	public static JSONArray MyChannels = null;
	public static JSONArray favoritesChannels = null;
	public static JSONObject currentChannel = null;
	public static int currentChannelPlace = 0;

	public static HomeActivity context;
	
	public IPTV(HomeActivity c){
		IPTV.context = c;
		Runnable r = new Runnable() {
            public void run() {
            	if(authenticate()){
            		Log.i(TAG, "after authenticate the status is : "+Status);
            	};
            }
        };
        new Thread(r).start();
	}
	
	public String getURLauthenticate(){
    	//return "http://api.arabtvnet.tv/authenticate?email="+IPTV.email+"&password="+IPTV.drowp;
    	return "http://api.arabtvnet.tv/authenticate?email="+IPTV.email+"&password="+IPTV.md5(IPTV.pass.toString());
    }
	public String getURLlogin(){
		TextView email = (TextView) context.findViewById(R.id.email);
		TextView pass = (TextView) context.findViewById(R.id.pass);
		if(email.getText()!="" && pass.getText()!=""){
			return "http://api.arabtvnet.tv/authenticate?email="+email.getText()+"&password="+IPTV.md5(pass.getText().toString());
		}
    	return "";
    }
    public String getURLlist(){
    	return "http://api.arabtvnet.tv/channels?email="+IPTV.email+"&password="+IPTV.drowp+"&package="+IPTV.Pack; 
    }
    public static String getURLfavorites(){
    	return "http://api.arabtvnet.tv/get_favorites?email="+IPTV.email+"&password="+IPTV.drowp;
    }
    public static String getURLSetfavorites(){
    	return "http://api.arabtvnet.tv/set_favorites?email="+IPTV.email+"&password="+IPTV.drowp;
    }
    public String getURLplay(int channel){
    	return "http://api.arabtvnet.tv/channel.php?package="+IPTV.Pack+"&email="+IPTV.email+"&password="+IPTV.drowp+"&channel="+channel;
    }
    public String getStream(){
    	/*String channel_id = null;
		try {
			channel_id = this.currentChannel.getString("ID");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
    	final AQuery aq = new AQuery(this.context);
    	
    	aq.ajax(this.getURLplay(Integer.parseInt(channel_id)), JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                if(json != null){
                	try {
						json.getString("Message");
					} catch (JSONException e) {							
						e.printStackTrace();
					}
                }
            }
    	});*/
    	
    	try {
    		String the_stream = this.getURLplay(Integer.parseInt(IPTV.currentChannel.getString("ID")));
    		Log.d(TAG,the_stream + "to play");
    		JSONObject json = JSONUtils.getJSONfromURL(the_stream);
    		return json.getString("Message");
		} catch (JSONException e) {
			Log.e(TAG,e.toString());
			e.printStackTrace();			
		}
    	return "";
    }
    public void login(){
    	final AQuery aq = new AQuery(IPTV.context);
    	if(this.getURLlogin()!=""){
	    	aq.ajax(this.getURLlogin(), JSONObject.class, new AjaxCallback<JSONObject>() {
	            @Override
	            public void callback(String url, JSONObject json, AjaxStatus status) {
	                if(json != null){
	                	try {
	            			IPTV.Name = json.getString("Name");
	            			IPTV.Status = json.getString("Status");
	            			IPTV.Pack = Integer.parseInt(json.getString("PackageID"));
	            			Log.i(TAG, "name = "+IPTV.Name+" status="+IPTV.Status+" pack="+IPTV.Pack);
	            			SharedPreferences preferences = context.getPreferences(Activity.MODE_PRIVATE);
	            			SharedPreferences.Editor editor = preferences.edit();
	            			editor.putString("Name", IPTV.Name);
	            			editor.putString("Status", IPTV.Status);
	            			editor.putInt("Pack", IPTV.Pack);
	            			editor.commit();
	            		} catch (JSONException e) {
	            			Log.e(TAG,e.toString());
	            			e.printStackTrace();
	            		}
	                	Log.i(TAG,json.toString());
	                }else{
	                	Toast.makeText(aq.getContext(), "Error:" + status.getCode(), Toast.LENGTH_LONG).show();
	                }
	            }
	    	});
    	}else{    		
    		Toast.makeText(aq.getContext(), "Error: Please Enter User NAme and Password" , Toast.LENGTH_LONG).show();
    	}
    }
    private boolean authenticate(){
    	Log.i(TAG, "Authenticate started...");
    	JSONObject json = JSONUtils.getJSONfromURL(this.getURLauthenticate());    	
    	try {
			IPTV.Name = json.getString("Name");
			IPTV.Status = json.getString("Status");
			IPTV.Pack = Integer.parseInt(json.getString("PackageID"));
			Log.i(TAG, "name = "+IPTV.Name+" status="+IPTV.Status+" pack="+IPTV.Pack);
			SharedPreferences preferences = context.getPreferences(Activity.MODE_PRIVATE);//PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString("email", IPTV.email);
			editor.putString("pass", IPTV.drowp);
			editor.putInt("pack", IPTV.Pack);
			editor.commit();
			return true;
		} catch (JSONException e) {
			Log.e(TAG,e.toString());
			e.printStackTrace();
		}
    	Log.i(TAG,json.toString());
    	return false;
    }
    
    public boolean list(final channelAdapter chn){
    	final AQuery aq = new AQuery(IPTV.context);
    	Log.d(TAG,this.getURLlist());
    	aq.ajax(this.getURLlist(), JSONArray.class, new AjaxCallback<JSONArray>() {
            @Override
            public void callback(String url, JSONArray json, AjaxStatus status) {
                if(json != null){
                	MyChannels = json;
                	chn.MyChannels = MyChannels;
                	context.programLayout.setAdapter(chn);
                	try {
                		/*if(context.boot){
                			SharedPreferences preferences = context.getPreferences(Activity.MODE_PRIVATE);
                	    	String last = preferences.getString("last", "");
                	    	if(last!=""){
                	    		currentChannel = MyChannels.getJSONObject(Integer.parseInt(last));
                	    	}
                		}else{
                			currentChannel = MyChannels.getJSONObject(1);
                		}*/
                		currentChannel = MyChannels.getJSONObject(1);
					} catch (JSONException e) {
						Log.e(TAG,"JSON error : "+e.toString());
						e.printStackTrace();
					}
                	context.playChannel();
                }else{
                	Toast.makeText(aq.getContext(), "Error:" + status.getCode(), Toast.LENGTH_LONG).show();
                }
            }
    	});          
    	return false;
    }
    public void responseHandlerList(){
    	
    }
    public static void listFavorites(final favoritesAdapter fav){
    	Log.i(TAG, "ListFavorites GET started...");
    	final AQuery aq = new AQuery(IPTV.context);
    	aq.ajax(IPTV.getURLfavorites(), JSONArray.class, new AjaxCallback<JSONArray>() {
            @Override
            public void callback(String url, JSONArray json, AjaxStatus status) {
                if(json != null){                	                	
                	favoritesChannels = json;                	
                	fav.FavoritesChannels = favoritesChannels;
                	context.favoritesLayout.setAdapter(fav);
                }else{
                	Toast.makeText(aq.getContext(), "Error:" + status.getCode(), Toast.LENGTH_LONG).show();
                }
            }
    	});
    }
    public static void setFavorites(final favoritesAdapter fav){
    	String list = "";
    	for(int i=0; i<IPTV.favoritesChannels.length();i++){
    		try {
				JSONObject chan = (JSONObject) IPTV.favoritesChannels.get(i);
				list+=chan.getString("ID")+",";
			} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
    	list = list.substring(0, list.length() - 1);
    	final AQuery aq = new AQuery(context);
    	aq.ajax(IPTV.getURLSetfavorites()+"&list="+list, JSONArray.class, new AjaxCallback<JSONArray>() {
            @Override
            public void callback(String url, JSONArray json, AjaxStatus status) {
                listFavorites(fav);
            }
    	});
    }
    /*
     * UTILS
     */
    public static boolean isNumeric(String str){
    	try{
    		Double.parseDouble(str);
    	}catch(NumberFormatException nfe){
    		return false;
    	}
    	return true;
    }
    public static String md5(String s){
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes(),0,s.length());
            String hash = new BigInteger(1, digest.digest()).toString(16);
            return hash;
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return "";
    }
}
