package com.ansimue.kajimbatsiko.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.ansimue.kajimbatsiko.adapter.AnalyseAdapter;
import com.ansimue.kajimbatsiko.R;
import com.ansimue.kajimbatsiko.data.dao.ExpenseDao;
import com.ansimue.kajimbatsiko.data.dao.IncomeDao;
import com.ansimue.kajimbatsiko.data.dao.SavingDao;
import com.ansimue.kajimbatsiko.data.database;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class AnalyseFragment extends Fragment {

    private ViewPager2 viewPage;
    private TabLayout tabLayout;
    private TextView total_balance, total_expense;
    private ImageView but_notif;

    public AnalyseFragment() {
    }

    public static AnalyseFragment newInstance() {
        return new AnalyseFragment();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.analyse, container, false);

        viewPage = view.findViewById(R.id.viewPage);
        tabLayout = view.findViewById(R.id.tabLayout);
        total_balance = view.findViewById(R.id.textView6);
        total_expense = view.findViewById(R.id.textView8);
        but_notif = view.findViewById(R.id.imageView2);

        but_notif.setOnClickListener(v -> openNotifications());

        setupViewPager();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        if (!isAdded() || getContext() == null) return;

        new Thread(() -> {
            database db = database.getDatabase(requireContext());
            IncomeDao incomeDao = db.incomeDao();
            ExpenseDao expenseDao = db.expenseDao();
            SavingDao savingDao = db.savingDao();

            double totalExpense = expenseDao.getTotalExpense();
            double totalEconomie = savingDao.getTotalAllSaving();
            double totalIncome = incomeDao.getTotalIncome() - totalExpense - totalEconomie;

            if (isAdded() && getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    
                    DecimalFormatSymbols symbole = new DecimalFormatSymbols();
                    symbole.setGroupingSeparator(' ');
                    DecimalFormat format = new DecimalFormat("#,###", symbole);

                    total_balance.setText("Ar " + format.format(totalIncome));
                    total_expense.setText("- Ar " + format.format(totalExpense));
                });
            }
        }).start();
    }

    private void setupViewPager() {
        AnalyseAdapter adapter = new AnalyseAdapter(this);
        viewPage.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPage, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Journalier"); break;
                case 1: tab.setText("Semaine"); break;
                case 2: tab.setText("Mensuel"); break;
                case 3: tab.setText("Annuel"); break;
            }
        }).attach();
    }

    private void openNotifications() {
        if (!isAdded()) return;
        NotificationFragment notificationFragment = new NotificationFragment();
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, notificationFragment)
                .addToBackStack(null)
                .commit();
    }
}
