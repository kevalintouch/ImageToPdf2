package com.imagetopdf.converter.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;
import android.widget.TextView;

import com.imagetopdf.converter.R;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;


public class Helper {
    public static boolean is_show_open_ad = true;

    public static void openUpdateDialog(Context context) {
        Dialog dialog = new Dialog(context, R.style.CustomDialog);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.dialog_update);
        dialog.findViewById(R.id.tv_maybe_later).setOnClickListener(view -> {
            ((Activity) context).finishAffinity();
            dialog.dismiss();
        });
        dialog.findViewById(R.id.tv_submit).setOnClickListener(view -> {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName()));
            intent.addFlags(1208483840);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException unused) {
            }
        });
        dialog.show();
    }

    public static void openExitDialog(Context context) {
        Dialog dialog = new Dialog(context, R.style.CustomDialog);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.dialog_update);

        ((TextView) dialog.findViewById(R.id.tv_dialog_title)).setText("Exit");
        ((TextView) dialog.findViewById(R.id.tv_dialog_title_2)).setText("Are you sure, you want to exit?");
        ((TextView) dialog.findViewById(R.id.tv_submit)).setText("Exit");
        ((TextView) dialog.findViewById(R.id.tv_maybe_later)).setText("Cancel");

        dialog.findViewById(R.id.tv_maybe_later).setOnClickListener(view -> {
            dialog.dismiss();
        });
        dialog.findViewById(R.id.tv_submit).setOnClickListener(view -> {
            ((Activity) context).finishAffinity();
        });
        dialog.show();
    }

    public static void generateImageFromPdf(Context context, Uri pdfUri, ImageView imageView) {
        int pageNumber = 0;
        PdfiumCore pdfiumCore = new PdfiumCore(context);
        try {
            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(pdfUri, "r");
            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
            pdfiumCore.openPage(pdfDocument, pageNumber);
            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height);
            imageView.setImageBitmap(bmp);
            pdfiumCore.closeDocument(pdfDocument); // important!
        } catch (Exception e) {
            //todo with exception
        }
    }
}
