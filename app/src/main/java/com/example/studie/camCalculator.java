package com.example.studie;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studie.ui.CameraOverlayView;
import com.google.common.cache.Cache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.matheclipse.core.reflection.system.In;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class camCalculator extends AppCompatActivity {


    ActivityResultLauncher<Intent> resultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cam_calculator);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);


            return insets;
        });

        camera = findViewById(R.id.previewView);
        overlayView = findViewById(R.id.overlay);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        } else {
            startCamera();
        }

        ImageButton capture = findViewById(R.id.captureButton);
        capture.setOnClickListener(v -> photoSolver());

        ImageButton gallery = findViewById(R.id.galleryButton);
        registerResult();


        gallery.setOnClickListener(v -> pickImage());

        overlayView = findViewById(R.id.overlay);


    }





    private PreviewView camera;
    private ProcessCameraProvider camProv;

    private ImageCapture problem;

    private Uri trueImageUri;

    private CameraOverlayView overlayView;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
        }
    }


    private void pickImage(){
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }

    private void registerResult(){
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        try{
                            Uri imageUri = o.getData().getData();
                            trueImageUri = imageUri;
                            processGalleryUri();
                            Log.d("UriCheck", "Returned URI: " + trueImageUri.toString());

                        }catch (Exception e){
                            Toast.makeText(camCalculator.this, "No image selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void processGalleryUri(){
        try {
            InputImage galleryImage = InputImage.fromFilePath(this,trueImageUri);
            TextRecognizer galleryRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            galleryRecognizer.process(galleryImage)
                    .addOnSuccessListener(visionText ->{
                        String resultText = visionText.getText();
                        Log.d("MlKIT","Math problem: "+resultText);
                        Toast.makeText(this, "Math Problem: "+resultText, Toast.LENGTH_SHORT).show();

                        // after a successful photo recognition. we will launch an intent
                        Intent intent = new Intent(camCalculator.this, solution.class);
                        intent.putExtra("math_problem",resultText);
                        startActivity(intent);


                    }).addOnFailureListener(e->{
                        Toast.makeText(this, "failed to extract image", Toast.LENGTH_SHORT).show();
                    });


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }






    private void startCamera(){
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(()->{
            try{
                camProv = cameraProviderFuture.get();


                // set up sa preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(camera.getSurfaceProvider());

                // Set nato ang atung problema
                problem = new ImageCapture.Builder().build();


                // pili anf back camera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                camProv.unbindAll();
                camProv.bindToLifecycle(this,cameraSelector,preview,problem);

            }catch (ExecutionException | InterruptedException e){
                e.printStackTrace();
            }
        },ContextCompat.getMainExecutor(this));
    }


    private void photoSolver(){
        // gama or prepare sa file kung asa i save ang image
        File photoFile = new File(getCacheDir(),"captured_image.jpg");

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // captures the exact image? I hope???
        problem.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        runOnUiThread(() -> Toast.makeText(camCalculator.this,"Image successfully taken",Toast.LENGTH_SHORT).show());

                        // run the Ml kit sa na saved na nga image para i solve niya
                        InputImage image;
                        Bitmap fullMap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

                        // get the dimensions sa preview and sa bitmap
                        int viewWidth = camera.getWidth();
                        int viewHeight = camera.getHeight();

                        // sa bitmap
                        int bitmapWidth = fullMap.getWidth();
                        int bitmapHeight = fullMap.getHeight();

                        //get the cut- out rectangle adtong sa overlay sa camera
                        RectF cutOut= overlayView.getCutOutRect();

                        //convert the rectangle sa bitmap scale
                        float scaleX = (float) bitmapWidth/viewWidth;
                        float scaleY = (float) bitmapHeight/ viewHeight;

                        Rect cropRectangle = new Rect(
                                (int) (cutOut.left * scaleX),
                                (int) (cutOut.top * scaleY),
                                (int) (cutOut.right * scaleX),
                                (int) (cutOut.bottom * scaleY)
                        );

                        // Crop the Bitmap
                        int left = Math.max(0, cropRectangle.left);
                        int top = Math.max(0, cropRectangle.top);
                        int right = Math.min(bitmapWidth, cropRectangle.right);
                        int bottom = Math.min(bitmapHeight, cropRectangle.bottom);

                        int width = right - left;
                        int height = bottom - top;

                        Bitmap croppedBitmap = Bitmap.createBitmap(fullMap, left, top, width, height);


                        //I send na dayun ang na cropped nga image sa MLKIT
                        InputImage cropped = InputImage.fromBitmap(croppedBitmap,0);

                        TextRecognizer math = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                        math.process(cropped)
                                .addOnSuccessListener(visionText ->{
                                    //naay buhaton sa na recognize nga math problem
                                    String resultText = visionText.getText();
                                    Log.d("MLKIT","a math problem: \n"+resultText);
                                    Toast.makeText(camCalculator.this,"Math Problem: "+resultText,Toast.LENGTH_SHORT).show();


                                    // After a successful text recggnition, we will launch another screen nga adto ipakita ang solution
                                    Intent intent = new Intent(camCalculator.this, solution.class);
                                    intent.putExtra("math_problem", resultText);
                                    startActivity(intent);
                                }).addOnFailureListener(e ->{
                                    Toast.makeText(camCalculator.this,"Can't solve this one boss",Toast.LENGTH_SHORT).show();
                                });

                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(camCalculator.this,"Failed to capture the image boss",Toast.LENGTH_SHORT).show();
                    }
                });
    }

}