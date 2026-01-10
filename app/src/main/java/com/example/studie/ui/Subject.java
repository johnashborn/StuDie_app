package com.example.studie.ui;
import java.util.ArrayList;
import java.util.List;


public class Subject {

    public String subjectName;
    public List<Flashcard> cards;

    public Subject(String subjectName) {
        this.subjectName = subjectName;
        this.cards = new ArrayList<>();
    }

    public void addCard(Flashcard card) {
        cards.add(card);
    }

}
