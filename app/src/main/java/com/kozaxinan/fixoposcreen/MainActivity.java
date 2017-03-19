package com.kozaxinan.fixoposcreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.kozaxinan.fixoposcreen.iab.DialogHelper;
import com.kozaxinan.fixoposcreen.iab.InAppUtils;
import com.kozaxinan.fixoposcreen.iab.utils.IabHelper;
import com.splunk.mint.Mint;

import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

	private SwitchCompat switchCompat;

	private CheckBox service;

	private IabHelper mHelper;

	private Toolbar toolbar;

    private Button showAds;

    InterstitialAd mInterstitialAd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "MyApp");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Opened");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "blabla");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("F61B4D68BFC0F8FC79BA0B1074C92EC7").build();
        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.inter_ad_unit_id));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();

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
						}
					}
				}
			}
		});

        showAds = (Button) findViewById(R.id.showAdsButton);
        if (!AppSettings.getInstance().isTry(this) && !AppSettings.getInstance().isPro(this)) {
            showAds.setVisibility(View.VISIBLE);
            showAds.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                        AppSettings.getInstance().resetTry(MainActivity.this);
                    } else {
                        Toast.makeText(MainActivity.this, "Please try later. :)", Toast.LENGTH_SHORT)
                             .show();
                    }
                    requestNewInterstitial();
                }
            });
        }

		service.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				AppSettings.getInstance().enableService(getApplicationContext(), isChecked);
			}
		});

		if (!BuildConfig.DEBUG) {
			Mint.logEvent("app_opened");
		}
	}

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("F61B4D68BFC0F8FC79BA0B1074C92EC7")
                .build();

        mInterstitialAd.loadAd(adRequest);
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

                try {
                    FragmentManager fm = getSupportFragmentManager();
                    Fragment prev = fm.findFragmentByTag(DialogHelper.TAG_FRAGMENT_DONATION);
                    if (prev == null) {
                        DialogHelper.showDonateDialog(MainActivity.this);
                        if (!BuildConfig.DEBUG) {
                            Mint.logEvent("donate_opened");
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    Mint.logException(e);
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
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (!BuildConfig.DEBUG) {
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
				}

				browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=pub%3AKozaxinan"));
				break;

			case R.id.xda:
				if (!BuildConfig.DEBUG) {
					Mint.logEvent("xdaforum");
				}

				browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/oneplus-one/general/app-notification-panel-problem-phone-t2999061/"));
				break;

			case R.id.oneplus:
				if (!BuildConfig.DEBUG) {
					Mint.logEvent("oneplusforum");
				}

				browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://forums.oneplus.net/threads/fix-app-notification-panel-opening-problem-during-phone-call.186588/"));
				break;

            case R.id.ads:
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                    AppSettings.getInstance().resetTry(MainActivity.this);
                } else {
                    Toast.makeText(MainActivity.this, "Please try later. :)", Toast.LENGTH_SHORT)
                         .show();
                }
                requestNewInterstitial();
                break;

            case R.id.policy:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://cdn.rawgit.com/kozaxinan/127bc6197cf74ea8e363fe6633253550/raw/0100a0ce5fa8a4fa4bab4ba434338b44881a355f/policy.html"));
                startActivity(browserIntent);

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

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Try Meteor Crush");
        builder.setView(R.layout.download_game);
        builder.setNegativeButton("Disable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setPositiveButton("Go Store", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                                  Uri.parse("https://play.google.com/store/apps/details?id=com.kozaxinan.meteorcrush"));
                startActivity(browserIntent);
                finish();

            }
        });

        builder.create().show();
    }
}
