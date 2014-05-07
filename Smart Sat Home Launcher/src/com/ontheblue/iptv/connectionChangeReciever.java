package com.ontheblue.iptv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class connectionChangeReciever extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
	    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();	    
	    while( activeNetInfo != null ){
	    	try {
	    		Log.d("connection BlahBlah","trying to connect... wait for 1000 ms");
	    		Thread.sleep(1000);
	    	} catch (InterruptedException e) {
	    		e.printStackTrace();
	    	}
	        
	      //Toast.makeText( context, "Active Network Type : " + activeNetInfo.getTypeName(), Toast.LENGTH_SHORT ).show();
	    	//Toast.makeText( context, "Active Network Type : " + activeNetInfo.getTypeName(), Toast.LENGTH_SHORT ).show();
	    }
	}

}
