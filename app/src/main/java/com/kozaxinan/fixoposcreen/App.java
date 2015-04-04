package com.kozaxinan.fixoposcreen;

import android.app.Application;

import com.flurry.android.FlurryAgent;

/**
 * Created by Sinan on 28.1.2015.
 */
public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		FlurryAgent.init(this, "XVBD73BM7MHQXGD47WFG");
	}
}
