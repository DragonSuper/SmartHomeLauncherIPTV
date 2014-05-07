package com.ontheblue.iptv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class appInfoAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<ApplicationInfo> mApplications;
	final PackageManager pm;

	public appInfoAdapter(Context context) {
		mContext = context;
		pm = context.getPackageManager();
		//mListAppInfo = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(pm));

        if (apps != null) {
            final int count = apps.size();

            if (mApplications == null) {
                mApplications = new ArrayList<ApplicationInfo>(count);
            }
            mApplications.clear();

            for (int i = 0; i < count; i++) {
                ApplicationInfo application = new ApplicationInfo();
                ResolveInfo info = apps.get(i);

                application.title = info.loadLabel(pm);
                application.setActivity(new ComponentName(
                        info.activityInfo.applicationInfo.packageName,
                        info.activityInfo.name),
                        Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                application.icon = info.activityInfo.loadIcon(pm);

                mApplications.add(application);
            }
        }
	}

	public int getCount() {
		return mApplications.size();
	}

	public Object getItem(int position) {
		return mApplications.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ApplicationInfo entry = mApplications.get(position);
		if(convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.app_info, null);
		}

		ImageView ivIcon = (ImageView)convertView.findViewById(R.id.ivIcon);		
		ivIcon.setImageDrawable(entry.icon);
		

		TextView tvName = (TextView)convertView.findViewById(R.id.tvName);
		tvName.setText(entry.title);

		return convertView;
	}
}
