package com.ontheblue.iptv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class IPTV {
	//http://api.arabtvnet.tv/channels?email=aminhamzeh89%40gmail.com&password=cf5b05d8d1cba4bd643cc8c1e949e02d&package=15
	final String TAG = "IPTV Class";
	/*private String email= "otbotr@gmail.com";
	private String drowp= "e10adc3949ba59abbe56e057f20f883e";*/
	//private String email= "aminhamzeh89@gmail.com";
	//private String drowp= "cf5b05d8d1cba4bd643cc8c1e949e02d";
	//http://api.arabtvnet.tv/channels?email=aiham%40ontheblue.se&password=e10adc3949ba59abbe56e057f20f883e&package=15
	private String email= "aiham@ontheblue.se";
	private String drowp= "e10adc3949ba59abbe56e057f20f883e";
	public int Pack=15;
	public String Name="";
	public String Status="";
	public JSONArray MyChannels = null;
	public JSONArray favoritesChannels = null;
	public JSONObject currentChannel = null;
	public int currentChannelPlace = 0;
	
	public IPTV(TvHome c){
		Runnable r = new Runnable() {
            public void run() {
            	if(authenticate()){
            		Log.i(TAG, "after authenticate the status is : "+Status);
            		/*if(list()){
            			try {
        					Log.i(TAG,"MyFirstChannel = "+MyChannels.get(0).toString());
        					JSONObject channel = MyChannels.getJSONObject(0);
        					currentChannel = channel;
        					currentChannelPlace = 0;
        				} catch (JSONException e) {
        					Log.e(TAG,e.toString());
        					e.printStackTrace();
        				}
            		};*/
            	};
            }
        };
        new Thread(r).start();
	}
	
	public String getURLauthenticate(){
    	return "http://api.arabtvnet.tv/authenticate?email="+this.email+"&password="+this.drowp;
    }
    public String getURLlist(){
    	return "http://api.arabtvnet.tv/channels?email="+this.email+"&password="+this.drowp+"&package="+this.Pack; 
    }
    public String getURLfavorites(){
    	return "http://api.arabtvnet.tv/get_favorites?email="+this.email+"&password="+this.drowp;
    }
    public String getURLSetfavorites(){
    	return "http://api.arabtvnet.tv/set_favorites?email="+this.email+"&password="+this.drowp;
    }
    public String getURLplay(int channel){
    	return "http://api.arabtvnet.tv/channel.php?package="+this.Pack+"&email="+this.email+"&password="+this.drowp+"&channel="+channel;
    }
    public String getStream(){
    	try {
    		JSONObject json = JSONUtils.getJSONfromURL(this.getURLplay(Integer.parseInt(this.currentChannel.getString("ID"))));
    		return json.getString("Message");
		} catch (JSONException e) {
			Log.e(TAG,e.toString());
			e.printStackTrace();			
		}
    	return "";
    }
    private boolean authenticate(){
    	Log.i(TAG, "Authenticate started...");
    	JSONObject json = JSONUtils.getJSONfromURL(this.getURLauthenticate());    	
    	try {
			this.Name = json.getString("Name");
			this.Status = json.getString("Status");
			this.Pack = Integer.parseInt(json.getString("PackageID"));
			Log.i(TAG, "name = "+this.Name+" status="+this.Status+" pack="+this.Pack);
			return true;
		} catch (JSONException e) {
			Log.e(TAG,e.toString());
			e.printStackTrace();
		}
    	Log.i(TAG,json.toString());
    	return false;
    }
    
    public boolean list(){
    	Log.i(TAG, "List GET started...");
    	JSONArray json = JSONUtils.getJSONArrfromURL(this.getURLlist());
    	//Log.i(TAG,json.toString());
    	this.MyChannels = json;
    	try {
			this.currentChannel = this.MyChannels.getJSONObject(0);
			Log.d(TAG,this.currentChannel.toString());
		} catch (JSONException e) {
			Log.e(TAG,e.toString());
			e.printStackTrace();
		}
    	/*if(this.MyChannels.length()>0){
    		thisTvHome.initProgramList(thisTvHome,this);
    		return true;
    	} */   	
    	return false;
    }
    public void listFavorites(){
    	Log.i(TAG, "ListFavorites GET started...");
    	JSONArray json = JSONUtils.getJSONArrfromURL(this.getURLfavorites());
    	Log.i(TAG,json.toString());
    	this.favoritesChannels = json;
    }
    public boolean setFavorites(){
    	//http://api.arabtvnet.tv/set_favorites?email=aiham%40ontheblue.se&password=e10adc3949ba59abbe56e057f20f883e&list=264%2C437%2C1288
    	String list = "";
    	for(int i=0; i<this.favoritesChannels.length();i++){
    		try {
				JSONObject chan = (JSONObject) this.favoritesChannels.get(i);
				list+=chan.getString("ID")+",";
			} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
    	list = list.substring(0, list.length() - 1);
    	Log.d(TAG,this.getURLSetfavorites()+"&list="+list);
    	JSONArray json = JSONUtils.getJSONArrfromURL(this.getURLSetfavorites()+"&list="+list);
    	json = JSONUtils.getJSONArrfromURL(this.getURLfavorites());
    	Log.i(TAG,json.toString());
    	this.favoritesChannels = json;
    	return true;
    }
    /*
     * UTILS
     */
    public boolean isNumeric(String str){
    	try{
    		Double.parseDouble(str);
    	}catch(NumberFormatException nfe){
    		return false;
    	}
    	return true;
    }
}
