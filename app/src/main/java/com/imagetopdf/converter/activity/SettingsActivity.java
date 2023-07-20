package com.imagetopdf.converter.activity;

import static com.imagetopdf.converter.Utils.Helper.showAdsNumberCount;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.appizona.yehiahd.fastsave.FastSave;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.imagetopdf.converter.R;
import com.imagetopdf.converter.Utils.Helper;
import com.imagetopdf.converter.ads.AdmobAdsHelper;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private BillingClient mBillingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView tvVersion = findViewById(R.id.tvVersion);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            tvVersion.setText("" + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        findViewById(R.id.llShare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hey check out this app at: https://play.google.com/store/apps/details?id=" + SettingsActivity.this.getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
        findViewById(R.id.llRemoveADs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeadsDialog();
            }
        });
        findViewById(R.id.llFeedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, FeedbackActivity.class));
                ShowFullAds(SettingsActivity.this);
            }
        });
        findViewById(R.id.llPrivacy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, PrivacyPolicyActivity.class));
                ShowFullAds(SettingsActivity.this);
            }
        });
        findViewById(R.id.rlDark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        new AdmobAdsHelper(this).bannerAds(this, findViewById(R.id.ad_layout));
        InAppBillingSetup();
    }

    private void removeadsDialog() {
        final Dialog dialog = new Dialog(SettingsActivity.this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        final View alertView = getLayoutInflater().inflate(R.layout.dialog_removead, null);
        dialog.setContentView(alertView);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);

        alertView.findViewById(R.id.ivClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        alertView.findViewById(R.id.removeTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InAppPurchase();
                dialog.dismiss();
            }
        });
    }

    public void InAppPurchase() {
        List<String> skuList = new ArrayList<>();
        skuList.add(Helper.REMOVE_ADS_PRODUCT_ID);
        SkuDetailsParams.Builder newBuilder = SkuDetailsParams.newBuilder();
        newBuilder.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        mBillingClient.querySkuDetailsAsync(newBuilder.build(), (billingResult, list) -> {
            for (SkuDetails skuDetails : list) {
                String sku = skuDetails.getSku();
                if (Helper.REMOVE_ADS_PRODUCT_ID.equals(sku)) {
                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetails)
                            .build();
                    mBillingClient.launchBillingFlow(SettingsActivity.this, flowParams).getResponseCode();
                }
            }
        });
    }

    private void InAppBillingSetup() {
        BillingClient build = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
        this.mBillingClient = build;
        build.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
            }

            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
            }
        });
        queryPurchases();
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
        } else if (billingResult.getResponseCode() != 1 && billingResult.getResponseCode() == 7) {
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
                    }
                }
            };
            if (!purchase.isAcknowledged()) {
                this.mBillingClient.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build(), acknowledgePurchaseResponseListener);
            } else if (purchase.getProducts().contains(Helper.REMOVE_ADS_PRODUCT_ID)) {
                FastSave.getInstance().saveBoolean(Helper.REMOVE_ADS_KEY, true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!FastSave.getInstance().getBoolean(Helper.REMOVE_ADS_KEY, false)) {
            LoadFullAd(this);
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

            InterstitialAd.load(context, FastSave.getInstance().getString("INTER", ""), interstitial_adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    ad_mob_interstitial = interstitialAd;
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    ad_mob_interstitial = null;
                    Log.e("TAG", "AdError1: " + loadAdError.getMessage());
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
                    Log.e("TAG", "AdError: " + adError.getMessage());
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

    @Override
    public void onBackPressed() {
        ShowFullAds(this);
        finish();
    }


}