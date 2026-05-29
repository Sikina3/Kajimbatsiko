package com.ansimue.kajimbatsiko;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ansimue.kajimbatsiko.data.database;
import com.ansimue.kajimbatsiko.data.rooms.DataCategory;
import com.ansimue.kajimbatsiko.data.rooms.DataCategorySaving;
import com.ansimue.kajimbatsiko.data.rooms.DataExpenses;
import com.ansimue.kajimbatsiko.data.rooms.DataIncome;
import com.ansimue.kajimbatsiko.data.rooms.DataSaving;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button btnLogin, btnSignUpNav;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EdgeToEdge.enable(this);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btn_login);
        btnSignUpNav = findViewById(R.id.btn_sign_up_nav);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> loginUser());
        btnSignUpNav.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            syncAllDataFromCloud(user.getUid());
                        } else {
                            showLoading(false);
                            Toast.makeText(LoginActivity.this, "Veuillez vérifier votre email.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, "Erreur : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void syncAllDataFromCloud(String userId) {
        // ORDRE CRITIQUE : 1. Catégories -> 2. Transactions -> 3. Épargnes
        downloadCategories(userId);
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

                                // Sécurité supplémentaire anti-crash
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
    }
}
