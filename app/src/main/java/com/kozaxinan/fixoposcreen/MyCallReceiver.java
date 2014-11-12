package com.kozaxinan.fixoposcreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Sinan on 20.10.2014.
 */
public class MyCallReceiver extends BroadcastReceiver {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
			// This code will execute when the phone has an incoming call

			// get the phone number
			String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
			showToast(context, "Call from:" + incomingNumber);

		} else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
			Log.d("MyCallReceiver", "Detected call event");
			showToast(context, "Detected call event");

			// Start the service if it is enabled
			if (AppSettings.getInstance().isServiceEnable(context)) {
				context.startService(new Intent(context, FixServices.class));
			}

		} else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
			// This code will execute when the call is disconnected
			Log.d("MyCallReceiver", "Detected call hangup event");
			showToast(context, "Detected call hangup event");

			// Stop the service if it is enabled
			if (AppSettings.getInstance().isServiceEnable(context)) {
				context.stopService(new Intent(context, FixServices.class));
			}
		}
	}

	/**
	 * For debug purpose.
	 *
	 * @param context
	 * @param text
	 */
	private void showToast(Context context, String text) {
		if (BuildConfig.DEBUG) {
			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
		}
	}
}
