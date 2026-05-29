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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

        // Initialiser la base de données Room en arrière-plan
        new Thread(() -> database.getDatabase(this)).start();

        // Vérifier si c'est le premier lancement
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true);

        // Vérifier l'état de connexion Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        new Handler().postDelayed(() -> {
            Intent intent;
            if (isFirstLaunch) {
                // 1. Premier lancement : Onboarding
                intent = new Intent(MainActivity.this, onBoarding1.class);
            } else if (currentUser == null || !currentUser.isEmailVerified()) {
                // 2. Pas connecté ou Email non vérifié : Login
                intent = new Intent(MainActivity.this, LoginActivity.class);
            } else {
                // 3. Tout est OK : Accueil
                intent = new Intent(MainActivity.this, home.class);
            }
            startActivity(intent);
            finish();
        }, time_out);
    }
}
