package com.imagetopdf.converter.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import com.imagetopdf.converter.R;
import com.imagetopdf.converter.Utils.Helper;

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

    }
}