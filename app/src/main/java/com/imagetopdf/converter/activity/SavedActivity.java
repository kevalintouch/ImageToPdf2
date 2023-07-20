package com.imagetopdf.converter.activity;

import static com.imagetopdf.converter.Utils.Helper.showAdsNumberCount;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import com.appizona.yehiahd.fastsave.FastSave;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.imagetopdf.converter.R;
import com.imagetopdf.converter.Utils.Helper;
import com.imagetopdf.converter.Utils.RatingDialogBuilder;
import com.imagetopdf.converter.ads.AdmobAdsHelper;

import java.io.File;

public class SavedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        String filepath = getIntent().getStringExtra("FILEPATH");

        TextView tvPDFName = findViewById(R.id.tvPDFName);
        TextView tvFilePath = findViewById(R.id.tvFilePath);
        TextView tvView = findViewById(R.id.tvView);
        TextView tvShare = findViewById(R.id.tvShare);

        tvPDFName.setText(new File(filepath).getName());
        tvFilePath.setText("Location: " + filepath);

        Helper.generateImageFromPdf(SavedActivity.this, FileProvider.getUriForFile(SavedActivity.this, getPackageName() + ".provider", new File(filepath)), ((ImageView) findViewById(R.id.ivPDF)));

        findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tvView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent target = new Intent(Intent.ACTION_VIEW);
                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", new File(filepath));
                target.setDataAndType(contentUri, "application/pdf");
                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Intent intent = Intent.createChooser(target, "Open File");
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(SavedActivity.this, "Install PDF reader application", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", new File(filepath));
                Intent target = ShareCompat.IntentBuilder.from(SavedActivity.this).setStream(contentUri).getIntent();
                target.setData(contentUri);
                target.setType("application/pdf");
                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (target.resolveActivity(getPackageManager()) != null) {
                    startActivity(target);
                }
            }
        });

        if (!FastSave.getInstance().getBoolean("ISREVIEW", false)) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Dialog build = new RatingDialogBuilder(SavedActivity.this, () -> {
                    }).build();
                    build.show();
                }
            }, 3000L);
        }

        new AdmobAdsHelper(this).bannerAds(this,findViewById(R.id.ad_layout));
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

    @Override
    public void onBackPressed() {
        ShowFullAds(this);
        finish();
    }
}