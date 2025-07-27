package com.example.studie;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCallerLauncher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.speech.SpeechRecognizer;
import android.speech.RecognizerIntent;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class speechToText extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_speech_to_text);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
        }


        EditText text = findViewById(R.id.transcribedText);
        FloatingActionButton record = findViewById(R.id.recordButton);
        TextView status = findViewById(R.id.recordingStatusText);

        //set the clear button nga mwala
        Button clear = findViewById(R.id.clearButton);
        clear.setOnClickListener(v -> text.setText(""));


        //initialize the speech recognizer
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        // sets up the intent for the uhhh speech recognizer
        Intent speech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"fil-PH");
        speech.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak now please");


        // setes the record button up
        record.setOnClickListener(v -> {
            if (!isListening) {
                speechRecognizer.startListening(speech);
                status.setText("Listening...");
                isListening = true;
            } else {
                speechRecognizer.stopListening();
                status.setText("Tap to start recording");
                isListening = false;
            }
        });



        // the actual magic
        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    text.append(" " + matches.get(0));

                }
            }
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {
                status.setText("Listening....");

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
                isListening = false;
                status.setText("Tap to record");

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

    }

    private boolean isListening = false;
    Uri speechUri;


    // for the permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
        } else {
            // Permission denied
        }
    }





}