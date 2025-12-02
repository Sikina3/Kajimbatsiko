package com.teste.kajimbatsiko.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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

import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.data.database;
import com.teste.kajimbatsiko.data.rooms.DataCategory;
import com.teste.kajimbatsiko.data.rooms.DataExpenses;
import com.teste.kajimbatsiko.data.rooms.NotificationItem;
import com.teste.kajimbatsiko.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class form_depense extends Fragment {

    private ImageView but_retour, but_notif;
    private Button but_send;
    private EditText note, montant, types, date;
    private Spinner titre;
    private TextView textView_cate, textView_titre, textView_ajout;

    private List<DataCategory> categories = new ArrayList<>();

    public form_depense() {}

    public static form_depense newInstance() {
        return new form_depense();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_form, container, false);

        // Bind UI
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

        // Text adjustments
        textView_cate.setText("Categorie");
        types.setHint("Titre de la dépense");
        textView_titre.setText("Titre");
        textView_ajout.setText("Ajouter une dépense");

        but_notif.setOnClickListener(v -> openNotifications());

        // Charger les catégories
        loadCategories();

        // Date picker
        date.setOnClickListener(v -> showDatePicker());

        // Bouton retour
        but_retour.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Bouton envoyer
        but_send.setOnClickListener(v -> saveExpense());

        return view;
    }

    private void loadCategories() {
        new Thread(() -> {
            database db = database.getDatabase(requireContext());
            categories = db.categoryDao().getAllCategory();

            List<String> names = new ArrayList<>();
            for (DataCategory c : categories) names.add(c.nom);

            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        names
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                titre.setAdapter(adapter);
            });
        }).start();
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                android.R.style.Theme_Holo_Dialog_MinWidth,
                (view, y, m, d) -> {
                    c.set(y, m, d);
                    java.text.SimpleDateFormat sdf =
                            new java.text.SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);
                    date.setText(sdf.format(c.getTime()));
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void saveExpense() {
        String noteTxt = note.getText().toString();
        String montantTxt = montant.getText().toString();
        String typeTxt = types.getText().toString();
        String dateTxt = date.getText().toString();

        if (montantTxt.isEmpty() || typeTxt.isEmpty() || dateTxt.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        int pos = titre.getSelectedItemPosition();
        if (pos < 0 || pos >= categories.size()) {
            Toast.makeText(requireContext(), "Aucune catégorie sélectionnée", Toast.LENGTH_SHORT).show();
            return;
        }

        DataCategory selectedCategory = categories.get(pos);

        DataExpenses depense = new DataExpenses();
        depense.date = dateTxt;
        depense.montant = Double.parseDouble(montantTxt);
        depense.categoryId = selectedCategory.uid;
        depense.titre_depense = typeTxt;
        depense.message = noteTxt;

        new Thread(() -> {
            database db = database.getDatabase(requireContext());
            db.expenseDao().insertExpense(depense);

            // Notification en BD
            NotificationItem item = new NotificationItem();
            item.title = "Nouvelle dépense enregistrée";
            item.message = typeTxt + " - Ar " +
                    String.format("%,.0f", depense.montant).replace(",", " ") +
                    " (" + selectedCategory.nom + ")";
            item.timestamp = System.currentTimeMillis();
            item.type = "expense";
            item.isRead = false;
            item.iconRes = selectedCategory.icon;

            db.notificationDao().insertNotification(item);

            // Notification système
            NotificationHelper helper = new NotificationHelper(requireContext());
            helper.sendExpenseNotification(typeTxt, depense.montant, selectedCategory.nom);

            // Vérifier dépassement (dépenses + épargne > revenus)
            double totalExpense = db.expenseDao().getTotalExpense();
            double totalIncome = db.incomeDao().getTotalIncome();
            double totalSaving = db.savingDao().getTotalAllSaving();

            if (totalExpense + totalSaving > totalIncome) {
                double diff = totalExpense + totalSaving - totalIncome;

                NotificationItem alert = new NotificationItem();
                alert.title = "⚠️ ALERTE BUDGET";
                alert.message = "Vos dépenses dépassent vos revenus de Ar " +
                        String.format("%,.0f", diff).replace(",", " ");
                alert.timestamp = System.currentTimeMillis();
                alert.type = "alert";
                alert.isRead = false;
                alert.iconRes = R.drawable.money;

                db.notificationDao().insertNotification(alert);

                helper.sendBudgetAlertNotification(totalExpense + totalSaving, totalIncome);
            }

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Dépense enregistrée", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            });

        }).start();
    }

    private void openNotifications() {
        NotificationFragment notificationFragment = new NotificationFragment();
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, notificationFragment)
                .addToBackStack(null)
                .commit();
    }
}
