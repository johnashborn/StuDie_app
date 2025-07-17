package com.example.studie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toolbar;

import com.example.studie.ui.notifications.NotificationsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.studie.databinding.ActivityMainHomeBinding;

public class mainHome extends AppCompatActivity {

    private ActivityMainHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMainHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main_home);

        NavigationUI.setupWithNavController(binding.navView, navController);



       Intent intent = getIntent();

       String name = intent.getStringExtra("name");
       String gender = intent.getStringExtra("gender");
       String level = intent.getStringExtra("level");
       String age = intent.getStringExtra("age");


        //  Fallback to SharedPreferences if any value is null since mo crash siya if i reset
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        if (name == null) name = prefs.getString("name", "Default Name");
        if (gender == null) gender = prefs.getString("gender", "Unknown");
        if (level == null) level = prefs.getString("level", "Unknown");
        if (age == null) age = prefs.getString("age", "Unknown");

       // prepares the bundle para i send sa notification fragment
        Bundle bundle    = new Bundle();
        bundle.putString("name",name);
        bundle.putString("gender", gender);
        bundle.putString("level", level);
        bundle.putString("age",age);







    }

}