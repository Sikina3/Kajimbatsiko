package com.ansimue.kajimbatsiko;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NewPasswordActivity extends AppCompatActivity {

    private EditText newPasswordField, confirmNewPasswordField;
    private Button btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        newPasswordField = findViewById(R.id.new_password);
        confirmNewPasswordField = findViewById(R.id.confirm_new_password);
        btnChangePassword = findViewById(R.id.btn_change_password);

        btnChangePassword.setOnClickListener(v -> {
            String newPass = newPasswordField.getText().toString().trim();
            String confirmPass = confirmNewPasswordField.getText().toString().trim();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                return;
            }

            // En réalité, avec Firebase, le changement de mot de passe se fait via le lien reçu par email.
            // On simule ici la réussite pour le design.
            Toast.makeText(this, "Mot de passe changé avec succès !", Toast.LENGTH_LONG).show();
            
            Intent intent = new Intent(NewPasswordActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
