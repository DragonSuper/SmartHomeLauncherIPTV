package atn.ontheblue.iptv.launcher;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings.Secure;
import android.util.Log;

public class autoUpdate {
	
	final String TAG = "IPTV autoUpdate";
	
	private Context ctx=null;
	private static String packageName;
	private static String appName;
	private static String device_id;
	private static int versionCode;
	private static String versionName;
	
	private String updateURL = "http://www.smart-sat.se/update/"; 
	
	autoUpdate(Context ctx){
		this.ctx=ctx;
		autoUpdate.packageName = this.ctx.getPackageName();
		autoUpdate.device_id = Secure.getString( this.ctx.getContentResolver(),Secure.ANDROID_ID);
		ApplicationInfo appinfo = this.ctx.getApplicationInfo();		
		if( appinfo.labelRes != 0 ) {
			autoUpdate.appName = this.ctx.getString(appinfo.labelRes);
		} else {
			Log.w(TAG, "unable to find application label");
		}
		PackageManager pm = this.ctx.getPackageManager();
		PackageInfo packageInfo = null;		
		try {
			packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
			autoUpdate.versionCode = packageInfo.versionCode;
			autoUpdate.setVersionName(packageInfo.versionName);
		} catch (NameNotFoundException e) {			
			Log.e(TAG, e.getMessage());
		}
		Log.i(TAG,"PackageName : "+autoUpdate.packageName);
		Log.i(TAG,"AppName : "+autoUpdate.appName);
		Log.i(TAG,"device_id : "+autoUpdate.device_id);
		Log.i(TAG,"versionCode : "+autoUpdate.versionCode);		
		Runnable r = new Runnable() {
			public void run() {
				checkUpdate();
			}			
		};
		new Thread(r).start();
	}
	private boolean checkUpdate(){
    	Log.i(TAG, "check Update started...");
    	JSONObject json = JSONUtils.getJSONfromURL(this.updateURL);    	
    	try {
			String packagename = json.getString("packagename");
			String version = json.getString("version");			
			String versioncode = json.getString("versioncode");
			String min_sdk_version = json.getString("min_sdk_version");
			Log.i(TAG,"results "+version+" "+packagename+" "+versioncode+" "+min_sdk_version);
			Log.i(TAG, autoUpdate.versionCode+" "+Integer.parseInt(versioncode));
			if(autoUpdate.versionCode < Integer.parseInt(versioncode)){
				Log.d(TAG,"we need to update");
			}
			return true;
		} catch (JSONException e) {
			Log.e(TAG,e.toString());
			e.printStackTrace();
		}
    	Log.i(TAG,json.toString());
    	return false;
    }
	public static String getVersionName() {
		return versionName;
	}
	public static void setVersionName(String versionName) {
		autoUpdate.versionName = versionName;
	}
}
