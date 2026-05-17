package com.ansimue.kajimbatsiko;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class onBoarding1 extends AppCompatActivity {

    private static final String PREFS_NAME = "KajimbatsikoPrefs";
    private static final String KEY_FIRST_LAUNCH = "isFirstLaunch";

    private TextView text, next, skip, title;
    private ImageView image;
    private LinearLayout dotLayout;

    private int[] images = { R.drawable.receive, R.drawable.send};
    private String[] titles = {
            "Bienvenue !",
            "Suivez vos finances"
    };
    private String[] texts = {
            "Kajimbatsiko vous aide à gérer facilement vos finances personnelles",
            "Enregistrez vos revenus et vos dépenses en quelques clics",
    };
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_boarding1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initialisationLogic();
        showPage(currentPage);

        next.setOnClickListener(v -> {
            currentPage++;
            if (currentPage < images.length) {
                showPage(currentPage);
            } else {
                finishOnboarding();
            }
        });

    }

    public void initialisationLogic() {
        text = findViewById(R.id.text);
        title = findViewById(R.id.title);
        next = findViewById(R.id.next);
        skip = findViewById(R.id.skip);
        image = findViewById(R.id.image);
        dotLayout = findViewById(R.id.dotLayout);

        // Bouton Skip
        skip.setOnClickListener(v -> {
            finishOnboarding();
        });
    }

    private void showPage(int page) {
        title.setText(titles[page]);
        text.setText(texts[page]);
        image.setImageResource(images[page]);
        updateDots(page);

        // Cacher le bouton Skip sur la dernière page
        if (page == images.length - 1) {
            next.setText("Commencer");
            skip.setVisibility(android.view.View.GONE);
        } else {
            next.setText("Suivant");
            skip.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void updateDots(int page) {
        dotLayout.removeAllViews();

        for (int i = 0; i < images.length; i++) {
            TextView dot = new TextView(this);
            dot.setText("●");
            dot.setTextSize(18);
            dot.setTextColor(i == page ? Color.GREEN : Color.WHITE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dotLayout.addView(dot);
        }
    }

    private void finishOnboarding() {
        // Marquer que l'onboarding a été vu
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_FIRST_LAUNCH, false);
        editor.apply();

        // Aller à l'écran d'accueil
        startActivity(new Intent(this, home.class));
        finish();
    }
}