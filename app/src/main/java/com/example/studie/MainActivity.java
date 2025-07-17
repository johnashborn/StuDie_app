package com.example.studie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.age), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //Checks if the user is already logged in
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if(isLoggedIn == true){
            Intent logIntent = new Intent(this, mainHome.class);
            startActivity(logIntent);
            finish(); // para di mo balik sa login
            return;
        }




    }

    public void logInPressed(View b){

        EditText logEmail = findViewById(R.id.logInEmail);
        EditText logPass = findViewById(R.id.logInPassword);

        String email = logEmail.getText().toString();
        String password = logPass.getText().toString();


        Intent intent =  new Intent(this, mainHome.class);

        if(!email.isEmpty() && !password.isEmpty()){
            // Save login state
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isLoggedIn", true);  // this is the key that will persist login...no shit
            editor.putString("email", email);       // no Shit
            editor.putString("password", password);
            editor.apply();  // save changes

            intent.putExtra("email", email);
            intent.putExtra("password", password);
            startActivity(intent);
            finish();
            Log.d("it worked", "But why?"); //prayer
        }else{
            Toast.makeText(this,"Fill out required fields",Toast.LENGTH_SHORT).show();
        }





    }

    public void SignUp(View b){
        Intent intent = new Intent(this, SignUp_Activity.class);
        startActivity(intent);
    }
}