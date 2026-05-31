package com.ansimue.kajimbatsiko;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.ansimue.kajimbatsiko.data.database;
import com.ansimue.kajimbatsiko.data.rooms.DataCategory;
import com.ansimue.kajimbatsiko.data.rooms.DataCategorySaving;
import com.ansimue.kajimbatsiko.data.rooms.DataExpenses;
import com.ansimue.kajimbatsiko.data.rooms.DataIncome;
import com.ansimue.kajimbatsiko.data.rooms.DataSaving;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private TextView forgot_password, tvErrorEmail, tvErrorPassword;
    private Button btnLogin, btnSignUpNav;
    private LinearLayout btnGoogle;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EdgeToEdge.enable(this);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        credentialManager = CredentialManager.create(this);

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btn_login);
        btnSignUpNav = findViewById(R.id.btn_sign_up_nav);
        progressBar = findViewById(R.id.progressBar);
        forgot_password = findViewById(R.id.forgot_password);
        btnGoogle = findViewById(R.id.btn_google);
        tvErrorEmail = findViewById(R.id.tvErrorEmail);
        tvErrorPassword = findViewById(R.id.tvErrorPassword);

        btnLogin.setOnClickListener(v -> loginUser());
        btnSignUpNav.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        forgot_password.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
        
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    private void clearErrors() {
        tvErrorEmail.setVisibility(View.GONE);
        tvErrorPassword.setVisibility(View.GONE);
    }

    private void showEmailError(String msg) {
        tvErrorEmail.setText(msg);
        tvErrorEmail.setVisibility(View.VISIBLE);
        emailField.requestFocus();
    }

    private void showPasswordError(String msg) {
        tvErrorPassword.setText(msg);
        tvErrorPassword.setVisibility(View.VISIBLE);
        passwordField.requestFocus();
    }

    private void loginUser() {
        clearErrors();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        boolean hasError = false;
        if (email.isEmpty()) {
            showEmailError("⚠ Veuillez entrer votre adresse email.");
            hasError = true;
        }
        if (password.isEmpty()) {
            showPasswordError("⚠ Veuillez entrer votre mot de passe.");
            hasError = true;
        }
        if (hasError) return;

        showLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            showLoading(true);
                            syncAllDataFromCloud(user.getUid());
                        } else {
                            showEmailError("📧 Email non vérifié. Vérifiez votre boîte mail (et les spams).");
                            mAuth.signOut();
                        }
                    } else {
                        String errorCode = "";
                        if (task.getException() != null) {
                            errorCode = task.getException().getMessage() != null
                                    ? task.getException().getMessage().toLowerCase() : "";
                        }
                        if (errorCode.contains("password") || errorCode.contains("wrong") || errorCode.contains("invalid-credential")) {
                            showPasswordError("❌ Mot de passe incorrect.");
                        } else if (errorCode.contains("user") || errorCode.contains("email") || errorCode.contains("no user") || errorCode.contains("not found")) {
                            showEmailError("❌ Aucun compte trouvé avec cet email.");
                        } else if (errorCode.contains("format") || errorCode.contains("badly")) {
                            showEmailError("⚠ Format d'email invalide.");
                        } else {
                            showEmailError("❌ Erreur de connexion. Vérifiez vos informations.");
                        }
                    }
                });
    }

    private void signInWithGoogle() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.google_client_id))
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        showLoading(true);
        
        credentialManager.getCredentialAsync(this, request, null, Runnable::run, new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
            @Override
            public void onResult(GetCredentialResponse result) {
                handleSignIn(result);
            }

            @Override
            public void onError(GetCredentialException e) {
                showLoading(false);
                Log.e("GoogleSignIn", "Error: " + e.getMessage());
                Toast.makeText(LoginActivity.this, "Échec de la connexion Google", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSignIn(GetCredentialResponse response) {
        if (response.getCredential() instanceof CustomCredential) {
            CustomCredential customCredential = (CustomCredential) response.getCredential();
            if (customCredential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                try {
                    GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(customCredential.getData());
                    String idToken = googleIdTokenCredential.getIdToken();
                    firebaseAuthWithGoogle(idToken);
                } catch (Exception e) {
                    showLoading(false);
                    Log.e("GoogleSignIn", "Exception: " + e.getMessage());
                }
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            syncAllDataFromCloud(user.getUid());
                        }
                    } else {
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, "Erreur Firebase Google: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void syncAllDataFromCloud(String userId) {
        // Pour éviter les doublons (Revenu + Revenu), on vide la base locale avant de synchroniser
        new Thread(() -> {
            database db = database.getDatabase(this);
            try {
                db.incomeDao().deleteAllIncomes();
                db.expenseDao().deleteAllExpenses();
                db.savingDao().deleteAllSavings();
                db.categoryDao().deleteAllCategories();
                db.category_savingDao().deleteAllCategorySaving();
                
                // Une fois la base vidée, on lance le téléchargement
                runOnUiThread(() -> downloadCategories(userId));
            } catch (Exception e) {
                Log.e("SyncError", "Erreur lors du nettoyage : " + e.getMessage());
                runOnUiThread(() -> downloadCategories(userId));
            }
        }).start();
    }

    private void downloadCategories(String userId) {
        firestore.collection("users").document(userId).collection("categories").get()
                .addOnSuccessListener(docs -> {
                    new Thread(() -> {
                        try {
                            database db = database.getDatabase(this);
                            for (QueryDocumentSnapshot doc : docs) {
                                DataCategory cat = new DataCategory();
                                Number numId = (Number) doc.get("id");
                                cat.uid = numId != null ? numId.intValue() : 0;
                                cat.nom = doc.getString("nom");
                                Number numIcon = (Number) doc.get("icon");
                                cat.icon = numIcon != null ? numIcon.intValue() : 0;
                                db.categoryDao().insertCategory(cat);
                            }
                        } catch (Exception e) {
                            Log.e("SyncError", "Categories : " + e.getMessage());
                        } finally {
                            downloadSavingCategories(userId);
                        }
                    }).start();
                })
                .addOnFailureListener(e -> {
                    Log.e("SyncError", "Categories failed : " + e.getMessage());
                    downloadSavingCategories(userId);
                });
    }

    private void downloadSavingCategories(String userId) {
        firestore.collection("users").document(userId).collection("saving_categories").get()
                .addOnSuccessListener(docs -> {
                    new Thread(() -> {
                        try {
                            database db = database.getDatabase(this);
                            for (QueryDocumentSnapshot doc : docs) {
                                DataCategorySaving scat = new DataCategorySaving();
                                Number numId = (Number) doc.get("id");
                                scat.id = numId != null ? numId.intValue() : 0;
                                scat.nom = doc.getString("nom");
                                Number numDevis = (Number) doc.get("devis");
                                scat.devis = numDevis != null ? numDevis.doubleValue() : 0.0;
                                Number numIcon = (Number) doc.get("icon");
                                scat.icon = numIcon != null ? numIcon.intValue() : 0;
                                db.category_savingDao().insertCategorySaving(scat);
                            }
                        } catch (Exception e) {
                            Log.e("SyncError", "Saving categories : " + e.getMessage());
                        } finally {
                            downloadIncomes(userId);
                        }
                    }).start();
                })
                .addOnFailureListener(e -> {
                    Log.e("SyncError", "Saving categories failed : " + e.getMessage());
                    downloadIncomes(userId);
                });
    }

    private void downloadIncomes(String userId) {
        firestore.collection("users").document(userId).collection("incomes").get()
                .addOnSuccessListener(docs -> {
                    new Thread(() -> {
                        try {
                            database db = database.getDatabase(this);
                            for (QueryDocumentSnapshot doc : docs) {
                                DataIncome inc = new DataIncome();
                                inc.titre_revenue = doc.getString("titre");
                                inc.type = doc.getString("type");
                                Number numMontant = (Number) doc.get("montant");
                                inc.montant = numMontant != null ? numMontant.doubleValue() : 0.0;
                                inc.date = doc.getString("date");
                                inc.message = doc.getString("note");
                                inc.userId = userId;
                                db.incomeDao().insertIncome(inc);
                            }
                        } catch (Exception e) {
                            Log.e("SyncError", "Incomes : " + e.getMessage());
                        } finally {
                            downloadExpenses(userId);
                        }
                    }).start();
                })
                .addOnFailureListener(e -> {
                    Log.e("SyncError", "Incomes failed : " + e.getMessage());
                    downloadExpenses(userId);
                });
    }

    private void downloadExpenses(String userId) {
        firestore.collection("users").document(userId).collection("expenses").get()
                .addOnSuccessListener(docs -> {
                    new Thread(() -> {
                        try {
                            database db = database.getDatabase(this);
                            for (QueryDocumentSnapshot doc : docs) {
                                DataExpenses exp = new DataExpenses();
                                exp.titre_depense = doc.getString("titre");
                                Number numMontant = (Number) doc.get("montant");
                                exp.montant = numMontant != null ? numMontant.doubleValue() : 0.0;
                                exp.date = doc.getString("date");
                                Number numCatId = (Number) doc.get("categoryId");
                                exp.categoryId = numCatId != null ? numCatId.intValue() : 0;
                                exp.message = doc.getString("note");
                                exp.userId = userId;

                                if (db.categoryDao().getCategoryName(exp.categoryId) == null) {
                                    DataCategory dummy = new DataCategory();
                                    dummy.uid = exp.categoryId;
                                    dummy.nom = doc.getString("categoryName");
                                    if (dummy.nom == null) dummy.nom = "Catégorie restaurée";
                                    dummy.icon = R.drawable.food; 
                                    db.categoryDao().insertCategory(dummy);
                                }
                                db.expenseDao().insertExpense(exp);
                            }
                        } catch (Exception e) {
                            Log.e("SyncError", "Expenses : " + e.getMessage());
                        } finally {
                            downloadSavings(userId);
                        }
                    }).start();
                })
                .addOnFailureListener(e -> {
                    Log.e("SyncError", "Expenses failed : " + e.getMessage());
                    downloadSavings(userId);
                });
    }

    private void downloadSavings(String userId) {
        firestore.collection("users").document(userId).collection("savings").get()
                .addOnSuccessListener(docs -> {
                    new Thread(() -> {
                        try {
                            database db = database.getDatabase(this);
                            for (QueryDocumentSnapshot doc : docs) {
                                DataSaving sav = new DataSaving();
                                sav.titre = doc.getString("titre");
                                Number numMontant = (Number) doc.get("montant");
                                sav.montant = numMontant != null ? numMontant.doubleValue() : 0.0;
                                sav.date = doc.getString("date");
                                Number numCatId = (Number) doc.get("categoryId");
                                sav.categoryId = numCatId != null ? numCatId.intValue() : 0;
                                sav.message = doc.getString("note");
                                sav.userId = userId;
                                db.savingDao().insertSaving(sav);
                            }
                        } catch (Exception e) {
                            Log.e("SyncError", "Savings : " + e.getMessage());
                        } finally {
                            navigateToHome();
                        }
                    }).start();
                })
                .addOnFailureListener(e -> {
                    Log.e("SyncError", "Savings failed : " + e.getMessage());
                    navigateToHome();
                });
    }

    private void navigateToHome() {
        runOnUiThread(() -> {
            showLoading(false);
            startActivity(new Intent(this, home.class));
            finish();
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        if (btnGoogle != null) btnGoogle.setEnabled(!isLoading);
    }
}
