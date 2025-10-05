package com.teste.kajimbatsiko.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.adapter.CategoryAdapter;
import com.teste.kajimbatsiko.data.dao.CategoryDao;
import com.teste.kajimbatsiko.data.dao.ExpenseDao;
import com.teste.kajimbatsiko.data.database;
import com.teste.kajimbatsiko.data.rooms.DataExpenses;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link categorie_lists#newInstance} factory method to
 * create an instance of this fragment.
 */
public class categorie_lists extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public categorie_lists() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment categorie_lists.
     */
    // TODO: Rename and change types and number of parameters
    public static categorie_lists newInstance(int categoryId) {
        categorie_lists fragment = new categorie_lists();
        Bundle args = new Bundle();
        args.putInt("category_id", categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    private int categoryId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            categoryId = getArguments().getInt("category_id", -1);
        }
    }

    private TextView total_balance, totale_depense, cat_name;
    private ImageView but_retour, notif;
    private RecyclerView affiche_revenue;
    private Button but_new_depense;
    database db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_categorie_lists, container, false);

        total_balance = view.findViewById(R.id.total_balance);
        totale_depense = view.findViewById(R.id.total_depense);
        cat_name = view.findViewById(R.id.cat_name);
        but_retour = view.findViewById(R.id.but_retour);
        notif = view.findViewById(R.id.but_notif);
        but_new_depense = view.findViewById(R.id.button);
        affiche_revenue = view.findViewById(R.id.affiche_revenue);

        but_new_depense.setOnClickListener(v -> {
            form_depense formulaire = form_depense.newInstance();

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, formulaire)
                    .addToBackStack(null)
                    .commit();
        });

        but_retour.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        List<Object> listdepense = new ArrayList<>();
        db = Room.databaseBuilder(requireContext(), database.class, "finance.db")
                .allowMainThreadQueries()
                .build();

        CategoryDao nameCate = db.categoryDao();
        String category_name = nameCate.getCategoryName(categoryId);
        cat_name.setText(category_name);

        DecimalFormatSymbols symbole = new DecimalFormatSymbols();
        symbole.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbole);

        ExpenseDao expenseDao = db.expenseDao();
        List<DataExpenses> expense = expenseDao.getExpenseByCategoryId(categoryId);
//        listdepense.addAll(expense);

        CategoryAdapter adapter = new CategoryAdapter(requireContext(), expense);
        affiche_revenue.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        affiche_revenue.setAdapter(adapter);

        return view;
    }
}