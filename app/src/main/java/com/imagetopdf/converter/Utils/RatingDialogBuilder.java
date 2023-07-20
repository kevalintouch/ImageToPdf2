package com.imagetopdf.converter.Utils;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appizona.yehiahd.fastsave.FastSave;
import com.imagetopdf.converter.R;


public class RatingDialogBuilder {
    final Dialog mDialog;
    final ImageView[] mStar = new ImageView[5];
    int rate = 0;
    final TextView tvMayBeLater;
    final TextView tvSubmit;


    public interface Callback {
        void onDialogDismiss();
    }

    public RatingDialogBuilder(final Context context, final Callback callback) {
        Dialog dialog = new Dialog(context, R.style.CustomDialog);
        this.mDialog = dialog;
        dialog.requestWindowFeature(1);
        this.mDialog.setContentView(R.layout.dialog_rating);
        this.tvSubmit = this.mDialog.findViewById(R.id.tv_submit);
        this.tvMayBeLater = this.mDialog.findViewById(R.id.tv_maybe_later);
        this.mStar[0] = this.mDialog.findViewById(R.id.rate1);
        this.mStar[1] = this.mDialog.findViewById(R.id.rate2);
        this.mStar[2] = this.mDialog.findViewById(R.id.rate3);
        this.mStar[3] = this.mDialog.findViewById(R.id.rate4);
        this.mStar[4] = this.mDialog.findViewById(R.id.rate5);
        this.tvMayBeLater.setOnClickListener(view -> {
            this.mDialog.dismiss();
            callback.onDialogDismiss();
        });
        this.tvSubmit.setOnClickListener(view -> {
            Toast.makeText(context, R.string.thank_for_rating, Toast.LENGTH_SHORT).show();
            this.mDialog.dismiss();
            callback.onDialogDismiss();
        });
        View.OnClickListener onClickListener = view -> {
            switch (view.getId()) {
                case R.id.rate1:
                    this.rate = 1;
                    break;
                case R.id.rate2:
                    this.rate = 2;
                    break;
                case R.id.rate3:
                    this.rate = 3;
                    break;
                case R.id.rate4:
                    this.rate = 4;
                    break;
                case R.id.rate5:
                    this.rate = 5;
                    break;
            }
            setContentRate(this.rate, context, callback);
        };
        this.mStar[0].setOnClickListener(onClickListener);
        this.mStar[1].setOnClickListener(onClickListener);
        this.mStar[2].setOnClickListener(onClickListener);
        this.mStar[3].setOnClickListener(onClickListener);
        this.mStar[4].setOnClickListener(onClickListener);
    }


    private void setContentRate(int i, Context context, Callback callback) {
        int i2 = 0;
        while (true) {
            boolean z = true;
            if (i2 >= 5) {
                break;
            }
            ImageView imageView = this.mStar[i2];
            if (i2 >= i) {
                z = false;
            }
            imageView.setActivated(z);
            i2++;
        }
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName()));
        intent.addFlags(1208483840);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException unused) {
        }
        FastSave.getInstance().saveBoolean("ISREVIEW",true);
        this.mDialog.dismiss();
        callback.onDialogDismiss();
    }

    public Dialog build() {
        return this.mDialog;
    }
}
