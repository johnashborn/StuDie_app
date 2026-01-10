package com.example.studie.ui;

// FlashcardStorage.java
import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FlashcardStorage {
    private static final String FILE_NAME = "flashcards.json";

    public static void saveSubjects(Context context, List<Subject> subjects) {
        Gson gson = new Gson();
        String json = gson.toJson(subjects);

        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Subject> loadSubjects(Context context) {
        Gson gson = new Gson();
        List<Subject> subjects = new ArrayList<>();

        try (FileInputStream fis = context.openFileInput(FILE_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            Type type = new TypeToken<List<Subject>>() {}.getType();
            subjects = gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return subjects != null ? subjects : new ArrayList<>();
    }
}
