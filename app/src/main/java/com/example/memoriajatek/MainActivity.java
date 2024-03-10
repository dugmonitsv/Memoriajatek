package com.example.memoriajatek;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void startGame(View view) {

        SharedPreferences sharedPreferences = getSharedPreferences("selected_country", MODE_PRIVATE);
        String selectedCountry = sharedPreferences.getString("country", null);
        if(selectedCountry != null){
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("selectedCountry", selectedCountry);

            startActivity(intent);
        }
        else{
            Toast.makeText(this, "Nincs kiválasztva egy ország sem, kérlek válassz ki egy országot!", Toast.LENGTH_SHORT).show();
        }

    }


    public void profile(View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent;
        if (currentUser != null) {
            intent = new Intent(this, Firebase.class);
        } else {
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
