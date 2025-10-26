package com.teste.kajimbatsiko.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.room.Room;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.teste.kajimbatsiko.adapter.AnalyseAdapter;
import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.data.dao.ExpenseDao;
import com.teste.kajimbatsiko.data.dao.IncomeDao;
import com.teste.kajimbatsiko.data.dao.SavingDao;
import com.teste.kajimbatsiko.data.database;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AnalyseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnalyseFragment extends Fragment {

    private ViewPager2 viewPage;
    private TabLayout tabLayout;
    private TextView total_balance, total_expense;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AnalyseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AnalyseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AnalyseFragment newInstance(String param1, String param2) {
        AnalyseFragment fragment = new AnalyseFragment();
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

    database db;
    IncomeDao incomeDao;
    ExpenseDao expenseDao;
    SavingDao savingDao;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.analyse, container, false);

        viewPage = view.findViewById(R.id.viewPage);
        tabLayout = view.findViewById(R.id.tabLayout);
        total_balance = view.findViewById(R.id.textView6);
        total_expense = view.findViewById(R.id.textView8);

        db = Room.databaseBuilder(requireContext(), database.class, "finance.db")
                .allowMainThreadQueries()
                .build();

        DecimalFormatSymbols symbole = new DecimalFormatSymbols();
        symbole.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbole);

        incomeDao = db.incomeDao();
        expenseDao = db.expenseDao();
        savingDao = db.savingDao();

        double totalExpense = expenseDao.getTotalExpense();
        double totalEconomie = savingDao.getTotalAllSaving();
        double totalIncome = incomeDao.getTotalIncome() - totalExpense - totalEconomie;

        total_balance.setText("Ar " + format.format(totalIncome));
        total_expense.setText("- Ar " + format.format(totalExpense));

        setupViewPager();
        return view;
    }

    private void setupViewPager(){
        AnalyseAdapter adapter = new AnalyseAdapter(this);
        viewPage.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPage, (tab, position) -> {
            switch (position){
                case 0: tab.setText("Journalier"); break;
                case 1: tab.setText("Semaine"); break;
                case 2: tab.setText("Mensuel"); break;
                case 3: tab.setText("Annuel"); break;
            }
        }).attach();
    }
}