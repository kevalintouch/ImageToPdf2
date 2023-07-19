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


public class SplashActivity extends AppCompatActivity {
    public static final String[] PERMISSIONS_S = {"android.permission.READ_MEDIA_IMAGES"};
    public static final String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.activity_splash);
        Helper.is_show_open_ad = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            TedPermission.create().setPermissions(PERMISSIONS_S).setDeniedMessage("If you reject this permission,you can not use this app because it is highly needed for search all images from your device.\n\nPlease turn on permissions at [Setting] > [Permission]").setPermissionListener(new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    if (isNetworkAvailable()) {
                        getdata();
                    } else {
                        ContinueWithoutAdsProcess();
                    }
                }

                @Override
                public void onPermissionDenied(List<String> list) {
                    finish();
                }
            }).check();
        }else{
            TedPermission.create().setPermissions(PERMISSIONS).setDeniedMessage("If you reject this permission,you can not use this app because it is highly needed for search all images from your device.\n\nPlease turn on permissions at [Setting] > [Permission]").setPermissionListener(new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    if (isNetworkAvailable()) {
                        getdata();
                    } else {
                        ContinueWithoutAdsProcess();
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
