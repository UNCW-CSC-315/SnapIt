package com.example.snapit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SnapActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private ConstraintLayout mLoggedInGroup;
    private ConstraintLayout mLoggedOutGroup;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap);

        mLoggedInGroup = findViewById(R.id.logged_in_group);
        mLoggedOutGroup = findViewById(R.id.logged_out_group);
        mImageView = findViewById(R.id.preview);

        updateUI(mAuth.getCurrentUser());

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            currentPhotoPath = savedInstanceState.getString(CURRENT_PHOTO_KEY);
            if (currentPhotoPath != null) {
                try {
                    Uri contentUri = Uri.fromFile(new File(currentPhotoPath));
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentUri);
                    mImageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            mLoggedOutGroup.setVisibility(View.GONE);
            mLoggedInGroup.setVisibility(View.VISIBLE);
        } else {
            mLoggedInGroup.setVisibility(View.GONE);
            mLoggedOutGroup.setVisibility(View.VISIBLE);
        }
    }


    private static final int REQUEST_IMAGE_CAPTURE = 129;
    private String currentPhotoPath;

    public void takePicture(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create an image file name
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                String imageFileName = "SnapIt_" + timeStamp;
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File photoFile = File.createTempFile(imageFileName, ".jpg",  storageDir);
                currentPhotoPath = photoFile.getAbsolutePath();

                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.snapit.fileprovider",
                        photoFile);

//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException ex) {
                Log.e(TAG, ex.toString());
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);

//            Log.d(TAG, "Saved the image to " + currentPhotoPath);
//            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            Uri contentUri = Uri.fromFile(new File(currentPhotoPath));
//            mediaScanIntent.setData(contentUri);
//            this.sendBroadcast(mediaScanIntent);
//
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentUri);
//                mImageView.setImageBitmap(bitmap);
//            } catch (IOException e) {
//                Log.e(TAG, e.toString());
//            }
//
//            saveImage(contentUri);
        }
    }

    private static final String CURRENT_PHOTO_KEY = "current photo";

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(CURRENT_PHOTO_KEY, currentPhotoPath);
        super.onSaveInstanceState(outState);
    }

}
