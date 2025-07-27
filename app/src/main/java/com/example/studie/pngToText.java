package com.example.studie;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ext.SdkExtensions;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresExtension;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;


import java.io.IOException;


public class pngToText extends AppCompatActivity {

    ActivityResultLauncher<Intent> resultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_png_to_text);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Button gallery = findViewById(R.id.toGallery);
         textView = findViewById(R.id.photoText);

        gallery.setOnClickListener(v->{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
                clicked();
            }
        });

        registerResult();
    }

    private Uri imageUri;
    MultiAutoCompleteTextView textView;



    // para pang kuha sa image URI
    private void registerResult(){
        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                try {
                    imageUri = o.getData().getData();
                    try {
                        processImage();
                    } catch (IOException e) {
                        Toast.makeText(pngToText.this, "Something is wrong on the registerResult part boss", Toast.LENGTH_SHORT).show();
                    }
                    Log.d("Uri", "Returned uri: " + imageUri);

                } catch (Exception e) {
                    Toast.makeText(pngToText.this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //pangkuha ug image sa gallery
    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    private void clicked(){
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }

    private void processImage() throws IOException {
        InputImage inputImage = InputImage.fromFilePath(this,imageUri);
        TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        textRecognizer.process(inputImage)
                .addOnSuccessListener(text -> {
                    String result = text.getText();
                    Log.d("Gallery text","Text: "+result);
                    Toast.makeText(this, "Image successfully extracted?", Toast.LENGTH_SHORT).show();
                    textView.setText(result);
                }).addOnFailureListener( e -> {
                    Toast.makeText(this, "Failed to extract Image..hehe", Toast.LENGTH_SHORT).show();
                });

    }




}