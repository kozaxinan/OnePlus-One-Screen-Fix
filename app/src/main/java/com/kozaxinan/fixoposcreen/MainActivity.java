package com.kozaxinan.fixoposcreen;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.kozaxinan.feedback.Feedback;
import com.kozaxinan.fixoposcreen.iab.DialogHelper;
import com.kozaxinan.fixoposcreen.iab.InAppUtils;
import com.kozaxinan.fixoposcreen.iab.utils.IabHelper;
import com.splunk.mint.Mint;

import java.util.HashSet;

public class MainActivity extends ActionBarActivity {

	private SwitchCompat switchCompat;

	private CheckBox service;

	private IabHelper mHelper;

	private Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
		setSupportActionBar(toolbar);

		service = (CheckBox) findViewById(R.id.checkBox);

		switchCompat = (SwitchCompat) findViewById(R.id.switchCompat);
		switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				if (AppSettings.getInstance().isPro(MainActivity.this) || AppSettings.getInstance().isTry(MainActivity.this)) {
					AppSettings.getInstance().setImmersiveServiceEnable(getApplicationContext(), isChecked);
				} else {
					if (isChecked) {
						AppSettings.getInstance().setImmersiveServiceEnable(getApplicationContext(), false);
						switchCompat.setChecked(false);
						InAppUtils.getInstance().buy(MainActivity.this);

						if (!BuildConfig.DEBUG) {
							Mint.logEvent("buy_try");
							FlurryAgent.logEvent("buy_try");
						}
					}
				}
			}
		});

		service.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				AppSettings.getInstance().enableService(getApplicationContext(), isChecked);
			}
		});

		Feedback.with(this).sendTo("kozaxinan@gmail.com");

		if (!BuildConfig.DEBUG) {
			Mint.logEvent("app_opened");
			FlurryAgent.logEvent("app_opened");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mHelper = InAppUtils.getInstance(this, inventoryListener).getHelper();

		TextView count = (TextView) findViewById(R.id.textView);

		// Update text count every time
		int useCount = AppSettings.getInstance().getUseCount(this);

		if (useCount > 1) {
			count.setText(useCount + " times fixed.");
		} else if (useCount == 1) {
			count.setText(useCount + " time fixed.");
		}

		findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogHelper.showDonateDialog(MainActivity.this);

				if (!BuildConfig.DEBUG) {
					Mint.logEvent("donate_opened");
					FlurryAgent.logEvent("donate_opened");
				}
			}
		});

		switchCompat.setChecked(AppSettings.getInstance().isImmersiveServiceEnable(getApplicationContext()));
		service.setChecked(AppSettings.getInstance().isServiceEnable(getApplicationContext()));

	}

	public void onLinkClicked(View view) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kozaxinan/OnePlus-One-Screen-Fix"));
		startActivity(browserIntent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!BuildConfig.DEBUG) {
			Mint.initAndStartSession(this, "0096c713");
			FlurryAgent.onStartSession(this);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (!BuildConfig.DEBUG) {
			FlurryAgent.onEndSession(this);
			Mint.closeSession(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
//		Feedback.with(this).addToMenu(getMenuInflater(), menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		Intent browserIntent = null;
		switch (id) {
			case R.id.my_apps:
				if (!BuildConfig.DEBUG) {
					Mint.logEvent("clicked_my_apps");
					FlurryAgent.logEvent("clicked_my_apps");
				}

				browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=pub%3AKozaxinan"));
				break;

			case R.id.xda:
				if (!BuildConfig.DEBUG) {
					Mint.logEvent("xdaforum");
					FlurryAgent.logEvent("xdaforum");
				}

				browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/oneplus-one/general/app-notification-panel-problem-phone-t2999061/"));
				break;

			case R.id.oneplus:
				if (!BuildConfig.DEBUG) {
					Mint.logEvent("oneplusforum");
					FlurryAgent.logEvent("oneplusforum");
				}

				browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://forums.oneplus.net/threads/fix-app-notification-panel-opening-problem-during-phone-call.186588/"));
				break;
		}

		if (browserIntent != null) {
			startActivity(browserIntent);
		}

//		Feedback.with(this).checkMenu(item, this);

		return super.onOptionsItemSelected(item);
	}

	private InAppUtils.InventoryQueryFinished inventoryListener = new InAppUtils.InventoryQueryFinished() {
		@Override
		public void onQueryFinished(HashSet<String> mInventorySet) {
			AppSettings.getInstance().setPro(getApplicationContext(), mInventorySet.contains(InAppUtils.sku));
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mHelper == null) {
			return;
		}

		// Pass on the activity result to the helper for handling
		if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
			// not handled, so handle it ourselves (here's where you'd
			// perform any handling of activity results not related to in-app
			// billing...
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
}
