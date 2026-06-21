package com.ansimue.kajimbatsiko;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SecurityPinActivity extends AppCompatActivity {

    private Button btnAccept, btnSendAgain;
    private TextView btnSignUpNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_pin);

        btnAccept = findViewById(R.id.btn_accept);
        btnSendAgain = findViewById(R.id.btn_send_again);
        btnSignUpNav = findViewById(R.id.btn_sign_up_nav);

        btnAccept.setOnClickListener(v -> {
            startActivity(new Intent(SecurityPinActivity.this, NewPasswordActivity.class));
        });

        btnSendAgain.setOnClickListener(v -> {
            Toast.makeText(this, "Pin sent again!", Toast.LENGTH_SHORT).show();
        });

        btnSignUpNav.setOnClickListener(v -> {
            startActivity(new Intent(SecurityPinActivity.this, RegisterActivity.class));
            finish();
        });
    }
}
