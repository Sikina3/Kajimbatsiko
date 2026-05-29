package com.ansimue.kajimbatsiko;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailField;
    private Button btnNext, btnSignUpNav;
    private TextView btnRegisterNav;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.email);
        btnNext = findViewById(R.id.btn_next);
        btnSignUpNav = findViewById(R.id.btn_sign_up_nav);
        btnRegisterNav = findViewById(R.id.btn_register_nav);

        btnNext.setOnClickListener(v -> resetPassword());

        btnSignUpNav.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, RegisterActivity.class));
            finish();
        });

        btnRegisterNav.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private void resetPassword() {
        String email = emailField.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer votre email", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Email de réinitialisation envoyé !", Toast.LENGTH_LONG).show();
                        // Rediriger vers l'écran du code PIN ou directement vers le login si on utilise le lien Firebase standard
                        // L'utilisateur demande un "Security Pin", mais Firebase envoie un lien.
                        // Je vais simuler la navigation vers le PIN si besoin, ou juste retourner au login.
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Erreur : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
