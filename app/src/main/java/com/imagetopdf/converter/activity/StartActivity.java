package com.imagetopdf.converter.activity;

import static com.imagetopdf.converter.Utils.Helper.showAdsNumberCount;
import static com.imagetopdf.converter.activity.ImageToPDF.READ_REQUEST_CODE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.appizona.yehiahd.fastsave.FastSave;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.imagetopdf.converter.BuildConfig;
import com.imagetopdf.converter.R;
import com.imagetopdf.converter.Utils.Helper;
import com.imagetopdf.converter.photopicker.activity.PickImageActivity;

public class StartActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.main_bg1));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            CheckStoragePermission();
        }

        findViewById(R.id.ivSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, SettingsActivity.class));
                ShowFullAds(StartActivity.this);
            }
        });

        findViewById(R.id.ivAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PickImageActivity.class);
                intent.putExtra(PickImageActivity.KEY_LIMIT_MAX_IMAGE, 9);
                intent.putExtra(PickImageActivity.KEY_LIMIT_MIN_IMAGE, 1);
                startActivityForResult(intent, READ_REQUEST_CODE);
                ShowFullAds(StartActivity.this);
            }
        });

        findViewById(R.id.myDocs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, MainActivity.class));
                ShowFullAds(StartActivity.this);
            }
        });

        findViewById(R.id.ivShare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Download Now https://play.google.com/store/apps/details?id=" + getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
    }

    private void CheckStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Storage Permission");
                alertDialog.setMessage("Storage permission is required in order to " +
                        "provide Image to PDF feature, please enable permission in app settings");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                                startActivity(i);
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2);
            }
        }
    }

    @Override
    public void onBackPressed() {
        showBottomSheetDialog();
    }

    private BottomSheetDialog mBottomSheetDialog;

    private void showBottomSheetDialog() {
        final View view = getLayoutInflater().inflate(R.layout.dialog_exit_sheet, null);
        view.findViewById(R.id.tvClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                finishAffinity();
            }
        });

        view.findViewById(R.id.tv_maybe_later).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
            }
        });

        mBottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme);
        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBottomSheetDialog = null;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!FastSave.getInstance().getBoolean(Helper.REMOVE_ADS_KEY, false)) {
            LoadFullAd(this);
            refreshAd();
        }
    }

    InterstitialAd ad_mob_interstitial;
    AdRequest interstitial_adRequest;
    public void LoadFullAd(Context context) {
        Log.e("TAG", "LoadFullAd: ");
        try {
            Bundle non_personalize_bundle = new Bundle();
            non_personalize_bundle.putString("npa", "1");

            interstitial_adRequest = new AdRequest.Builder().build();

            InterstitialAd.load(context, FastSave.getInstance().getString("INTER",""), interstitial_adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    ad_mob_interstitial = interstitialAd;
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    ad_mob_interstitial = null;
                    Log.e("TAG", "AdError1: " +loadAdError.getMessage());
                }
            });
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public void ShowFullAds(final Context context) {
        if (ad_mob_interstitial != null) {
            ad_mob_interstitial.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    Helper.is_show_open_ad = true;
                    LoadFullAd(context);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    Log.e("TAG", "AdError: " +adError.getMessage());
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    ad_mob_interstitial = null;
                }
            });
        }
        int i = showAdsNumberCount + 1;
        showAdsNumberCount = i;
        if (FastSave.getInstance().getInt("CLICKS", -1) < i) {
            if (ad_mob_interstitial != null) {
                ad_mob_interstitial.show(this);
            }
            Helper.is_show_open_ad = false;
            showAdsNumberCount = 0;
        }
    }

    LinearLayout frameLayout;

    private void refreshAd() {
        AdLoader.Builder builder = new AdLoader.Builder(this, FastSave.getInstance().getString("NATIVE", ""))
                .forNativeAd(nativeAd -> {
                    frameLayout = findViewById(R.id.linear_ads_bottom);
                    NativeAdView adView = (NativeAdView) getLayoutInflater()
                            .inflate(R.layout.ad_unit, null);
                    populateUnifiedNativeAdView(nativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                });

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void populateUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);

        TextView headlineView = adView.findViewById(R.id.ad_headline);
        TextView ad_call_to_action = adView.findViewById(R.id.ad_call_to_action);
        TextView ad_body = adView.findViewById(R.id.ad_body);
        adView.setBodyView(ad_body);
        ImageView ad_app_icon = adView.findViewById(R.id.ad_app_icon);
        headlineView.setText(nativeAd.getHeadline());

        if (nativeAd.getBody() == null) {
            ad_body.setVisibility(View.INVISIBLE);
        } else {
            ad_body.setVisibility(View.VISIBLE);
            ad_body.setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            ad_call_to_action.setVisibility(View.INVISIBLE);
        } else {
            ad_call_to_action.setVisibility(View.VISIBLE);
            ad_call_to_action.setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            ad_app_icon.setVisibility(View.GONE);
        } else {
            ad_app_icon.setImageDrawable(nativeAd.getIcon().getDrawable());
            ad_app_icon.setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }

}