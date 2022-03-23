package com.daud.imagecropwithucrop;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private ImageView img;
    private Button btn;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private String ACTION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img = findViewById(R.id.img);
        btn = findViewById(R.id.btn);


        btn.setOnClickListener(view -> {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            View view1 = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null);
            alertDialog.setView(view1);
            ImageView camera = view1.findViewById(R.id.camera);
            ImageView gallery = view1.findViewById(R.id.gallery);

            camera.setOnClickListener(view2 -> {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                activityResultLauncher.launch(intent);
                alertDialog.dismiss();
                ACTION = "CAMERA";
            });

            gallery.setOnClickListener(view2 -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
                activityResultLauncher.launch(intent);
                alertDialog.dismiss();
                ACTION = "GALLERY";
            });
            alertDialog.setCancelable(false);
            alertDialog.show();
        });

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result!=null){
                    if (ACTION.equals("CAMERA")){
                        Bundle bundle = result.getData().getExtras();
                        Bitmap imgBitmap = (Bitmap) bundle.get("data");
                        Uri imgUri = getImageUri(MainActivity.this,imgBitmap);
                        uCropStart(imgUri);
                    }else if (ACTION.equals("GALLERY")){
                        Uri imgUri = result.getData().getData();
                        uCropStart(imgUri);
                    }
                }
            }
        });
    }
    public Uri getImageUri(MainActivity inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Image", null);
        return Uri.parse(path);
    }

    public void uCropStart(Uri sourceUri){
        String destinationString = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(),destinationString));
        UCrop.of(sourceUri,destinationUri)
                .withMaxResultSize(1920, 1920)
                .withAspectRatio(1, 1)
                .start(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){
            final Uri imgUri = UCrop.getOutput(data);
            img.setImageURI(imgUri);
        }else{
            final Throwable uCropError = UCrop.getError(data);
        }
    }
}