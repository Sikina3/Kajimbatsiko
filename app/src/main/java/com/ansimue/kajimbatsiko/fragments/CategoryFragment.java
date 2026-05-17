package com.ansimue.kajimbatsiko.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ansimue.kajimbatsiko.adapter.GridAdapter;
import com.ansimue.kajimbatsiko.R;
import com.ansimue.kajimbatsiko.data.dao.ExpenseDao;
import com.ansimue.kajimbatsiko.data.dao.IncomeDao;
import com.ansimue.kajimbatsiko.data.dao.SavingDao;
import com.ansimue.kajimbatsiko.data.database;
import com.ansimue.kajimbatsiko.data.rooms.DataCategory;
import com.ansimue.kajimbatsiko.dialog.NewCategoryDialog;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {

    public CategoryFragment() {}

    public static CategoryFragment newInstance() {
        return new CategoryFragment();
    }

    GridView grid;
    GridAdapter adapter;
    TextView total_balance, total_expense;
    Double totalIncome, totalExpense, totalEconomie;
    ImageView but_notif;
    List<DataCategory> categoriesList = new ArrayList<>();

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.category, container, false);
        grid = view.findViewById(R.id.grid);
        total_balance = view.findViewById(R.id.total_balance);
        total_expense = view.findViewById(R.id.total_expense);
        but_notif = view.findViewById(R.id.imageView2);

        but_notif.setOnClickListener(v -> openNotifications());
        
        grid.setOnItemClickListener((parent, view1, position, id) -> {
            String item = (String) parent.getItemAtPosition(position);
            if ("Plus".equals(item)) {
                NewCategoryDialog dialog = new NewCategoryDialog();
                dialog.setOnCategoryAdded(category -> loadData());
                dialog.show(getParentFragmentManager(), "NewCategoryDialog");
            } else {
                if (position - 1 >= 0 && position - 1 < categoriesList.size()) {
                    DataCategory selectedCategory = categoriesList.get(position - 1);
                    int catId = selectedCategory.uid;
                    categorie_lists categorieLists = categorie_lists.newInstance(catId);
                    
                    // FIX: Utiliser le container principal pour que le retour fonctionne
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, categorieLists)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        if (!isAdded()) return;
        DecimalFormatSymbols symbole = new DecimalFormatSymbols();
        symbole.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbole);

        new Thread(() -> {
            if (!isAdded()) return;
            database db = database.getDatabase(requireContext());
            List<DataCategory> categories = db.categoryDao().getAllCategory();
            IncomeDao incomeDao = db.incomeDao();
            ExpenseDao expenseDao = db.expenseDao();
            SavingDao savingDao = db.savingDao();

            totalExpense = expenseDao.getTotalExpense();
            totalEconomie = savingDao.getTotalAllSaving();
            totalIncome = incomeDao.getTotalIncome() - totalExpense - totalEconomie;

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    total_balance.setText("Ar " + format.format(totalIncome));
                    total_expense.setText("- Ar " + format.format(totalExpense));

                    String[] titles = { "Plus" };
                    int[] images = { R.drawable.plus };
                    adapter = new GridAdapter(getContext(), titles, images);
                    grid.setAdapter(adapter);

                    categoriesList.clear();
                    categoriesList.addAll(categories);
                    for (DataCategory c : categories) {
                        adapter.addItem(c.nom, c.icon);
                    }
                });
            }
        }).start();
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
