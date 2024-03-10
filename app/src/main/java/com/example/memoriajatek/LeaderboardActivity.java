package com.example.memoriajatek;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        loadLeaderboardData();

    }

    private void loadLeaderboardData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("global_scores");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String country = snapshot.getKey();
                    Double time = snapshot.getValue(Double.class);

                    if (country != null && time != null) {
                        displayLeaderboardGlobal(country, time);
                    }
                }

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("user_scores").child(currentUser.getUid());

                    userReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Map<String, Double> userTimes = new HashMap<>();
                            for (DataSnapshot countrySnapshot : dataSnapshot.getChildren()) {
                                String country = countrySnapshot.getKey();
                                Double userTime = countrySnapshot.getValue(Double.class);

                                if (country != null && userTime != null) {
                                    userTimes.put(country, userTime);
                                }
                            }
                            displayUserTime(userTimes);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void displayUserTime(Map<String, Double> userTimes) {
        for (Map.Entry<String, Double> entry : userTimes.entrySet()) {
            String country = entry.getKey();
            Double time = entry.getValue();
            int wholeTime = (int) Math.round(time);

            int textViewResourceId;
            TextView textView;

            switch (country) {
                case "Belgium":
                    textViewResourceId = getResources().getIdentifier("SB", "id", getPackageName());
                    textView = findViewById(textViewResourceId);
                    textView.setText(String.valueOf(wholeTime + " s"));
                    break;
                case "Italy":
                    textViewResourceId = getResources().getIdentifier("SO", "id", getPackageName());
                    textView = findViewById(textViewResourceId);
                    textView.setText(String.valueOf(wholeTime + " s"));
                    break;
                case "Hungary":
                    textViewResourceId = getResources().getIdentifier("SM", "id", getPackageName());
                    textView = findViewById(textViewResourceId);
                    textView.setText(String.valueOf(wholeTime + " s"));
                    break;
                case "Greece":
                    textViewResourceId = getResources().getIdentifier("SG", "id", getPackageName());
                    textView = findViewById(textViewResourceId);
                    textView.setText(String.valueOf(wholeTime + " s"));
                    break;
                case "Spain":
                    textViewResourceId = getResources().getIdentifier("SS", "id", getPackageName());
                    textView = findViewById(textViewResourceId);
                    textView.setText(String.valueOf(wholeTime + " s"));
                    break;
                case "France":
                    textViewResourceId = getResources().getIdentifier("SF", "id", getPackageName());
                    textView = findViewById(textViewResourceId);
                    textView.setText(String.valueOf(wholeTime + " s"));
                    break;
                default:
                    break;
            }
        }
    }



    private void displayLeaderboardGlobal(String country, double time) {
        int wholeTime = (int) time;

        int textViewResourceId;
        TextView textView;

        switch (country) {
            case "Belgium":
                textViewResourceId = getResources().getIdentifier("GB", "id", getPackageName());
                textView = findViewById(textViewResourceId);
                textView.setText(String.valueOf(wholeTime + " s"));
                break;
            case "Italy":
                textViewResourceId = getResources().getIdentifier("GO", "id", getPackageName());
                textView = findViewById(textViewResourceId);
                textView.setText(String.valueOf(wholeTime + " s"));
                break;
            case "Hungary":
                textViewResourceId = getResources().getIdentifier("GM", "id", getPackageName());
                textView = findViewById(textViewResourceId);
                textView.setText(String.valueOf(wholeTime + " s"));
                break;
            case "Greece":
                textViewResourceId = getResources().getIdentifier("GG", "id", getPackageName());
                textView = findViewById(textViewResourceId);
                textView.setText(String.valueOf(wholeTime + " s"));
                break;
            case "Spain":
                textViewResourceId = getResources().getIdentifier("GS", "id", getPackageName());
                textView = findViewById(textViewResourceId);
                textView.setText(String.valueOf(wholeTime + " s"));
                break;
            case "France":
                textViewResourceId = getResources().getIdentifier("GF", "id", getPackageName());
                textView = findViewById(textViewResourceId);
                textView.setText(String.valueOf(wholeTime + " s"));
                break;
            default:
                break;
        }
    }



    public void backToMenu(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
