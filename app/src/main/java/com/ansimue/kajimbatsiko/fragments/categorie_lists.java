package com.ansimue.kajimbatsiko.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ansimue.kajimbatsiko.R;
import com.ansimue.kajimbatsiko.adapter.CategoryAdapter;
import com.ansimue.kajimbatsiko.data.dao.CategoryDao;
import com.ansimue.kajimbatsiko.data.dao.ExpenseDao;
import com.ansimue.kajimbatsiko.data.dao.IncomeDao;
import com.ansimue.kajimbatsiko.data.dao.SavingDao;
import com.ansimue.kajimbatsiko.data.database;
import com.ansimue.kajimbatsiko.data.rooms.DataExpenses;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class categorie_lists extends Fragment {

    public categorie_lists() {}

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
            categoryId = getArguments().getInt("category_id", -1);
        }
    }

    private TextView total_balance, totale_depense, cat_name;
    private ImageView but_retour, notif;
    private RecyclerView affiche_revenue;
    private Button but_new_depense;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categorie_lists, container, false);

        total_balance = view.findViewById(R.id.total_balance);
        totale_depense = view.findViewById(R.id.total_depense);
        cat_name = view.findViewById(R.id.cat_name);
        but_retour = view.findViewById(R.id.but_retour);
        notif = view.findViewById(R.id.but_notif);
        but_new_depense = view.findViewById(R.id.button);
        affiche_revenue = view.findViewById(R.id.affiche_revenue);

        notif.setOnClickListener(v -> openNotifications());

        but_new_depense.setOnClickListener(v -> {
            form_depense formulaire = form_depense.newInstance(categoryId);
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, formulaire)
                    .addToBackStack(null)
                    .commit();
        });

        but_retour.setOnClickListener(v -> {
            if (isAdded()) requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        if (!isAdded() || getContext() == null) return;

        DecimalFormatSymbols symbole = new DecimalFormatSymbols();
        symbole.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbole);

        new Thread(() -> {
            if (!isAdded() || getContext() == null) return;
            database db = database.getDatabase(requireContext());
            CategoryDao nameCate = db.categoryDao();
            String category_name = nameCate.getCategoryName(categoryId);
            int categoryIcon = nameCate.getIconCategory(categoryId);

            ExpenseDao expenseDao = db.expenseDao();
            IncomeDao incomeDao = db.incomeDao();
            SavingDao savingDao = db.savingDao();

            double totalExpense = expenseDao.getTotalExpense();
            double totalEconomie = savingDao.getTotalAllSaving();
            double totalIncome = incomeDao.getTotalIncome() - totalExpense - totalEconomie;

            List<DataExpenses> expense = expenseDao.getExpenseByCategoryId(categoryId);

            if (isAdded() && getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    cat_name.setText(category_name);
                    total_balance.setText("Ar " + format.format(totalIncome));
                    totale_depense.setText("- Ar " + format.format(totalExpense));

                    CategoryAdapter adapter = new CategoryAdapter(requireContext(), expense, categoryIcon);
                    
                    adapter.setOnClickListener(exp -> {
                        form_depense form = form_depense.newInstance(categoryId, exp.uid);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, form)
                                .addToBackStack(null).commit();
                    });

                    adapter.setOnLongClickListener(exp -> {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Suppression")
                                .setMessage("Voulez-vous supprimer cette dépense ?")
                                .setPositiveButton("Oui", (dialog, id) -> {
                                    new Thread(() -> {
                                        database.getDatabase(requireContext()).expenseDao().deleteExpense(exp);
                                        if (isAdded()) {
                                            requireActivity().runOnUiThread(() -> {
                                                Toast.makeText(getContext(), "Supprimé", Toast.LENGTH_SHORT).show();
                                                loadData();
                                            });
                                        }
                                    }).start();
                                })
                                .setNegativeButton("Non", null)
                                .show();
                    });

                    affiche_revenue.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
                    affiche_revenue.setAdapter(adapter);
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
