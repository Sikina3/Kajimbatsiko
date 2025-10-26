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

import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.data.database;
import com.teste.kajimbatsiko.data.rooms.DataCategory;
import com.teste.kajimbatsiko.data.rooms.DataCategorySaving;
import com.teste.kajimbatsiko.data.rooms.DataSaving;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link saving_form#newInstance} factory method to
 * create an instance of this fragment.
 */
public class saving_form extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public saving_form() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment saving_form.
     */
    // TODO: Rename and change types and number of parameters
    public static saving_form newInstance() {
        saving_form fragment = new saving_form();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private ImageView but_retour, but_notif;
    private Button but_send;
    private EditText note, montant, types, date;
    private Spinner titre;
    private TextView textView_cate, textView_titre, textView_ajout;
    private List<DataCategorySaving> categories = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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

        textView_cate.setText("Categorie");
        textView_titre.setText("Titre de depot");
        types.setHint("Titre du depot");

        textView_ajout.setText("Ajouter un depot économie");

        new Thread(() -> {
            database db = database.getDatabase(requireContext());
            categories = db.category_savingDao().getAllCategorySaving();

            List<String> categoryNames = new ArrayList<>();
            for (DataCategorySaving c : categories){
                categoryNames.add(c.nom);
            }
            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        categoryNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                titre.setAdapter(adapter);
            });
        }).start();

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

                        // Formater en "12 Mars 2025"
                        java.text.SimpleDateFormat format =
                                new java.text.SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);
                        String formattedDate = format.format(calendar.getTime());

                        date.setText(formattedDate);
                    },
                    year, month, day
            );
            datePicker.show();
        });

        but_retour.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        but_send.setOnClickListener(v -> {
            String noteTxt = note.getText().toString();
            String montantTxt = montant.getText().toString();
            String typeTxt = types.getText().toString();
            String dateTxt = date.getText().toString();
            String titreText = titre.getSelectedItem().toString();

            int pos = titre.getSelectedItemPosition();
            DataCategorySaving selectedCategory = categories.get(pos);

            DataSaving saving = new DataSaving();
            saving.date = dateTxt;
            saving.montant = Double.parseDouble(montantTxt);
            saving.titre = typeTxt;
            saving.message = noteTxt;
            saving.categoryId = selectedCategory.id;

            new Thread(() -> {
                database db = database.getDatabase(requireContext());
                db.savingDao().insertSaving(saving);
            }).start();

            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }
}