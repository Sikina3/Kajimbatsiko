package com.teste.kajimbatsiko;

import android.content.Intent;
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

    private TextView text, next;
    private ImageView image;
    private LinearLayout dotLayout;

    private int[] images = { R.drawable.receive, R.drawable.send};
    private String[] texts = {
            "Tongasoa eto amin'ny Kajimbatsiko",
            "Ampiasao ny ranjambaiko mba hitantanana ny volanao"
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
            if (currentPage < images.length){
                showPage(currentPage);
            } else {
                startActivity(new Intent(this, home.class));
                finish();
            }
        });

    }

    public  void initialisationLogic(){
        text = findViewById(R.id.text);
        next = findViewById(R.id.next);
        image = findViewById(R.id.image);
        dotLayout = findViewById(R.id.dotLayout);
    }

    private void showPage(int page) {
        text.setText(texts[page]);
        image.setImageResource(images[page]);
        updateDots(page);
    }

    private void updateDots(int page){
        dotLayout.removeAllViews();

        for(int i = 0; i< images.length; i++){
            TextView dot = new TextView(this);
            dot.setText("●");
            dot.setTextSize(18);
            dot.setTextColor(i == page ? Color.GREEN : Color.WHITE);
            dotLayout.addView(dot);
        }
    }
}