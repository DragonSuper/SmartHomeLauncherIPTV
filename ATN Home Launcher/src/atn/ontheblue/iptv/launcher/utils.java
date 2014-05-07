package atn.ontheblue.iptv.launcher;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class utils {
	private Context c;
	final String TAG = "IPTV utils Class";
	
	public utils(HomeActivity c){
		this.c = c;
	}
	
	public boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    Log.d(TAG,"isAvailable : "+activeNetworkInfo.isAvailable());
	    return activeNetworkInfo != null;
	}
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
