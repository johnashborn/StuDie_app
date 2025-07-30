package com.example.studie;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ext.SdkExtensions;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresExtension;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studie.ui.home.HomeFragment;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.segmentation.Segmentation;
import com.google.mlkit.vision.segmentation.SegmentationMask;
import com.google.mlkit.vision.segmentation.Segmenter;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;



public class delete_background extends AppCompatActivity {

    ActivityResultLauncher<Intent> resultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delete_background);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });





        image = findViewById(R.id.mainImageView);

        process = findViewById(R.id.processButton);

        loadingOverlay = findViewById(R.id.loadingOverlay);

        process.setOnClickListener(v->{
            Bitmap bitmap = getBitmapFromImage(imageUri);
            if(bitmap!=null){
                runSegmentation(bitmap);
            }else{
                Toast.makeText(this, "Failed to load bitmap", Toast.LENGTH_SHORT).show();
            }
        });




        MaterialButton galleryButton = findViewById(R.id.selectImageButton);
        galleryButton.setOnClickListener(v->{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
                galleryOpener();
            }
        });

        ImageView back = findViewById(R.id.backButton);
        back.setOnClickListener(v->{
            finish();

        });

        registerResult();
    }

    private Uri imageUri;
    private ImageView image;
    private Bitmap processedBitmap;


    private MaterialButton process;



    FrameLayout loadingOverlay;




    //gallery opener

    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    private void galleryOpener(){
        Intent intent =  new Intent(MediaStore.ACTION_PICK_IMAGES);
       resultLauncher.launch(intent);
    }


    // mo kuha sa URI sa image and sets the imageview sa URI
    private void registerResult(){
        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
               try{
                   assert o.getData() != null;
                   imageUri= o.getData().getData();
                   image.setImageURI(imageUri);
                   process.setEnabled(true);
                   Log.d("Image URI","Image uri: "+imageUri);
               } catch (Exception e) {
                   Toast.makeText(delete_background.this, "Please select an image next time", Toast.LENGTH_SHORT).show();
               }
            }
        });
    }


    // the actual magic, uses the ml kit and tells it how to handle the image input (nagpatabang nako ni chat gpt ani)
    private Segmenter initializeSegmenter() {
        SelfieSegmenterOptions options =
                new SelfieSegmenterOptions.Builder()
                        .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                        .build();

        return Segmentation.getClient(options);

    }

    //converts image URI to Bitmap
    private Bitmap getBitmapFromImage(Uri uri){
        try{
            return  MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);

        } catch (IOException e) {
           e.printStackTrace();
           return  null;
        }
    }

    //converts the bitmap into input image and calls the segmenter...I hope
    private void runSegmentation(Bitmap bitmap) {
        loadingOverlay.setVisibility(View.VISIBLE);

        Segmenter segmenter = initializeSegmenter();

        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);

        segmenter.process(inputImage)
                .addOnSuccessListener(mask -> {
                    processedBitmap = applyMaskToBitmap(bitmap, mask);
                    image.setImageBitmap(processedBitmap);
                    loadingOverlay.setVisibility(View.GONE);
                    promptSaveDialog(processedBitmap);

                    Log.d("Segmentation", "Success! Got mask");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Segmentation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingOverlay.setVisibility(View.GONE);
                    e.printStackTrace();
                });
    }

    private Bitmap applyMaskToBitmap(Bitmap original, SegmentationMask mask) {
        Bitmap result = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        ByteBuffer maskBuffer = mask.getBuffer();
        maskBuffer.rewind();

        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                int pixel = original.getPixel(x, y);
                float foregroundConfidence = maskBuffer.getFloat();

                if (foregroundConfidence > 0.6) {
                    result.setPixel(x, y, pixel); // keep foreground
                } else {
                    result.setPixel(x, y, Color.TRANSPARENT); // remove background
                }
            }
        }

        return result;
    }


    // user prompt if I save ang image
    private void promptSaveDialog(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Save Image")
                .setMessage("Do you want to save this image?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    saveImage(bitmap);
                })
                .setNegativeButton("No", null)
                .show();
    }


    private void saveImage(Bitmap bitmap){
        // set the metadatas needed

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "image_" + System.currentTimeMillis() + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyAppFolder");
        /* This tells Android:
                The file name (e.g., image_1723456200.png)
                File type is PNG
                Store it in Pictures/MyAppFolder (shows in Gallery)*/

        ContentResolver resolver = getContentResolver();
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream outStream = resolver.openOutputStream(imageUri);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();
            Toast.makeText(this, "Image saved to Gallery", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }


    }














}