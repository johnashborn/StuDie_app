package com.example.studie;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignUp_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.age), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    public void GetInfo(View v ){
        EditText name = findViewById(R.id.CreateName);
        EditText  email = findViewById(R.id.CreateEmail);
        EditText pass = findViewById(R.id.CreatePassword);

        String createdName = name.getText().toString();
        String createdEmail = email.getText().toString();
        String createdPass = pass.getText().toString();

        Intent intent = new Intent(this, Profile.class);

        //input validation
        if(!createdName.isEmpty() &&!createdEmail.isEmpty() && !createdPass.isEmpty()){
            intent.putExtra("name",createdName);
            intent.putExtra("email",createdEmail);
            intent.putExtra("password",createdPass);
            startActivity(intent);
        }else{
            Toast.makeText(this,"Fill up all requirements",Toast.LENGTH_SHORT).show();
        }

    }
}