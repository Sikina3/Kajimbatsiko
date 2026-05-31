package com.ansimue.kajimbatsiko;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailField;
    private Button btnNext;
    private TextView btnRegisterNav, tvErrorEmail, tvConfirmEmail;
    private ConstraintLayout forgotCard;
    private LinearLayout confirmationBlock;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        EdgeToEdge.enable(this);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.email);
        btnNext = findViewById(R.id.btn_next);
        btnRegisterNav = findViewById(R.id.btn_register_nav);
        tvErrorEmail = findViewById(R.id.tvErrorEmail);
        tvConfirmEmail = findViewById(R.id.tvConfirmEmail);
        forgotCard = findViewById(R.id.forgot_card);
        confirmationBlock = findViewById(R.id.confirmationBlock);

        btnNext.setOnClickListener(v -> resetPassword());

        btnRegisterNav.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, RegisterActivity.class));
            finish();
        });

        Button btnBackToLogin = findViewById(R.id.btnBackToLogin);
        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void resetPassword() {
        String email = emailField.getText().toString().trim();
        tvErrorEmail.setVisibility(View.GONE);

        if (email.isEmpty()) {
            tvErrorEmail.setText("⚠ Veuillez entrer votre adresse email.");
            tvErrorEmail.setVisibility(View.VISIBLE);
            return;
        }

        btnNext.setEnabled(false);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    btnNext.setEnabled(true);
                    if (task.isSuccessful()) {
                        // Cacher le formulaire, afficher le bloc de confirmation
                        forgotCard.setVisibility(View.GONE);
                        tvConfirmEmail.setText(
                                "Un lien de réinitialisation a été envoyé à :\n\n" + email
                        );
                        confirmationBlock.setVisibility(View.VISIBLE);
                    } else {
                        String error = task.getException() != null
                                ? task.getException().getMessage() : "";
                        if (error != null && (error.toLowerCase().contains("user") || error.toLowerCase().contains("not found"))) {
                            tvErrorEmail.setText("❌ Aucun compte trouvé avec cet email.");
                        } else if (error != null && error.toLowerCase().contains("format")) {
                            tvErrorEmail.setText("⚠ Format d'email invalide.");
                        } else {
                            tvErrorEmail.setText("❌ Une erreur est survenue. Réessayez.");
                        }
                        tvErrorEmail.setVisibility(View.VISIBLE);
                    }
                });
    }
}
