package com.ansimue.kajimbatsiko;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameField, emailField, passwordField, confirmPasswordField;
    private CheckBox checkboxTerms;
    private Button btnRegister;
    private TextView btnLoginNav, tvErrorPassword, tvErrorTerms;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EdgeToEdge.enable(this);

        mAuth = FirebaseAuth.getInstance();

        nameField = findViewById(R.id.full_name);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.confirm_password);
        checkboxTerms = findViewById(R.id.checkbox_terms);
        btnRegister = findViewById(R.id.btn_register);
        btnLoginNav = findViewById(R.id.btn_login_nav);
        progressBar = findViewById(R.id.progressBar);
        tvErrorPassword = findViewById(R.id.tvErrorPassword);
        tvErrorTerms = findViewById(R.id.tvErrorTerms);

        btnRegister.setOnClickListener(v -> registerUser());
        btnLoginNav.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        // Afficher l'indice sur la force du mdp en temps réel
        passwordField.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String pwd = passwordField.getText().toString();
                if (!pwd.isEmpty() && pwd.length() < 8) {
                    tvErrorPassword.setText("⚠ Le mot de passe doit contenir au moins 8 caractères.");
                    tvErrorPassword.setVisibility(View.VISIBLE);
                } else {
                    tvErrorPassword.setVisibility(View.GONE);
                }
            }
        });
    }

    private void registerUser() {
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        // Reset errors
        tvErrorPassword.setVisibility(View.GONE);
        tvErrorTerms.setVisibility(View.GONE);

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            tvErrorPassword.setText("⚠ Le mot de passe doit contenir au moins 8 caractères.");
            tvErrorPassword.setVisibility(View.VISIBLE);
            passwordField.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            tvErrorPassword.setText("Les mots de passe ne correspondent pas.");
            tvErrorPassword.setVisibility(View.VISIBLE);
            confirmPasswordField.requestFocus();
            return;
        }

        if (!checkboxTerms.isChecked()) {
            tvErrorTerms.setVisibility(View.VISIBLE);
            return;
        }

        showLoading(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates);

                            user.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        showLoading(false);
                                        if (emailTask.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "Compte créé ! Vérifiez votre email.", Toast.LENGTH_LONG).show();
                                            mAuth.signOut();
                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Erreur d'envoi d'email.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        showLoading(false);
                        Toast.makeText(RegisterActivity.this, "Erreur : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
    }
}
