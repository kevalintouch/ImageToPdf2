package com.imagetopdf.converter.ads;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.appizona.yehiahd.fastsave.FastSave;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.imagetopdf.converter.R;
import com.imagetopdf.converter.Utils.Helper;

public class AdmobAdsHelper {
    Context context;
//    private AdView adView;
    public AdmobAdsHelper(Context context) {
        this.context = context;
    }

    public void bannerAds(Context mContext, ViewGroup frameLayout) {
        if (!FastSave.getInstance().getBoolean(Helper.REMOVE_ADS_KEY, false)) {
            String admonnative = FastSave.getInstance().getString("NATIVE", "");

            Log.e("TAG", "nativerAds: " + admonnative);
            new AdLoader.Builder(mContext, admonnative)
                    .forNativeAd(nativeAd -> {
                        if (frameLayout != null) {
                            frameLayout.removeAllViews();
                            NativeAdView unifiedNativeAdView = (NativeAdView) LayoutInflater.from(mContext).inflate(R.layout.custom_native_ads, null);
                            frameLayout.addView(unifiedNativeAdView);
                            populateUnifiedNativeAdView(nativeAd, unifiedNativeAdView);
                        }
                    }).withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.e("TAG", "bannerAds: " + loadAdError.getMessage());
                        }
                    }).build().loadAd(new AdRequest.Builder().build());
        }
    }

    public void populateUnifiedNativeAdView(NativeAd unifiedNativeAd, NativeAdView unifiedNativeAdView) {
        TextView ad_headline = unifiedNativeAdView.findViewById(R.id.ad_headline);
        TextView ad_body = unifiedNativeAdView.findViewById(R.id.ad_body);
        TextView ad_call_to_action = unifiedNativeAdView.findViewById(R.id.ad_call_to_action);
        ImageView ad_app_icon = unifiedNativeAdView.findViewById(R.id.ad_app_icon);

        unifiedNativeAdView.setHeadlineView(ad_headline);
        unifiedNativeAdView.setBodyView(ad_body);
        unifiedNativeAdView.setCallToActionView(ad_call_to_action);
        unifiedNativeAdView.setIconView(ad_app_icon);
        try {
            ad_headline.setText(unifiedNativeAd.getHeadline());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (unifiedNativeAd.getBody() == null) {
                ad_body.setVisibility(View.INVISIBLE);
            } else {
                ad_body.setVisibility(View.VISIBLE);
                ad_body.setText(unifiedNativeAd.getBody());
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            if (unifiedNativeAd.getCallToAction() == null) {
                ad_call_to_action.setVisibility(View.INVISIBLE);
            } else {
                ad_call_to_action.setVisibility(View.VISIBLE);
                ad_call_to_action.setText(unifiedNativeAd.getCallToAction());
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            if (unifiedNativeAd.getIcon() == null) {
                ad_app_icon.setVisibility(View.GONE);
            } else {
                ad_app_icon.setImageDrawable(unifiedNativeAd.getIcon().getDrawable());
                ad_app_icon.setVisibility(View.VISIBLE);
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        unifiedNativeAdView.setNativeAd(unifiedNativeAd);
    }
}
