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

import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.data.database;
import com.teste.kajimbatsiko.data.rooms.DataIncome;

import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Form#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Form extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Form() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Form.
     */
    // TODO: Rename and change types and number of parameters
    public static Form newInstance() {
        Form fragment = new Form();
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
    String[] titres = {"Salaire", "Autres"};

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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                titres
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        titre.setAdapter(adapter);

        but_retour.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        but_send.setOnClickListener(v -> {
            String noteTxt = note.getText().toString();
            String montantTxt = montant.getText().toString();
            String typeTxt = types.getText().toString();
            String dateTxt = date.getText().toString();
            String titreTxt = titre.getSelectedItem().toString();

            DataIncome revenue = new DataIncome();
            revenue.date = dateTxt;
            revenue.montant = Double.parseDouble(montantTxt);
            revenue.titre_revenue = titreTxt;
            revenue.type = typeTxt;
            revenue.message = noteTxt;

            new Thread(() -> {
                database db = database.getDatabase(requireContext());
                db.incomeDao().insertIncome(revenue);
            }).start();

            //Retour au transactionFragment
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
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