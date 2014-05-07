package com.ontheblue.iptv;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class utils {
	private Context c;
	final String TAG = "IPTV utils Class";
	
	public utils(TvHome c){
		this.c = c;
	}
	
	public boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    Log.d(TAG,"isAvailable : "+activeNetworkInfo.isAvailable());
	    return activeNetworkInfo != null;
	}
}
