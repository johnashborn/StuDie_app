package com.example.studie;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class camCalculator extends AppCompatActivity {

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        } else {
            startCamera();
        }

        ImageButton capture = findViewById(R.id.captureButton);
        capture.setOnClickListener(v -> photoSolver());



    }



    private PreviewView camera;
    private ProcessCameraProvider camProv;

    private ImageCapture problem;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
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
                        try{
                            image = InputImage.fromFilePath(getApplicationContext(), Uri.fromFile(photoFile));
                            TextRecognizer math = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                            math.process(image)
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
                        }catch (IOException e){
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(camCalculator.this,"Failed to capture the image boss",Toast.LENGTH_SHORT).show();
                    }
                });
    }

}