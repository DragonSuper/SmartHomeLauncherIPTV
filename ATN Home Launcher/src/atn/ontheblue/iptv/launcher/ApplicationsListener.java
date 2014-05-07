package atn.ontheblue.iptv.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ApplicationsListener  extends BroadcastReceiver{
	//final private String TAG = "ATN IPTV HL -- Apps BroadcastReceiver";
	private HomeActivity homeActivity;
	
	public ApplicationsListener(HomeActivity homeActivity) {
		this.homeActivity = homeActivity;
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		this.homeActivity.apps.setAdapter(new appInfoAdapter(this.homeActivity));
	}

}
