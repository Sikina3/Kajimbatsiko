package com.teste.kajimbatsiko.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.TransactionAdapter;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransactionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TransactionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TransactionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TransactionFragment newInstance(String param1, String param2) {
        TransactionFragment fragment = new TransactionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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

    private LinearLayout but_income, but_expense;
    private FloatingActionButton but_ajout;
    private RecyclerView affiche_revenue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.transaction, container, false);

        but_income = view.findViewById(R.id.but_income);
        but_expense = view.findViewById(R.id.but_expense);
        but_ajout = view.findViewById(R.id.but_ajout);
        affiche_revenue = view.findViewById(R.id.affiche_revenue);

        List<TransactionAdapter.Transaction> transactionList = List.of(
                new TransactionAdapter.Transaction(R.drawable.money, "Salaire", "30 Avril", "Mois", "Ar 12000"),
                new TransactionAdapter.Transaction(R.drawable.money, "Vente", "01 Mai 2025", "Jour", "Ar 5 000")
        );

        TransactionAdapter adapter = new TransactionAdapter(transactionList);
        affiche_revenue.setAdapter(adapter);

        affiche_revenue.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        but_ajout.setOnClickListener(v -> {
            Form form_fragment = Form.newInstance();

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, form_fragment)
                    .addToBackStack(null)
                    .commit();

        });

        return view;
    }

}