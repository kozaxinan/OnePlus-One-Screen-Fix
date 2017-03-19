package com.kozaxinan.fixoposcreen.iab;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.kozaxinan.fixoposcreen.iab.utils.IabHelper;
import com.kozaxinan.fixoposcreen.iab.utils.IabResult;
import com.kozaxinan.fixoposcreen.iab.utils.Inventory;
import com.kozaxinan.fixoposcreen.iab.utils.Purchase;
import com.splunk.mint.Mint;

import java.util.HashSet;

/**
 * Created by Sinan on 24.1.2015.
 */
public class InAppUtils {

    public static final int RC_REQUEST = 10001;

    private IabHelper mHelper;

    private static InAppUtils INSTANCE;

    private static final Object mutex = new Object();

    private final HashSet<String> mInventorySet = new HashSet<String>();

    public static final String sku = "pro_2";

    InventoryQueryFinished listener;

    Context context;

    private InAppUtils() {
    }

    private InAppUtils(Context context, InventoryQueryFinished listener) {

        this.context = context;
        this.listener = listener;

        initBilling();
    }

    public void initBilling() {
        disposeBilling();

        String base64EncodedPublicKey = Build.GOOGLE_PLAY_PUBLIC_KEY;
        mHelper = new IabHelper(context, base64EncodedPublicKey);
        mHelper.enableDebugLogging(Build.DEBUG);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (mHelper == null) {
                    return;
                }
                if (!result.isSuccess()) {
                    return;
                }

                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (Exception e) {
                    mHelper.dispose();
                    e.printStackTrace();
                    Mint.logException(e);
                }
            }
        });
    }

    public static InAppUtils getInstance() {
        return INSTANCE;
    }

    public static InAppUtils getInstance(Context context, InventoryQueryFinished listener) {

        if (INSTANCE == null) {
            synchronized (mutex) {
                if (INSTANCE == null) {
                    INSTANCE = new InAppUtils(context, listener);
                }
            }
        }

        return INSTANCE;
    }

    /**
     * Releases billing service.
     */
    public void disposeBilling() {
        try {
            if (mHelper != null) {
                mHelper.dispose();
                mHelper = null;
            }
        } catch (Exception e) {
            Mint.logException(e);
            mHelper = null;
        }
    }

    public void buy(Activity activity) {
        if (mHelper != null) {
            if (!mInventorySet.contains(sku)) {
                /**
                 * See {@link com.kozaxinan.fixoposcreen.iab.DonationFragment#verifyDeveloperPayload(Purchase)}.
                 */
                String payload = "";
                try {
                    mHelper.launchPurchaseFlow(activity, sku, RC_REQUEST, mPurchaseFinishedListener, payload);
                } catch (Exception e) {
                    e.printStackTrace();
                    complain("Failed to launch a purchase flow.");
                    initBilling();
                    if (listener != null) {
                        listener.onQueryFinished(mInventorySet);
                    }
                }
            } else {
                Toast.makeText(activity, "You've bought this item already.", Toast.LENGTH_SHORT).show();
            }
        } else {
            complain("Please try later.");
            initBilling();
        }
    }

    public IabHelper getHelper() {
        return mHelper;
    }

    private final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (mHelper == null) {
                return;
            }
            if (result.isFailure()) {
                complain("Error purchasing: " + result.getMessage());
                return;
            }

            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                return;
            }

            String sku = purchase.getSku();
            mInventorySet.add(sku);

            if (listener != null) {
                listener.onQueryFinished(mInventorySet);
            }

            Mint.logEvent("pro_user");

            initBilling();
        }
    };

    private final IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) {
                return;
            }
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result.getMessage());
                return;
            }

            mInventorySet.clear();

            Purchase purchase = inventory.getPurchase(sku);
            boolean isBought = (purchase != null && verifyDeveloperPayload(purchase));

            if (isBought) {
                mInventorySet.add(sku);
            }

            if (listener != null) {
                listener.onQueryFinished(mInventorySet);
            }
        }
    };

    private boolean verifyDeveloperPayload(Purchase purchase) {
        // TODO: This method itself is a big question.
        // Personally, I think that this whole ‘best practices’ part
        // is confusing and is trying to make you do work that the API
        // should really be doing. Since the purchase is tied to a Google account,
        // and the Play Store obviously saves this information, they should
        // just give you this in the purchase details. Getting a proper user ID
        // requires additional permissions that you shouldn’t need to add just
        // to cover for the deficiencies of the IAB API.
        return true;
    }

    private void complain(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static interface InventoryQueryFinished {

        public void onQueryFinished(HashSet<String> mInventorySet);
    }
}
