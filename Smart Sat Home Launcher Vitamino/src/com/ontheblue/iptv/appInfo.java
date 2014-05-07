package com.ontheblue.iptv;

import android.graphics.Bitmap;

public class appInfo {
	private Bitmap mIcon;
	private String mName;

	public appInfo(Bitmap icon, String name) {
		mIcon = icon;
		mName = name;
	}

	public void setIcon(Bitmap icon) {
		mIcon = icon;
	}
	public Bitmap getIcon() {
		return mIcon;
	}

	public void setName(String name) {
		mName = name;
	}
	public String getName() {
		return mName;
	}
}
