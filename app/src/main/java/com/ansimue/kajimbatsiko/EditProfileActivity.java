package com.ansimue.kajimbatsiko;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail;
    private TextView tvInitials, tvError;
    private Button btnSave;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        tvInitials = findViewById(R.id.tvInitials);
        tvError = findViewById(R.id.tvError);
        btnSave = findViewById(R.id.btnSave);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadUserProfile();

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();

            if (name != null) etName.setText(name);
            if (email != null) etEmail.setText(email);
            tvInitials.setText(getInitials(name != null && !name.isEmpty() ? name : email));
        }
    }

    private void saveProfile() {
        String newName = etName.getText().toString().trim();

        if (newName.isEmpty()) {
            tvError.setText("⚠ Le nom ne peut pas être vide.");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        tvError.setVisibility(View.GONE);
        btnSave.setEnabled(false);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        btnSave.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Profil mis à jour", Toast.LENGTH_SHORT).show();
                            tvInitials.setText(getInitials(newName));
                        } else {
                            tvError.setText("❌ Erreur lors de la mise à jour.");
                            tvError.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < Math.min(parts.length, 2); i++) {
            if (!parts[i].isEmpty()) {
                initials.append(parts[i].charAt(0));
            }
        }
        return initials.toString().toUpperCase();
    }
}
