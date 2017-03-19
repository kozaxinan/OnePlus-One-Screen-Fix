package com.kozaxinan.fixoposcreen;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.android.internal.telephony.ITelephony;
import com.splunk.mint.Mint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Sinan on 20.10.2014.
 *
 * This service put a view on status bar. This view catches the ghost touch.
 */
public class FixServices extends Service implements SensorEventListener {

	/** The broadcast receiver */
	private BroadcastReceiver receiver;

	/** The sensor manager */
	private SensorManager mSensorManager;

	/** The dummy view for to put on top of status bar. It will consume ghost touchs */
	private View statusBarView;

	private WindowManager.LayoutParams params;

	private WindowManager wm;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d("FixServices", "onStartCommand");

		if (!BuildConfig.DEBUG) {
            Mint.initAndStartSession(this, "0096c713");
		}

		if (receiver == null) {
			IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			receiver = new ScreenReceiver();
			getApplicationContext().registerReceiver(receiver, filter);
		}

		if (mSensorManager == null) {
			mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			Sensor mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
		}

		int statusBarHeight = (int) Math.ceil(25 * getResources().getDisplayMetrics().density);

		if (statusBarView == null) {
			statusBarView = new View(getApplicationContext());

			// Handle the problem
			// At first there was a ghost touch on left side of status bar.
			// We blocked to opening navigation panel problem
			// But when there is a ghost touch, the first touch to screen act like it was a slide from top of the screen.
			// When this happened check where this slide action goes.
			// And help the user and disconnect the call
			statusBarView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					Log.d("onTouch", "onTouch");

					int action = event.getAction();

					switch (action) {
						case MotionEvent.ACTION_OUTSIDE:
							Log.d("onTouch", "ACTION_OUTSIDE");
							hide();
						case MotionEvent.ACTION_DOWN:
//						collapse();
							// finger touches the screen
							break;

						case MotionEvent.ACTION_MOVE:
							// finger moves on the screen
							Log.d("onTouch", "ACTION_MOVE x: " + event.getX() + " ACTION_MOVE y: " + event.getY());

							// The red hangup button's height
							// If the touch slides over the hangup button, assume that user try to hang up and disconnect the call.
							if (event.getY() > 1400 && event.getY() < 1520) {

								// Hang up call
								disconnectCallAndroid();
							}
							break;

						case MotionEvent.ACTION_UP:
							// finger leaves the screen
							hide();
							break;
					}
					return true;
				}
			});
		}

		// For debug
		if (BuildConfig.DEBUG) {
			statusBarView.setBackgroundColor(0x5500FF00);
		}

		wm = (WindowManager) getSystemService(WINDOW_SERVICE);

		if (AppSettings.getInstance().isImmersiveServiceEnable(getApplicationContext())) {

			// Put a view above status bar
			params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
													statusBarHeight * 8,
													WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
													WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
															| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
															| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
															| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
													PixelFormat.TRANSLUCENT);
		} else {
			// Put a view above status bar
			params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
													statusBarHeight * 3,
													WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
													WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
															| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
															| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
															| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
															| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
													PixelFormat.TRANSLUCENT);
		}

		params.gravity = Gravity.LEFT | Gravity.TOP;

		return START_STICKY;
	}

	/**
	 * This method try to hang up with a hacky method.
	 * {@link com.android.internal.telephony.ITelephony} is an internal interface of android.
	 * We need to put same interface in our app to use instead
	 */
	private void disconnectCallAndroid() {
		try {
			TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
			Class<?> clazz = Class.forName(telephonyManager.getClass().getName());
			Method method = clazz.getDeclaredMethod("getITelephony");

			method.setAccessible(true);
			ITelephony telephonyService = (ITelephony) method.invoke(telephonyManager);
			telephonyService.endCall();
		} catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDestroy() {
		Log.d("FixServices", "onDestroy");

		// Unregister broadcast receiver
		getApplicationContext().unregisterReceiver(receiver);

		// Unregister sensor receiver
		mSensorManager.unregisterListener(this);

		// Remove dummy view which is on top of status bar
		hide();

		try {
			wm.removeView(statusBarView);
		} catch (Exception e) {
//			e.printStackTrace();
		}

		// Increase the fix number
		int useCount = AppSettings.getInstance().increaseUseCount(getApplicationContext());

		if (useCount % 25 == 0 && !AppSettings.getInstance().isPro(this)) {
			Utils.showNotification(getApplicationContext(), "Just prevented bug", useCount + " times notification panel opening bug fixed");
		}
		if (!BuildConfig.DEBUG) {
            Mint.closeSession(this);
		}

		// Collapse navigation panel
		collapse();

		super.onDestroy();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// not supporting binding
		return null;
	}

	/**
	 * This method close the navigation panel
	 */
	private void collapse() {
		Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		getApplicationContext().sendBroadcast(it);
	}

	private void show() {
		try {
			wm.addView(statusBarView, params);
		} catch (Exception e) {
//			e.printStackTrace();
		}
		statusBarView.setVisibility(View.VISIBLE);
		Utils.pro(getApplicationContext(), statusBarView);
	}

	private void hide() {
		statusBarView.setVisibility(View.GONE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		float distance = event.values[0];
		Log.d("onSensorChanged", "distance " + distance);

		// If sensor value changed from 0, try to collapse navigation panel
		if (distance != 0) {
			show();
		} else {
			hide();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.d("onAccuracyChanged", "accuracy " + accuracy);
	}

	/**
	 * During a phone call, try to detect action events.
	 * But on a call, phone doesnt close screen. It make it black and unresponsive.
	 * It is just an prevention
	 */
	public class ScreenReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("ScreenReceiver", "onReceive");

			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Log.d("ScreenReceiver", "ACTION_SCREEN_OFF");
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				Log.d("ScreenReceiver", "ACTION_SCREEN_ON");

				// try to collapse navigation panel
				collapse();
			}
		}
	}
}
