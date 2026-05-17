package com.ansimue.kajimbatsiko.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

import com.ansimue.kajimbatsiko.R;
import com.ansimue.kajimbatsiko.data.database;
import com.ansimue.kajimbatsiko.data.rooms.DataCategorySaving;
import com.ansimue.kajimbatsiko.data.rooms.DataSaving;
import com.ansimue.kajimbatsiko.data.rooms.NotificationItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class saving_form extends Fragment {

    private ImageView but_retour, but_notif;
    private Button but_send;
    private EditText note, montant, types, date;
    private Spinner titre;
    private TextView textView_cate, textView_titre, textView_ajout;
    private List<DataCategorySaving> categories = new ArrayList<>();
    private int preselectedCategoryId = -1;
    private int savingUid = -1;
    private DataSaving existingSaving;

    public saving_form() {}

    public static saving_form newInstance(int categoryId) {
        saving_form fragment = new saving_form();
        Bundle args = new Bundle();
        args.putInt("categorySaving_id", categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    public static saving_form newInstance(int categoryId, int savingUid) {
        saving_form fragment = new saving_form();
        Bundle args = new Bundle();
        args.putInt("categorySaving_id", categoryId);
        args.putInt("saving_uid", savingUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            preselectedCategoryId = getArguments().getInt("categorySaving_id", -1);
            savingUid = getArguments().getInt("saving_uid", -1);
        }
    }

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
        textView_cate = view.findViewById(R.id.textView4);
        textView_titre = view.findViewById(R.id.textView9);
        textView_ajout = view.findViewById(R.id.textView3);

        but_notif.setOnClickListener(v -> openNotifications());

        textView_cate.setText("Categorie");
        textView_titre.setText("Titre de depot");
        types.setHint("Titre du depot");
        
        if (savingUid != -1) {
            textView_ajout.setText("Modifier le depot");
            but_send.setText("Modifier");
        } else {
            textView_ajout.setText("Ajouter un depot économie");
        }

        loadCategories();
        date.setOnClickListener(v -> showDatePicker());

        but_retour.setOnClickListener(v -> {
            if (isAdded()) getParentFragmentManager().popBackStack();
        });

        but_send.setOnClickListener(v -> saveSaving());

        return view;
    }

    private void loadCategories() {
        new Thread(() -> {
            if (!isAdded() || getContext() == null) return;
            database db = database.getDatabase(requireContext());
            categories = db.category_savingDao().getAllCategorySaving();

            if (savingUid != -1) {
                List<DataSaving> allSavings = db.savingDao().getAllSaving();
                for (DataSaving s : allSavings) {
                    if (s.id == savingUid) {
                        existingSaving = s;
                        break;
                    }
                }
            }

            List<String> categoryNames = new ArrayList<>();
            int selectionIndex = 0;
            for (int i = 0; i < categories.size(); i++) {
                categoryNames.add(categories.get(i).nom);
                if (existingSaving != null) {
                    if (categories.get(i).id == existingSaving.categoryId) selectionIndex = i;
                } else if (categories.get(i).id == preselectedCategoryId) {
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
                            categoryNames
                    ) {
                        @NonNull
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView text = (TextView) view.findViewById(android.R.id.text1);
                            text.setTextColor(getResources().getColor(R.color.cyprus));
                            return view;
                        }
                    };
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    titre.setAdapter(adapter);
                    titre.setSelection(finalSelectionIndex);

                    if (existingSaving != null) {
                        montant.setText(String.valueOf((int)existingSaving.montant));
                        types.setText(existingSaving.titre);
                        date.setText(existingSaving.date);
                        note.setText(existingSaving.message);
                    }
                });
            }
        }).start();
    }

    private void showDatePicker() {
        if (!isAdded() || getContext() == null) return;
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                android.R.style.Theme_Holo_Dialog_MinWidth,
                (view1, year1, month1, dayOfMonth) -> {
                    calendar.set(year1, month1, dayOfMonth);
                    java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);
                    date.setText(format.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void saveSaving() {
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
        if (pos < 0 || pos >= categories.size()) return;
        DataCategorySaving selectedCategory = categories.get(pos);

        new Thread(() -> {
            if (!isAdded() || getContext() == null) return;
            database db = database.getDatabase(requireContext());
            
            double totalExp = db.expenseDao().getTotalExpense();
            double totalInc = db.incomeDao().getTotalIncome();
            double totalSav = db.savingDao().getTotalAllSaving();
            if (existingSaving != null) totalSav -= existingSaving.montant;
            
            double availableBalance = totalInc - totalExp - totalSav;

            double currentCategorySaving = db.savingDao().getTotalSaving(selectedCategory.id);
            if (existingSaving != null && existingSaving.categoryId == selectedCategory.id) {
                currentCategorySaving -= existingSaving.montant;
            }
            double objective = selectedCategory.devis;

            if (totalInc <= 0) {
                requireActivity().runOnUiThread(() -> montant.setError("Votre revenu total est de 0 Ar"));
                return;
            }

            if (montantSaisi > availableBalance) {
                requireActivity().runOnUiThread(() -> {
                    montant.requestFocus();
                    montant.setError("Solde insuffisant ! Reste : Ar " + String.format(Locale.FRENCH, "%,.0f", availableBalance));
                });
                return;
            }

            if (currentCategorySaving + montantSaisi > objective) {
                requireActivity().runOnUiThread(() -> {
                    montant.requestFocus();
                    montant.setError("Limite dépassée ! Objectif total : Ar " + String.format(Locale.FRENCH, "%,.0f", objective));
                });
                return;
            }

            DataSaving saving = (existingSaving != null) ? existingSaving : new DataSaving();
            saving.date = dateTxt;
            saving.montant = montantSaisi;
            saving.titre = typeTxt;
            saving.message = noteTxt;
            saving.categoryId = selectedCategory.id;

            if (existingSaving != null) db.savingDao().updateSaving(saving);
            else db.savingDao().insertSaving(saving);

            NotificationItem item = new NotificationItem();
            item.title = (existingSaving != null) ? "Épargne modifiée" : "Épargne enregistrée";
            item.message = typeTxt + " - Ar " + String.format(Locale.FRENCH, "%,.0f", montantSaisi) + " (" + selectedCategory.nom + ")";
            item.timestamp = System.currentTimeMillis();
            item.type = "saving";
            item.isRead = false;
            item.iconRes = R.drawable.money; 
            db.notificationDao().insertNotification(item);

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Enregistré", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            });
        }).start();
    }

    private void openNotifications() {
        if (!isAdded()) return;
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NotificationFragment())
                .addToBackStack(null).commit();
    }
}
