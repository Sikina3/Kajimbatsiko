package com.ansimue.kajimbatsiko;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.ansimue.kajimbatsiko.data.database;

public class MainActivity extends AppCompatActivity {
    private static final int time_out = 3000;
    private static final String PREFS_NAME = "KajimbatsikoPrefs";
    private static final String KEY_FIRST_LAUNCH = "isFirstLaunch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialiser la base de données
        database db = database.getDatabase(this);
        db = Room.databaseBuilder(getApplicationContext(), database.class, "finance.db")
                .allowMainThreadQueries()
                .build();

        // Vérifier si c'est le premier lancement
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true);

        new Handler().postDelayed(() -> {
            Intent intent;
            if (isFirstLaunch) {
                // Premier lancement : afficher l'onboarding
                intent = new Intent(MainActivity.this, onBoarding1.class);
            } else {
                // Pas le premier lancement : aller directement à l'écran d'accueil
                intent = new Intent(MainActivity.this, home.class);
            }
            startActivity(intent);
            finish();
        }, time_out);
    }
}