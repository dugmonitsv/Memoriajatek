package com.example.memoriajatek;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class GameActivity extends AppCompatActivity {
    private ImageView[] imageViews;
    TextView textView,timerText;
    boolean settingUp = false;
    Timer timer;
    TimerTask timerTask;
    double time = 0.0;
    private int[][] cardArray;
    private int[] images;
    private int flippedCardsCount;
    private int firstClickedIndex = -1;
    private int secondClickedIndex = -1;
    KonfettiView konfettiView;
    Shape.DrawableShape drawableShape;
    private boolean allCardsFaceDown = false;
    private boolean gamePaused = false;
    private String selectedCountry;
    boolean gameOver,agreedToPauseGame;
    private boolean shuffleDialogShown = false;
    private SensorEventListener sensorEventListener;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        timer = new Timer();
        timerText = findViewById(R.id.timer);
        textView = findViewById(R.id.shake_text);

        konfettiView = findViewById(R.id.konfettiView);
        drawableShape = new Shape.DrawableShape(AppCompatResources.getDrawable(this, R.drawable.ic_android), true);

        GridLayout gridLayout = findViewById(R.id.gridLayout);
        if (gridLayout == null) {
            Log.e("GameActivity", "GridLayout not found in activity_game.xml");
            return;
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("selectedCountry")) {
            selectedCountry = intent.getStringExtra("selectedCountry");
        } else {
            Log.e("GameActivity", "No selected country found in intent");
            return;
        }
        
        initializeGame();
        registerShakeSensorListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterSensorListener();
    }


    private void unregisterSensorListener() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensorShake = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.unregisterListener(sensorEventListener, sensorShake);
    }



    private void registerShakeSensorListener() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensorShake = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent != null) {
                    float x_accl = sensorEvent.values[0];
                    float y_accl = sensorEvent.values[1];
                    float z_accl = sensorEvent.values[2];

                    float floatSum = Math.abs(x_accl) + Math.abs(y_accl) + Math.abs(z_accl);

                    if (floatSum > 20) {
                        showShuffleConfirmationDialog();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        sensorManager.registerListener(sensorEventListener, sensorShake, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void showShuffleConfirmationDialog() {
        if (!shuffleDialogShown && !allCardsFaceDown && !gamePaused) {
            if (timer != null) {
                timer.cancel();
            }
            gamePaused = true;
            shuffleDialogShown = true;

            Button igen, megsem;

            View alertCustomDialoge = LayoutInflater.from(GameActivity.this).inflate(R.layout.custom_dialoge_reshuffle, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
            builder.setView(alertCustomDialoge);

            igen = (Button) alertCustomDialoge.findViewById(R.id.igenButton);
            megsem = (Button) alertCustomDialoge.findViewById(R.id.megsemButton);

            final AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(false);
            dialog.show();

            igen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shuffleCards();
                    dialog.dismiss();
                    shuffleDialogShown = false;
                    gamePaused = false;
                }

            });

            megsem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    shuffleDialogShown = false;
                    if (timer != null) {
                        startTimer();
                    }
                    gamePaused = false;
                }

            });
        }
    }



    private void shuffleCards() {
        time = 0.0;
        timerText.setText(getTimerText());
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();

        Random random = new Random();

        for (int i = images.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);

            int temp = images[index];
            images[index] = images[i];
            images[i] = temp;

            boolean tempTag = (boolean) imageViews[index].getTag();
            imageViews[index].setTag(imageViews[i].getTag());
            imageViews[i].setTag(tempTag);
        }

        flippedCardsCount = 0;
        showAllCardsForAWhile();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startTimer();
            }
        }, 5500);
    }


    private void initializeGame() {
        settingUp =  true;
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("selectedCountry")) {
            String selectedCountry = intent.getStringExtra("selectedCountry");
            int[] countryImages = getCountryImagesBySelectedCountry(selectedCountry);
            createMemoryBoard(countryImages);

        }
        shuffleCards();
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        timer = new Timer();

        timerTask = new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        time++;
                        timerText.setText(getTimerText());
                    }
                });
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }


    private String getTimerText() {
        int rounded = (int) Math.round(time);

        int sec = ((rounded % 86400) % 3600) % 60;
        int min = ((rounded % 86400) % 3600) / 60;
        return String.format("%02d", min) + " : " + String.format("%02d", sec);
    }

    private void showAllCardsForAWhile() {
        agreedToPauseGame =false;
        allCardsFaceDown = true;
        for (ImageView imageView : imageViews) {
            imageView.setImageResource(images[getIndex(imageView)]);
            imageView.setTag(true);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (ImageView imageView : imageViews) {
                    imageView.setImageResource(R.drawable.back);
                    imageView.setTag(false);
                }
                allCardsFaceDown = false;
                agreedToPauseGame =true;
            }
        }, 5000);

    }


    private int[] getCountryImagesBySelectedCountry(String country) {
        switch (country) {
            case "Hungary":
                return new int[]{R.drawable.h1, R.drawable.h2, R.drawable.h3, R.drawable.h4, R.drawable.h5, R.drawable.h6, R.drawable.h7, R.drawable.h8};
            case "Belgium":
                return new int[]{R.drawable.b1, R.drawable.b2, R.drawable.b3, R.drawable.b4, R.drawable.b5, R.drawable.b6, R.drawable.b7, R.drawable.b8};
            case "Greece":
                return new int[]{R.drawable.g1, R.drawable.g2, R.drawable.g3, R.drawable.g4, R.drawable.g5, R.drawable.g6, R.drawable.g7, R.drawable.g8, R.drawable.g9, R.drawable.g10};
            case "France":
                return new int[]{R.drawable.f1, R.drawable.f2, R.drawable.f3, R.drawable.f4, R.drawable.f5, R.drawable.f6, R.drawable.f7, R.drawable.f8, R.drawable.f9, R.drawable.f10};
            case "Spain":
                return new int[]{R.drawable.s1, R.drawable.s2, R.drawable.s3, R.drawable.s4, R.drawable.s5, R.drawable.s6, R.drawable.s7, R.drawable.s8, R.drawable.s9, R.drawable.s10, R.drawable.s11, R.drawable.s12};
            case "Italy":
                return new int[]{R.drawable.i1, R.drawable.i2, R.drawable.i3, R.drawable.i4, R.drawable.i5, R.drawable.i6, R.drawable.i7, R.drawable.i8, R.drawable.i9, R.drawable.i10, R.drawable.i11, R.drawable.i12};
            default:
                return new int[]{};
        }
    }

    private void createMemoryBoard(int[] countryImages) {

        int[] doubledImages = new int[countryImages.length * 2];

        for (int i = 0; i < countryImages.length; i++) {
            doubledImages[i] = countryImages[i];
            doubledImages[i + countryImages.length] = countryImages[i];
        }

        images = doubledImages;

        GridLayout gridLayout = findViewById(R.id.gridLayout);
        imageViews = new ImageView[countryImages.length * 2];

        cardArray = new int[countryImages.length * 2][2];
        int cardSize = 250;
        int columnCount = 4;

        for (int i = 0; i < countryImages.length * 2; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.back);
            imageView.setTag(false);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.height = cardSize;
            params.width = cardSize;
            params.rowSpec = GridLayout.spec(i / columnCount);
            params.columnSpec = GridLayout.spec(i % columnCount);

            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCardClick(v);
                }
            });

            gridLayout.addView(imageView);
            imageViews[i] = imageView;
        }
    }

    private int getIndex(ImageView imageView) {
        for (int i = 0; i < imageViews.length; i++) {
            if (imageViews[i] == imageView) {
                return i;
            }
        }
        return -1;
    }

    public void onCardClick(View view) {
        if (view instanceof ImageView) {
            ImageView clickedImageView = (ImageView) view;
            int clickedIndex = getIndex(clickedImageView);

           if ((boolean) clickedImageView.getTag() || flippedCardsCount == 2 || flippedCardsCount == 1 && firstClickedIndex == clickedIndex) {
                return;
            }

            clickedImageView.setImageResource(images[clickedIndex]);
            clickedImageView.setTag(true);

            flippedCardsCount++;

            if (flippedCardsCount == 2) {
                secondClickedIndex = clickedIndex;
                if (!isMatchingPair()) {
                    flipBackNonMatchingCards();
                } else {
                    flippedCardsCount = 0;
                    firstClickedIndex = -1;
                    secondClickedIndex = -1;
                    gameOver();
                }
            } else if (flippedCardsCount == 1) {
                firstClickedIndex = clickedIndex;
            }
        }
    }

    private boolean isMatchingPair() {
        if (firstClickedIndex != -1 && secondClickedIndex != -1) {
            return images[firstClickedIndex] == images[secondClickedIndex];
        }
        return false;
    }

    private void flipBackNonMatchingCards() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (firstClickedIndex != -1 && secondClickedIndex != -1) {
                    imageViews[firstClickedIndex].setImageResource(R.drawable.back);
                    imageViews[firstClickedIndex].setTag(false);
                    imageViews[secondClickedIndex].setImageResource(R.drawable.back);
                    imageViews[secondClickedIndex].setTag(false);
                }
                flippedCardsCount = 0;
                firstClickedIndex = -1;
                secondClickedIndex = -1;
            }
        }, 800);

    }


    private void gameOver() {
        boolean allMatched = true;
        for (ImageView imageView : imageViews) {
            if (!(boolean) imageView.getTag()) {
                allMatched = false;
                break;
            }
        }
        if (allMatched) {
            if (timer != null) {
                timer.cancel();
            }
            EmitterConfig emitterConfig = new Emitter(300, TimeUnit.MILLISECONDS).max(300);
            List<Integer> colorList = Arrays.asList(
                    ContextCompat.getColor(this, R.color.darkblue),
                    ContextCompat.getColor(this, R.color.pink),
                    ContextCompat.getColor(this, R.color.purple),
                    ContextCompat.getColor(this, R.color.turquoise),
                    ContextCompat.getColor(this, R.color.lightblue)
            );
            konfettiView.start(
                    new PartyFactory(emitterConfig)
                            .shapes(Shape.Circle.INSTANCE, Shape.Square.INSTANCE, drawableShape)
                            .spread(360)
                            .position(0.5, 0.25, 1,1)
                            .sizes(new Size(14, 50, 25))
                            .timeToLive(2000)
                            .fadeOutEnabled(true)
                            .colors(colorList)
                            .build()
            );
            Toast.makeText(this, "Gratulálok, megtaláltad az összes párt!", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(GameActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 3000);
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                saveBestTimeForUser(time);
                saveBestTimeForCountry(time);
            }
            gameOver = true;
        }
    }

    public void pause(View view) {
        if (agreedToPauseGame) {
            if (timer != null) {
                timer.cancel();
            }
            gamePaused = true;

            Button folytatas;
            View alertCustomDialoge = LayoutInflater.from(GameActivity.this).inflate(R.layout.custom_dialoge_pause, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
            builder.setView(alertCustomDialoge);
            folytatas = (Button) alertCustomDialoge.findViewById(R.id.folytatasButton);
            final AlertDialog dialog = builder.create();

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(false);
            dialog.show();

            folytatas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (timer != null) {
                        startTimer();
                    }
                    gamePaused = false;
                }

            });

        }
    }


    private void saveBestTimeForCountry(double time) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference countryScoresRef = FirebaseDatabase.getInstance().getReference("global_scores").child(selectedCountry);

            countryScoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Double bestTime = dataSnapshot.getValue(Double.class);
                    if (bestTime == null || time < bestTime) {
                        countryScoresRef.setValue(time).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Ország legjobb idő sikeresen mentve.");
                                } else {
                                    Log.e(TAG, "Hiba történt az ország legjobb idő mentésekor: " + task.getException().getMessage());
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else {
            Log.e(TAG, "Nincs bejelentkezett felhasználó, ezért nem lett mentve az ország legjobb idő.");
        }
    }

    private void saveBestTimeForUser(double newBestTime) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String username = currentUser.getDisplayName();

            DatabaseReference userScoresRef = FirebaseDatabase.getInstance().getReference("user_scores").child(userId);

            userScoresRef.child(selectedCountry).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Double bestTime = dataSnapshot.getValue(Double.class);
                    if (bestTime == null || newBestTime < bestTime) {
                        userScoresRef.child(selectedCountry).setValue(newBestTime).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Felhasználói adatok sikeresen mentve.");
                                } else {
                                    Log.e(TAG, "Hiba történt a felhasználói adatok mentésekor: " + task.getException().getMessage());
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Log.e(TAG, "Nincs bejelentkezett felhasználó, ezért nem lett mentve az adat.");
        }
    }

    public void backToMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}


