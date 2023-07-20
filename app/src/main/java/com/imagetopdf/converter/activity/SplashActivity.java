package com.imagetopdf.converter.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.appizona.yehiahd.fastsave.FastSave;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.imagetopdf.converter.BuildConfig;
import com.imagetopdf.converter.R;
import com.imagetopdf.converter.Utils.Helper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;


public class SplashActivity extends AppCompatActivity implements PurchasesUpdatedListener {
    public static final String[] PERMISSIONS_S = {"android.permission.READ_MEDIA_IMAGES"};
    public static final String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private BillingClient mBillingClient;
    boolean in_app_check = false;
    boolean Ad_Show = false;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.activity_splash);
        Helper.is_show_open_ad = false;

        setView();


    }

    private void setView() {
        BillingClient build = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
        this.mBillingClient = build;
        build.startConnection(new BillingClientStateListener() { // from class: mobi.bluetooth.shortcut.vs.SplashActivity.2
            @Override
            public void onBillingServiceDisconnected() {
            }

            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                try {
                    SplashActivity.this.mBillingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType("inapp").build(), new PurchasesResponseListener() { // from class: mobi.bluetooth.shortcut.vs.SplashActivity.2.1
                        @Override
                        public void onQueryPurchasesResponse(BillingResult billingResult2, List<Purchase> list) {
                            if (list.toString().contains(Helper.REMOVE_ADS_PRODUCT_ID)) {
                                SplashActivity.this.in_app_check = true;
                            } else {
                                SplashActivity.this.in_app_check = false;
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        queryPurchases();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            TedPermission.create().setPermissions(PERMISSIONS_S).setDeniedMessage("If you reject this permission,you can not use this app because it is highly needed for search all images from your device.\n\nPlease turn on permissions at [Setting] > [Permission]").setPermissionListener(new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    if (SplashActivity.this.in_app_check) {
                        FastSave.getInstance().saveBoolean(Helper.REMOVE_ADS_KEY, true);
                        SplashActivity.this.ContinueWithoutAdsProcess();
                    } else if (FastSave.getInstance().getBoolean(Helper.REMOVE_ADS_KEY, false)) {
                        SplashActivity.this.ContinueWithoutAdsProcess();
                    } else {
                        if (isNetworkAvailable()) {
                            getdata();
                        } else {
                            ContinueWithoutAdsProcess();
                        }
                    }
                }

                @Override
                public void onPermissionDenied(List<String> list) {
                    finish();
                }
            }).check();
        } else {
            TedPermission.create().setPermissions(PERMISSIONS).setDeniedMessage("If you reject this permission,you can not use this app because it is highly needed for search all images from your device.\n\nPlease turn on permissions at [Setting] > [Permission]").setPermissionListener(new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    if (SplashActivity.this.in_app_check) {
                        FastSave.getInstance().saveBoolean(Helper.REMOVE_ADS_KEY, true);
                        SplashActivity.this.ContinueWithoutAdsProcess();
                    } else if (FastSave.getInstance().getBoolean(Helper.REMOVE_ADS_KEY, false)) {
                        SplashActivity.this.ContinueWithoutAdsProcess();
                    } else {
                        if (isNetworkAvailable()) {
                            getdata();
                        } else {
                            ContinueWithoutAdsProcess();
                        }
                    }
                }

                @Override
                public void onPermissionDenied(List<String> list) {
                    finish();
                }
            }).check();
        }
    }

    private static final int TIMEOUT = 5000;
    private boolean isRequestCompleted = false;

    private void getdata() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        String getdata_url = "http://167.71.226.31:8700/api/setting";
        params.put("package_name", getPackageName());
        client.post(getdata_url, params, new AsyncHttpResponseHandler() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e("TAG", statusCode + " > " + new String(responseBody));
                try {
                    JSONObject jsonObject2 = new JSONObject(new String(responseBody));
                    String app_open_interstitial = jsonObject2.getString("app_open_interstitial");
                    String interestrial_ads = jsonObject2.getString("interestrial_ads");
                    String native_ads = jsonObject2.getString("native_ads");
                    String banner_ads = jsonObject2.getString("banner_ads");
                    String reward_video_ads = jsonObject2.getString("reward_video_ads");
                    int version_code = Integer.parseInt(jsonObject2.getString("version_code"));
                    int clickCount = Integer.parseInt(jsonObject2.getString("update_url"));
                    String is_update = jsonObject2.getString("is_update");
                    String privacy_policy = jsonObject2.getString("privacy_policy");

                    FastSave.getInstance().saveString("PRIVACY_POLICY", privacy_policy);
                    FastSave.getInstance().saveString("APP_OPEN", app_open_interstitial);
                    FastSave.getInstance().saveString("INTER", interestrial_ads);
                    FastSave.getInstance().saveString("NATIVE", native_ads);
                    FastSave.getInstance().saveString("BANNER", banner_ads);
                    FastSave.getInstance().saveString("REWARD", reward_video_ads);
                    FastSave.getInstance().saveInt("CLICKS", clickCount);
//                    FastSave.getInstance().saveString("APP_OPEN", "ca-app-pub-3940256099942544/3419835294");
//                    FastSave.getInstance().saveString("INTER", "ca-app-pub-3940256099942544/1033173712");
//                    FastSave.getInstance().saveString("NATIVE", "ca-app-pub-3940256099942544/2247696110");
//                    FastSave.getInstance().saveString("BANNER", "ca-app-pub-3940256099942544/6300978111");
//                    FastSave.getInstance().saveString("REWARD", "ca-app-pub-3940256099942544/5224354917");
                    Helper.is_show_open_ad = true;
                    if (is_update.equals("1") && version_code > BuildConfig.VERSION_CODE) {
                        Helper.openUpdateDialog(SplashActivity.this);
                    } else {
                        loadAppOpenAd();
                    }
                    isRequestCompleted = true;
                } catch (Exception e) {
                    isRequestCompleted = true;
                    Log.e("TAG", "showAdIfAvailable: 111 " + e.getMessage());
                    ContinueWithoutAdsProcess();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                isRequestCompleted = true;
                Log.e("TAG1", error.getMessage());
                ContinueWithoutAdsProcess();
            }
        });

        new Handler().postDelayed(() -> {
            if (!isRequestCompleted) {
                ContinueAfterAdsProcess();
            }
        }, TIMEOUT);
    }

    private AppOpenAd appOpenAd;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;

    private void loadAppOpenAd() {
        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(
                this,
                FastSave.getInstance().getString("APP_OPEN", ""),
                request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        appOpenAd = ad;
                        showAppOpenAd();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        Log.e("TAG", "onAdFailedToShowFullScreenContent: " + error.getMessage());
                        // Handle ad loading failure.
                        ContinueAfterAdsProcess();
                    }
                });
    }

    private void showAppOpenAd() {
        if (appOpenAd != null) {
            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Ad dismissed, redirect to the main screen.
                            ContinueAfterAdsProcess();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            Log.e("TAG", "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                            ContinueAfterAdsProcess();
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            // Ad shown.
                        }
                    };

            appOpenAd.show(this);
            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
        } else {
            // Ad not ready, redirect to the main screen.
            ContinueAfterAdsProcess();
        }
    }

    private void queryPurchases() {
        this.mBillingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType("inapp").build(), new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(BillingResult billingResult, List<Purchase> list) {
                if (list == null || list.isEmpty()) {
                    return;
                }
                for (Purchase purchase : list) {
                    if (purchase.getProducts().contains(Helper.REMOVE_ADS_PRODUCT_ID)) {
                        FastSave.getInstance().saveBoolean(Helper.REMOVE_ADS_KEY, true);
                    }
                }
            }
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> list) {
        if (billingResult.getResponseCode() == 0 && list != null) {
            for (Purchase purchase : list) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == 1) {
            Log.d("str", "User Canceled" + billingResult.getResponseCode());
        } else if (billingResult.getResponseCode() == 7) {
            FastSave.getInstance().saveBoolean(Helper.REMOVE_ADS_KEY, true);
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == 1) {
            AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
                @Override
                public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                    Log.e("result", "" + billingResult.getResponseCode() + "::" + billingResult.getDebugMessage());
                    if (billingResult.getResponseCode() == 0) {
                        FastSave.getInstance().saveBoolean(Helper.REMOVE_ADS_KEY, true);
                        SplashActivity.this.ContinueWithoutAdsProcess();
                    }
                }
            };
            if (!purchase.isAcknowledged()) {
                this.mBillingClient.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build(), acknowledgePurchaseResponseListener);
            } else if (purchase.getProducts().contains(Helper.REMOVE_ADS_PRODUCT_ID)) {
                FastSave.getInstance().saveBoolean(Helper.REMOVE_ADS_KEY, true);
                ContinueWithoutAdsProcess();
            }
        }
    }

    private void ContinueAfterAdsProcess() {
        startActivity(new Intent(SplashActivity.this, StartActivity.class));
    }

    private void ContinueWithoutAdsProcess() {
        // java.lang.Runnable
        new Handler(Looper.getMainLooper()).postDelayed(SplashActivity.this::ContinueAfterAdsProcess, 5000L);
    }

    @Override
    public void onBackPressed() {
    }

    public boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
