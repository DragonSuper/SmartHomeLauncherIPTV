package atn.ontheblue.iptv.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class ConnectivityListener extends BroadcastReceiver{
	final private String TAG = "ATN IPTV HL -- Conn BroadcastReceiver";
	HomeActivity homeActivity;
	
	public ConnectivityListener(){}
	public ConnectivityListener(HomeActivity homeActivity) {
		this.homeActivity = homeActivity;		
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    if ( activeNetworkInfo != null ){	    	
	    	Log.i(TAG,"InternetConnection available.");	    	
	    	//Toast.makeText( context, "Active Network Type : " + activeNetworkInfo.getTypeName(), Toast.LENGTH_SHORT ).show();
	    	if(this.homeActivity != null){
	    		this.homeActivity.updateProgramList();
		    	this.homeActivity.updateFavoriteList();
		    	this.homeActivity.mIsAvailableTostream = true;
		    	//SurfaceView mPreview = (SurfaceView) context.findViewById(R.id.surface);
		    	homeActivity.mPreview.setBackgroundDrawable(null);
		    	homeActivity.initAutoUpdate();
		    	homeActivity.closeMenu();
	    	}else{	    		
	    		try{
	    			homeActivity = (HomeActivity) context;
	    			this.homeActivity.updateProgramList();
			    	this.homeActivity.updateFavoriteList();
			    	this.homeActivity.mIsAvailableTostream = true;
			    	homeActivity.mPreview.setBackgroundDrawable(null);
			    	homeActivity.initAutoUpdate();
			    	homeActivity.closeMenu();
	    		}catch(Exception err){}
	    	}
	    	
	    }else{
	    	Toast.makeText( context, R.string.txt_no_network, Toast.LENGTH_SHORT ).show();
	    }
	}

}
