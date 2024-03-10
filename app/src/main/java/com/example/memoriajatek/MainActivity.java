package com.example.memoriajatek;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    public void startGame(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("selected_country", MODE_PRIVATE);
        String selectedCountry = sharedPreferences.getString("country", null);

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("selectedCountry", selectedCountry);

        startActivity(intent);
    }


    public void profile(View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent;
        if (currentUser != null) {
            // Ha a felhasználó be van jelentkezve, irányítsd a Firebase Activity-re
            intent = new Intent(this, Firebase.class);
        } else {
            // Ha a felhasználó nincs bejelentkezve, irányítsd a Login Activity-re
            intent = new Intent(this, Login.class);
        }
        startActivity(intent);
    }



    public void chooseCountry(View view) {
        Intent intent = new Intent(this, CountryActivity.class);
        startActivity(intent);
    }

    public void leaderboard(View view) {
        Intent intent = new Intent(this, LeaderboardActivity.class);
        startActivity(intent);
    }
}
