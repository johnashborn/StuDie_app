package com.example.studie.ui;

// FlashcardAdapter.java
// FlashcardAdapter.java
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studie.R;

import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.ViewHolder> {
    private final List<Flashcard> data;

    public FlashcardAdapter(List<Flashcard> data) {
        this.data = data;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView frontText, backText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            frontText = itemView.findViewById(R.id.frontText);
            backText = itemView.findViewById(R.id.backText);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Flashcard card = data.get(position);
        holder.frontText.setText(card.front);
        holder.backText.setText(card.back);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}