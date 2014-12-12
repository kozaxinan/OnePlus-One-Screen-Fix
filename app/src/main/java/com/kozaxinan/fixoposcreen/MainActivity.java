package com.kozaxinan.fixoposcreen;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.kozaxinan.feedback.Feedback;
import com.kozaxinan.fixoposcreen.iab.DialogHelper;
import com.splunk.mint.Mint;


public class MainActivity extends Activity {

	private CheckBox service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		service = (CheckBox) findViewById(R.id.checkBox);
		service.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				AppSettings.getInstance().enableService(getApplicationContext(), isChecked);
			}
		});

		Feedback.with(this).sendTo("kozaxinan@gmail.com");
	}

	@Override
	protected void onResume() {
		super.onResume();

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
			}
		});

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
			FlurryAgent.onStartSession(this, "XVBD73BM7MHQXGD47WFG");
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
		Feedback.with(this).addToMenu(getMenuInflater(), menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		Feedback.with(this).checkMenu(item, this);

		return super.onOptionsItemSelected(item);
	}
}
