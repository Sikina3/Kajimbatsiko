package com.ansimue.kajimbatsiko;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ansimue.kajimbatsiko.utils.ThemeManager;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmPassword;
    private TextView tvPasswordError;
    private Button btnChangePassword, btnDeleteAccount;
    private FirebaseAuth mAuth;

    private MaterialCardView cardThemeLight, cardThemeDark, cardThemeSystem;
    private ImageView checkThemeLight, checkThemeDark, checkThemeSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        cardThemeLight = findViewById(R.id.cardThemeLight);
        cardThemeDark = findViewById(R.id.cardThemeDark);
        cardThemeSystem = findViewById(R.id.cardThemeSystem);
        checkThemeLight = findViewById(R.id.checkThemeLight);
        checkThemeDark = findViewById(R.id.checkThemeDark);
        checkThemeSystem = findViewById(R.id.checkThemeSystem);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnChangePassword.setOnClickListener(v -> changePassword());
        btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());

        setupThemeSelection();
    }

    private void setupThemeSelection() {
        updateThemeCheckmarks(ThemeManager.getSavedThemeMode(this));

        cardThemeLight.setOnClickListener(v -> selectTheme(ThemeManager.THEME_LIGHT));
        cardThemeDark.setOnClickListener(v -> selectTheme(ThemeManager.THEME_DARK));
        cardThemeSystem.setOnClickListener(v -> selectTheme(ThemeManager.THEME_SYSTEM));
    }

    private void selectTheme(int themeMode) {
        ThemeManager.saveThemeMode(this, themeMode);
        updateThemeCheckmarks(themeMode);
        recreate();
    }

    private void updateThemeCheckmarks(int themeMode) {
        checkThemeLight.setVisibility(themeMode == ThemeManager.THEME_LIGHT ? View.VISIBLE : View.GONE);
        checkThemeDark.setVisibility(themeMode == ThemeManager.THEME_DARK ? View.VISIBLE : View.GONE);
        checkThemeSystem.setVisibility(themeMode == ThemeManager.THEME_SYSTEM ? View.VISIBLE : View.GONE);

        int selectedStroke = 3;
        int defaultStroke = 1;
        cardThemeLight.setStrokeWidth(themeMode == ThemeManager.THEME_LIGHT ? selectedStroke : defaultStroke);
        cardThemeDark.setStrokeWidth(themeMode == ThemeManager.THEME_DARK ? selectedStroke : defaultStroke);
        cardThemeSystem.setStrokeWidth(themeMode == ThemeManager.THEME_SYSTEM ? selectedStroke : defaultStroke);
    }

    private void changePassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        tvPasswordError.setVisibility(View.GONE);

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            tvPasswordError.setText("⚠ Veuillez remplir tous les champs.");
            tvPasswordError.setVisibility(View.VISIBLE);
            return;
        }

        if (newPassword.length() < 8) {
            tvPasswordError.setText("⚠ Le mot de passe doit contenir au moins 8 caractères.");
            tvPasswordError.setVisibility(View.VISIBLE);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            tvPasswordError.setText("Les mots de passe ne correspondent pas.");
            tvPasswordError.setVisibility(View.VISIBLE);
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            btnChangePassword.setEnabled(false);
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        btnChangePassword.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Mot de passe mis à jour avec succès.", Toast.LENGTH_LONG).show();
                            etNewPassword.setText("");
                            etConfirmPassword.setText("");
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "";
                            if (error != null && error.contains("recent authentication")) {
                                tvPasswordError.setText("Opération sensible. Veuillez vous déconnecter et vous reconnecter avant de réessayer.");
                            } else {
                                tvPasswordError.setText("Erreur : " + error);
                            }
                            tvPasswordError.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le compte")
                .setMessage("Êtes-vous sûr de vouloir supprimer définitivement votre compte ? Cette action est irréversible et effacera toutes vos données sur le serveur.")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteAccount())
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            btnDeleteAccount.setEnabled(false);
            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Compte supprimé.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            btnDeleteAccount.setEnabled(true);
                            String error = task.getException() != null ? task.getException().getMessage() : "";
                            if (error != null && error.contains("recent authentication")) {
                                Toast.makeText(SettingsActivity.this, "Veuillez vous déconnecter et vous reconnecter pour supprimer votre compte.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(SettingsActivity.this, "Erreur : " + error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }
}
