package com.example.studie.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studie.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ReviewFlashcards extends AppCompatActivity {

    // UI Elements
    private MaterialCardView questionCard;
    private MaterialCardView answerCard;
    private TextView questionText;
    private TextView questionTextSmall;
    private TextView answerText;
    private TextView progressText;
    private TextView scoreText;
    private TextView instructionText;
    private ProgressBar progressBar;
    private LinearLayout buttonRow;
    private MaterialButton wrongButton;
    private MaterialButton repeatButton;
    private MaterialButton rightButton;

    // Data
    private List<Flashcard> queue = new ArrayList<>();
    private int currentIndex = 0;
    private int correctCount = 0;
    private int totalAnswered = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_flashcards);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        initializeViews();

        // Load flashcards from intent
        loadFlashcards();

        // Set up click listeners
        setupClickListeners();

        // Show first card
        showCard();
    }

    private void initializeViews() {
        questionCard = findViewById(R.id.questionCard);
        answerCard = findViewById(R.id.answerCard);
        questionText = findViewById(R.id.questionText);
        questionTextSmall = findViewById(R.id.questionTextSmall);
        answerText = findViewById(R.id.answerText);
        progressText = findViewById(R.id.progressText);
        scoreText = findViewById(R.id.scoreText);
        instructionText = findViewById(R.id.instructionText);
        progressBar = findViewById(R.id.progressBar);
        buttonRow = findViewById(R.id.buttonRow);
        wrongButton = findViewById(R.id.wrongButton);
        repeatButton = findViewById(R.id.repeatButton);
        rightButton = findViewById(R.id.rightButton);

        // Initially hide answer card and buttons
        answerCard.setVisibility(View.GONE);
        instructionText.setVisibility(View.GONE);
        buttonRow.setVisibility(View.GONE);
    }

    private void loadFlashcards() {
        String subjectName = getIntent().getStringExtra("subjectName");
        if (subjectName == null) {
            questionText.setText("No subject selected");
            return;
        }

        List<Subject> subjects = FlashcardStorage.loadSubjects(this);
        Subject chosen = null;

        for (Subject s : subjects) {
            if (s.subjectName.equals(subjectName)) {
                chosen = s;
                break;
            }
        }

        if (chosen != null && chosen.cards != null && !chosen.cards.isEmpty()) {
            queue.addAll(chosen.cards);
        } else {
            questionText.setText("No flashcards found!");
        }
    }

    private void setupClickListeners() {
        // Tap question card to reveal answer
        questionCard.setOnClickListener(v -> revealAnswer());

        // Tap answer card to hide it (optional)
        answerCard.setOnClickListener(v -> {
            // Optional: allow hiding answer again
            // hideAnswer();
        });

        // Wrong → reinsert after 5 cards
        wrongButton.setOnClickListener(v -> {
            if (currentIndex < queue.size()) {
                Flashcard current = queue.get(currentIndex);
                queue.remove(currentIndex);
                int reinsertPosition = Math.min(currentIndex + 5, queue.size());
                queue.add(reinsertPosition, current);
                totalAnswered++;
                updateScore();
                nextCard();
            }
        });

        // Repeat → reinsert after 10 cards
        repeatButton.setOnClickListener(v -> {
            if (currentIndex < queue.size()) {
                Flashcard current = queue.get(currentIndex);
                queue.remove(currentIndex);
                int reinsertPosition = Math.min(currentIndex + 10, queue.size());
                queue.add(reinsertPosition, current);
                totalAnswered++;
                updateScore();
                nextCard();
            }
        });

        // Correct → remove from queue
        rightButton.setOnClickListener(v -> {
            if (currentIndex < queue.size()) {
                queue.remove(currentIndex);
                correctCount++;
                totalAnswered++;
                updateScore();
                nextCard();
            }
        });
    }

    private void showCard() {
        if (currentIndex < queue.size()) {
            Flashcard card = queue.get(currentIndex);

            // Update card content
            questionText.setText(card.front);
            questionTextSmall.setText(card.front);
            answerText.setText(card.back);

            // Show question card, hide answer card
            questionCard.setVisibility(View.VISIBLE);
            answerCard.setVisibility(View.GONE);
            instructionText.setVisibility(View.GONE);
            buttonRow.setVisibility(View.GONE);

            // Update progress
            updateProgress();
        } else {
            // All cards completed
            showCompletion();
        }
    }

    private void revealAnswer() {
        // Hide question card, show answer card
        questionCard.setVisibility(View.GONE);
        answerCard.setVisibility(View.VISIBLE);
        instructionText.setVisibility(View.VISIBLE);
        buttonRow.setVisibility(View.VISIBLE);
    }

    private void hideAnswer() {
        // Show question card, hide answer card
        questionCard.setVisibility(View.VISIBLE);
        answerCard.setVisibility(View.GONE);
        instructionText.setVisibility(View.GONE);
        buttonRow.setVisibility(View.GONE);
    }

    private void nextCard() {
        // Don't increment index since we're removing cards
        // Just show the next card at the same index
        showCard();
    }

    private void updateProgress() {
        int totalCards = queue.size();
        int cardsCompleted = totalAnswered;

        progressText.setText("Card " + (currentIndex + 1) + " of " + totalCards);

        // Calculate progress percentage
        int totalOriginal = totalCards + totalAnswered - correctCount;
        int progress = totalOriginal > 0 ? (int) ((float) totalAnswered / totalOriginal * 100) : 0;
        progressBar.setProgress(progress);
    }

    private void updateScore() {
        scoreText.setText("Score: " + correctCount + "/" + totalAnswered);
    }

    private void showCompletion() {
        // Update progress to 100%
        progressBar.setProgress(100);
        progressText.setText("Completed!");

        // Calculate percentage
        int percentage = totalAnswered > 0 ? (int) ((float) correctCount / totalAnswered * 100) : 0;

        // Determine performance message
        String message;
        if (percentage >= 90) {
            message = "Excellent work! Brayt kaayo ka dawg 🌟";
        } else if (percentage >= 70) {
            message = "Good job! Normal raka dawg 👍";
        } else {
            message = "Keep practicing! Nah pag study jud tarong 💪";
        }

        // Create and show completion dialog
        showCompletionDialog(percentage, message);
    }

    private void showCompletionDialog(int percentage, String message) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

        // Inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_completion, null);
        builder.setView(dialogView);

        // Get views from dialog
        TextView scoreText = dialogView.findViewById(R.id.dialogScoreText);
        TextView percentageText = dialogView.findViewById(R.id.dialogPercentageText);
        TextView messageText = dialogView.findViewById(R.id.dialogMessageText);
        MaterialButton tryAgainButton = dialogView.findViewById(R.id.tryAgainButton);
        MaterialButton backButton = dialogView.findViewById(R.id.backButton);

        // Set text
        scoreText.setText(correctCount + "/" + totalAnswered);
        percentageText.setText(percentage + "%");
        messageText.setText(message);

        // Create dialog
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Prevent dismissing by tapping outside

        // Try Again button - restart the session
        tryAgainButton.setOnClickListener(v -> {
            dialog.dismiss();
            restartFlashcards();
        });

        // Back button - close activity
        backButton.setOnClickListener(v -> {
            dialog.dismiss();
            finish(); // Close the activity and return to previous screen
        });

        dialog.show();
    }

    private void restartFlashcards() {
        // Reset counters
        currentIndex = 0;
        correctCount = 0;
        totalAnswered = 0;

        // Reload flashcards
        queue.clear();
        loadFlashcards();

        // Show first card
        showCard();
    }
}