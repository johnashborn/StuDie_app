package com.example.studie.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studie.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private List<Subject> subjects;

    public SubjectAdapter(List<Subject> subjects) {
        this.subjects = subjects;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjects.get(position);
        holder.bind(subject);
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView subjectNameText;
        private TextView cardCountText;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.subjectCard);
            subjectNameText = itemView.findViewById(R.id.subjectName);
            cardCountText = itemView.findViewById(R.id.cardCount);
        }

        public void bind(Subject subject) {
            subjectNameText.setText(subject.subjectName);

            // Show number of cards
            int cardCount = subject.cards != null ? subject.cards.size() : 0;
            cardCountText.setText(cardCount + " cards");

            // Set click listener to open ReviewFlashcards
            cardView.setOnClickListener(v -> {
                Context context = itemView.getContext();
                Intent intent = new Intent(context, ReviewFlashcards.class);
                intent.putExtra("subjectName", subject.subjectName);
                context.startActivity(intent);
            });
        }
    }
}