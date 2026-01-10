package com.example.studie; // use your actual package name

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studie.ui.Flashcard;
import com.example.studie.ui.FlashcardAdapter;
import com.example.studie.ui.FlashcardStorage;
import com.example.studie.ui.Subject;

import java.util.ArrayList;
import java.util.List;

public class flashcards extends AppCompatActivity {

    EditText subjectNameInput, frontInput, backInput;
    Button addCardButton, saveSetButton;
    RecyclerView flashcardPreviewList;
    List<Flashcard> flashcards = new ArrayList<>();
    FlashcardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcards);

        // Initialize views
        subjectNameInput = findViewById(R.id.subjectNameInput);
        frontInput = findViewById(R.id.frontInput);
        backInput = findViewById(R.id.backInput);
        addCardButton = findViewById(R.id.addCardButton);
        saveSetButton = findViewById(R.id.saveSetButton);
        flashcardPreviewList = findViewById(R.id.flashcardPreviewList);

        // Setup RecyclerView
        adapter = new FlashcardAdapter(flashcards);
        flashcardPreviewList.setLayoutManager(new LinearLayoutManager(this));
        flashcardPreviewList.setAdapter(adapter);

        // Add card button logic
        addCardButton.setOnClickListener(v -> {
            String front = frontInput.getText().toString().trim();
            String back = backInput.getText().toString().trim();
            if (!front.isEmpty() && !back.isEmpty()) {
                flashcards.add(new Flashcard(front, back));
                adapter.notifyItemInserted(flashcards.size() - 1);
                frontInput.setText("");
                backInput.setText("");
            }
        });

        // Save set button logic (placeholder for now)
        saveSetButton.setOnClickListener(v -> {
            String subjectName = subjectNameInput.getText().toString().trim();
            if (!subjectName.isEmpty() && !flashcards.isEmpty()) {
                // Load existing subjects
                List<Subject> subjects = FlashcardStorage.loadSubjects(this);

                // Create new subject and add cards
                Subject newSubject = new Subject(subjectName);
                for (Flashcard card : flashcards) {
                    newSubject.addCard(card);
                }

                // Add to list and save
                subjects.add(newSubject);
                FlashcardStorage.saveSubjects(this, subjects);

                // Close activity
                finish();
            }
        });
    }
}