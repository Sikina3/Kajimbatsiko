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
import com.ansimue.kajimbatsiko.data.rooms.DataIncome;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Form extends Fragment {

    private int incomeUid = -1;
    private DataIncome existingIncome;
    private String currentUserId;
    private FirebaseFirestore firestore;

    public Form() {
    }

    public static Form newInstance() {
        return new Form();
    }

    public static Form newInstance(int incomeUid) {
        Form fragment = new Form();
        Bundle args = new Bundle();
        args.putInt("income_uid", incomeUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            incomeUid = getArguments().getInt("income_uid", -1);
        }
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    private ImageView but_retour, but_notif;
    private Button but_send;
    private EditText note, montant, types, date;
    private Spinner titre;
    String[] titres = {
            "Salaire",
            "Bourse d'études",
            "Aide familiale",
            "Freelance",
            "Commerce / Vente",
            "Location",
            "Investissement",
            "Cadeau / Don",
            "Remboursement",
            "Autres"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_form, container, false);

        but_retour = view.findViewById(R.id.but_retour);
        but_notif = view.findViewById(R.id.but_notif);
        but_send = view.findViewById(R.id.but_send);
        note = view.findViewById(R.id.note);
        montant = view.findViewById(R.id.montant);
        types = view.findViewById(R.id.types);
        date = view.findViewById(R.id.date);
        titre = view.findViewById(R.id.titre);

        TextView textView_ajout = view.findViewById(R.id.textView3);
        if (incomeUid != -1) {
            textView_ajout.setText("Modifier le Revenu");
            but_send.setText("Modifier");
        }

        but_notif.setOnClickListener(v -> openNotifications());

        date.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    requireContext(),
                    android.R.style.Theme_Holo_Dialog_MinWidth,
                    (view1, year1, month1, dayOfMonth) -> {
                        calendar.set(year1, month1, dayOfMonth);
                        java.text.SimpleDateFormat format =
                                new java.text.SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);
                        String formattedDate = format.format(calendar.getTime());
                        date.setText(formattedDate);
                    },
                    year, month, day
            );
            datePicker.show();
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                titres
        ){
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                View v = super.getView(position, convertView, parent);
                TextView text = (TextView) v.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(R.color.cyprus));
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        titre.setAdapter(adapter);

        if (incomeUid != -1) {
            loadExistingIncome();
        }

        but_retour.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        but_send.setOnClickListener(v -> {
            String noteTxt = note.getText().toString();
            String montantTxt = montant.getText().toString();
            String typeTxt = types.getText().toString();
            String dateTxt = date.getText().toString();
            String titreTxt = titre.getSelectedItem().toString();

            if (montantTxt.isEmpty() || dateTxt.isEmpty()) {
                Toast.makeText(requireContext(), "Champs obligatoires manquants", Toast.LENGTH_SHORT).show();
                return;
            }

            DataIncome revenue = (existingIncome != null) ? existingIncome : new DataIncome();
            revenue.date = dateTxt;
            revenue.montant = Double.parseDouble(montantTxt);
            revenue.titre_revenue = titreTxt;
            revenue.type = typeTxt;
            revenue.message = noteTxt;
            revenue.userId = currentUserId;

            new Thread(() -> {
                database db = database.getDatabase(requireContext());
                if (incomeUid != -1) {
                    db.incomeDao().updateIncome(revenue);
                } else {
                    db.incomeDao().insertIncome(revenue);
                }

                // Synchronisation Cloud avec logs
                syncIncomeToFirestore(revenue);
                
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Enregistré !", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
            }).start();
        });

        return view;
    }

    private void syncIncomeToFirestore(DataIncome income) {
        if (currentUserId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("titre", income.titre_revenue);
        data.put("type", income.type);
        data.put("montant", income.montant);
        data.put("date", income.date);
        data.put("note", income.message);
        data.put("userId", currentUserId);
        data.put("updatedAt", System.currentTimeMillis());

        firestore.collection("users")
                .document(currentUserId)
                .collection("incomes")
                .add(data)
                .addOnSuccessListener(doc -> Log.d("FirestoreDebug", "Revenu synchronisé : " + doc.getId()))
                .addOnFailureListener(e -> Log.e("FirestoreDebug", "Erreur synchro revenu : " + e.getMessage()));
    }

    private void loadExistingIncome() {
        new Thread(() -> {
            database db = database.getDatabase(requireContext());
            List<DataIncome> all = db.incomeDao().getAllIncome(currentUserId);
            for (DataIncome i : all) {
                if (i.uid == incomeUid) {
                    existingIncome = i;
                    break;
                }
            }
            if (isAdded() && existingIncome != null) {
                requireActivity().runOnUiThread(() -> {
                    montant.setText(String.valueOf((int)existingIncome.montant));
                    types.setText(existingIncome.type);
                    date.setText(existingIncome.date);
                    note.setText(existingIncome.message);
                    for (int i = 0; i < titres.length; i++) {
                        if (titres[i].equals(existingIncome.titre_revenue)) {
                            titre.setSelection(i);
                            break;
                        }
                    }
                });
            }
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
