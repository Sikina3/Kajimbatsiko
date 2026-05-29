package com.ansimue.kajimbatsiko.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ansimue.kajimbatsiko.R;
import com.ansimue.kajimbatsiko.data.database;
import com.ansimue.kajimbatsiko.data.rooms.DataCategory;
import com.ansimue.kajimbatsiko.data.rooms.DataExpenses;
import com.ansimue.kajimbatsiko.data.rooms.NotificationItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class form_depense extends Fragment {

    private ImageView but_retour, but_notif;
    private Button but_send;
    private EditText note, montant, types, date;
    private Spinner titre;
    private TextView textView_cate, textView_titre, textView_ajout;

    private List<DataCategory> categories = new ArrayList<>();
    private int preselectedCategoryId = -1;
    private int expenseUid = -1;
    private DataExpenses existingExpense;
    private String currentUserId;
    private FirebaseFirestore firestore;

    public form_depense() {}

    public static form_depense newInstance() {
        return new form_depense();
    }

    public static form_depense newInstance(int categoryId) {
        form_depense fragment = new form_depense();
        Bundle args = new Bundle();
        args.putInt("category_id", categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    public static form_depense newInstance(int categoryId, int expenseUid) {
        form_depense fragment = new form_depense();
        Bundle args = new Bundle();
        args.putInt("category_id", categoryId);
        args.putInt("expense_uid", expenseUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            preselectedCategoryId = getArguments().getInt("category_id", -1);
            expenseUid = getArguments().getInt("expense_uid", -1);
        }
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_form, container, false);

        but_retour = view.findViewById(R.id.but_retour);
        but_send = view.findViewById(R.id.but_send);
        note = view.findViewById(R.id.note);
        montant = view.findViewById(R.id.montant);
        types = view.findViewById(R.id.types);
        date = view.findViewById(R.id.date);
        titre = view.findViewById(R.id.titre);
        textView_cate = view.findViewById(R.id.textView4);
        textView_titre = view.findViewById(R.id.textView9);
        textView_ajout = view.findViewById(R.id.textView3);
        but_notif = view.findViewById(R.id.but_notif);

        textView_cate.setText("Categorie");
        types.setHint("Titre de la dépense");
        textView_titre.setText("Titre");
        
        if (expenseUid != -1) {
            textView_ajout.setText("Modifier la dépense");
            but_send.setText("Modifier");
        } else {
            textView_ajout.setText("Ajouter une dépense");
        }

        but_notif.setOnClickListener(v -> openNotifications());
        loadCategories();
        date.setOnClickListener(v -> showDatePicker());

        but_retour.setOnClickListener(v -> {
            if (isAdded()) requireActivity().getSupportFragmentManager().popBackStack();
        });

        but_send.setOnClickListener(v -> saveExpense());

        return view;
    }

    private void loadCategories() {
        new Thread(() -> {
            if (!isAdded() || getContext() == null) return;
            database db = database.getDatabase(requireContext());
            categories = db.categoryDao().getAllCategory();

            if (expenseUid != -1) {
                List<DataExpenses> all = db.expenseDao().getAllExpense(currentUserId);
                for (DataExpenses e : all) {
                    if (e.uid == expenseUid) {
                        existingExpense = e;
                        break;
                    }
                }
            }

            List<String> names = new ArrayList<>();
            int selectionIndex = 0;
            for (int i = 0; i < categories.size(); i++) {
                names.add(categories.get(i).nom);
                if (existingExpense != null) {
                    if (categories.get(i).uid == existingExpense.categoryId) selectionIndex = i;
                } else if (categories.get(i).uid == preselectedCategoryId) {
                    selectionIndex = i;
                }
            }

            final int finalSelectionIndex = selectionIndex;
            if (isAdded() && getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || getContext() == null) return;
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            names
                    ){
                        @NonNull
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View v = super.getView(position, convertView, parent);
                            TextView text = (TextView) v.findViewById(android.R.id.text1);
                            text.setTextColor(getResources().getColor(R.color.cyprus));
                            return v;
                        }
                    };
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    titre.setAdapter(adapter);
                    titre.setSelection(finalSelectionIndex);

                    if (existingExpense != null) {
                        montant.setText(String.valueOf((int)existingExpense.montant));
                        types.setText(existingExpense.titre_depense);
                        date.setText(existingExpense.date);
                        note.setText(existingExpense.message);
                    }
                });
            }
        }).start();
    }

    private void showDatePicker() {
        if (!isAdded() || getContext() == null) return;
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                android.R.style.Theme_Holo_Dialog_MinWidth,
                (view, y, m, d) -> {
                    c.set(y, m, d);
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);
                    date.setText(sdf.format(c.getTime()));
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void saveExpense() {
        if (!isAdded() || getContext() == null) return;
        
        String noteTxt = note.getText().toString();
        String montantTxt = montant.getText().toString();
        String typeTxt = types.getText().toString();
        String dateTxt = date.getText().toString();

        if (montantTxt.isEmpty()) { montant.setError("Champ obligatoire"); return; }
        if (typeTxt.isEmpty()) { types.setError("Champ obligatoire"); return; }
        if (dateTxt.isEmpty()) { date.setError("Champ obligatoire"); return; }

        double montantSaisi;
        try {
            montantSaisi = Double.parseDouble(montantTxt);
        } catch (NumberFormatException e) {
            montant.setError("Montant invalide");
            return;
        }

        int pos = titre.getSelectedItemPosition();
        if (pos < 0 || pos >= categories.size()) {
            Toast.makeText(getContext(), "Veuillez choisir une catégorie", Toast.LENGTH_SHORT).show();
            return;
        }
        DataCategory selectedCategory = categories.get(pos);

        new Thread(() -> {
            if (!isAdded() || getContext() == null) return;
            database db = database.getDatabase(requireContext());
            
            double currentTotalExpense = db.expenseDao().getTotalExpense(currentUserId);
            if (existingExpense != null) currentTotalExpense -= existingExpense.montant;
            
            double totalIncome = db.incomeDao().getTotalIncome(currentUserId);
            double totalSaving = db.savingDao().getTotalAllSaving(currentUserId);
            double availableBalance = totalIncome - currentTotalExpense - totalSaving;

            if (totalIncome <= 0) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Veuillez d'abord enregistrer un revenu.", Toast.LENGTH_LONG).show());
                return;
            }

            if (montantSaisi > availableBalance) {
                requireActivity().runOnUiThread(() -> {
                    montant.requestFocus();
                    montant.setError("Solde insuffisant ! Max : Ar " + String.format(Locale.FRENCH, "%,.0f", availableBalance));
                });
                return;
            }

            DataExpenses depense = (existingExpense != null) ? existingExpense : new DataExpenses();
            depense.date = dateTxt;
            depense.montant = montantSaisi;
            depense.categoryId = selectedCategory.uid;
            depense.titre_depense = typeTxt;
            depense.message = noteTxt;
            depense.userId = currentUserId;
            
            if (existingExpense != null) db.expenseDao().updateExpense(depense);
            else {
                // IMPORTANT : On récupère l'ID généré pour la synchro Firestore
                long id = db.expenseDao().insertExpense(depense);
                depense.uid = (int) id;
            }

            // SYNCHRO CLOUD
            syncExpenseToFirestore(depense, selectedCategory.nom);

            NotificationItem item = new NotificationItem();
            item.title = (existingExpense != null) ? "Dépense modifiée" : "Dépense enregistrée";
            item.message = typeTxt + " - Ar " + String.format(Locale.FRENCH, "%,.0f", depense.montant);
            item.timestamp = System.currentTimeMillis();
            item.type = "expense";
            item.isRead = false;
            item.iconRes = selectedCategory.icon;
            db.notificationDao().insertNotification(item);

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Enregistré et synchronisé !", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            });
        }).start();
    }

    private void syncExpenseToFirestore(DataExpenses expense, String categoryName) {
        if (currentUserId == null) {
            Log.e("FirestoreDebug", "L'UID utilisateur est NULL. Synchro impossible.");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("titre", expense.titre_depense);
        data.put("montant", expense.montant);
        data.put("date", expense.date);
        data.put("note", expense.message);
        data.put("categoryId", expense.categoryId);
        data.put("categoryName", categoryName);
        data.put("userId", currentUserId);
        data.put("updatedAt", System.currentTimeMillis());

        // On utilise l'ID Room comme ID Firestore pour éviter les doublons
        String docId = "exp_" + expense.uid;

        firestore.collection("users")
                .document(currentUserId)
                .collection("expenses")
                .document(docId)
                .set(data)
                .addOnSuccessListener(aVoid -> Log.d("FirestoreDebug", "Synchro réussie pour " + docId))
                .addOnFailureListener(e -> Log.e("FirestoreDebug", "Erreur Firestore: " + e.getMessage()));
    }

    private void openNotifications() {
        if (!isAdded()) return;
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NotificationFragment())
                .addToBackStack(null).commit();
    }
}
