package com.example.studie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studie.ui.notifications.NotificationsFragment;

public class Profile extends AppCompatActivity {
        String name;

    String[] Gender = {"Male","Female","Batman"};
    String[] Educational_level = {"Elementary","High School","College"};

    AutoCompleteTextView autoCompleteTextView;


    ArrayAdapter<String> adapterItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.age), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        autoCompleteTextView = findViewById(R.id.GenderSelect);
        adapterItems = new ArrayAdapter<String>(this,R.layout.list_item,Gender);


        autoCompleteTextView.setAdapter(adapterItems);


        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(Profile.this,"Gender " + item,Toast.LENGTH_SHORT).show();
            }
        });

        autoCompleteTextView = findViewById(R.id.EducLevel);
        adapterItems = new ArrayAdapter<String>(this,R.layout.list_item,Educational_level);


        autoCompleteTextView.setAdapter(adapterItems);


        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(Profile.this,"Educational level:  " + item,Toast.LENGTH_SHORT).show();
            }
        });





        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");
        String pass = intent.getStringExtra("password");


        TextView ProfileName = findViewById(R.id.ProfileName);
        ProfileName.setText(name);

    }



    public void toMainMenu(View v){
        Intent intent = new Intent(this, mainHome.class);

        AutoCompleteTextView gender = findViewById(R.id.GenderSelect);
        AutoCompleteTextView educLevel = findViewById(R.id.EducLevel);
        TextView age = findViewById(R.id.ageSelect);

        String realGender = gender.getText().toString();
        String realEducLevel = educLevel.getText().toString();
        String realAge = age.getText().toString();

        String name = ((TextView) findViewById(R.id.ProfileName)).getText().toString();


        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("name", name);
        editor.putString("gender", realGender);
        editor.putString("level", realEducLevel);
        editor.putString("age",realAge);
        editor.putBoolean("isLoggedIn", true);  // para ma check ang log-in status bro

        editor.apply();




        startActivity(intent);
        finish();



    }


}