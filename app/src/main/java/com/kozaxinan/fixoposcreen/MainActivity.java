package com.kozaxinan.fixoposcreen;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.kozaxinan.feedback.Feedback;
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

		count.setText(AppSettings.getInstance().getUseCount(this) + " times fixed.");
		service.setChecked(AppSettings.getInstance().isServiceEnable(getApplicationContext()));
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

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}