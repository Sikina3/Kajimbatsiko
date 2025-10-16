package com.teste.kajimbatsiko;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.teste.kajimbatsiko.fragments.AnalyseFragment;
import com.teste.kajimbatsiko.fragments.CategoryFragment;
import com.teste.kajimbatsiko.fragments.HomeFragment;
import com.teste.kajimbatsiko.fragments.SavingFragment;
import com.teste.kajimbatsiko.fragments.TransactionFragment;

public class home extends AppCompatActivity {

    BottomNavigationView bottomNav;
    FrameLayout fragment_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets.consumeSystemWindowInsets();
        });
        initialisation();

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.app_bar_home) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
                return true;
            } else if (id == R.id.app_bar_analyste) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AnalyseFragment())
                        .commit();
                return true;
            } else if (id == R.id.app_bar_trans) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TransactionFragment())
                        .commit();
                return true;
            } else if (id == R.id.app_bar_cat) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CategoryFragment())
                        .commit();
                return true;
            } else if (id == R.id.app_bar_profile) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SavingFragment())
                        .commit();
                return true;
            }

            return false;
        });

    }

    private void initialisation(){
        bottomNav = findViewById(R.id.bottomNav);
        fragment_container = findViewById(R.id.fragment_container);
    }
}