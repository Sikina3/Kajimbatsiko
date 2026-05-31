package com.ansimue.kajimbatsiko.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ansimue.kajimbatsiko.R;
import com.ansimue.kajimbatsiko.adapter.GridAdapter;
import com.ansimue.kajimbatsiko.data.dao.ExpenseDao;
import com.ansimue.kajimbatsiko.data.dao.IncomeDao;
import com.ansimue.kajimbatsiko.data.dao.SavingDao;
import com.ansimue.kajimbatsiko.data.database;
import com.ansimue.kajimbatsiko.data.rooms.DataCategorySaving;
import com.ansimue.kajimbatsiko.dialog.SavingDialog;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class SavingFragment extends Fragment {

    public SavingFragment() {
    }

    public static SavingFragment newInstance() {
        return new SavingFragment();
    }

    GridView grid;
    GridAdapter adapter;
    TextView total_balance, total_expense;
    Double totalIncome, totalExpense, totalEconomie;
    Button add_new;
    ImageView notifIcon, profileIcon;
    List<DataCategorySaving> categoriesLists = new ArrayList<>();
    private String currentUserId;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.saving, container, false);
        grid = view.findViewById(R.id.grid);
        total_balance = view.findViewById(R.id.total_balance);
        total_expense = view.findViewById(R.id.total_expense);
        add_new = view.findViewById(R.id.button);
        notifIcon = view.findViewById(R.id.imageView2);
        profileIcon = view.findViewById(R.id.imageView3);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        notifIcon.setOnClickListener(v -> openNotifications());
        profileIcon.setOnClickListener(v -> openProfile());

        add_new.setOnClickListener(v -> {
            SavingDialog dialog = new SavingDialog();
            dialog.setOnCategoryAdded(saving -> loadData());
            dialog.show(getParentFragmentManager(), "SavingDialog");
        });

        grid.setOnItemClickListener((parent, view1, position, id) -> {
            if (position < categoriesLists.size()) {
                DataCategorySaving selectedCategory = categoriesLists.get(position);
                int cat_id = selectedCategory.id;
                Saving_List saving_list = Saving_List.newInstance(cat_id);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, saving_list)
                        .addToBackStack(null)
                        .commit();
            }
        });

        grid.setOnItemLongClickListener((parent, view1, position, id) -> {
            if (position < categoriesLists.size()) {
                DataCategorySaving selectedCategory = categoriesLists.get(position);
                showEditDeleteSavingCategoryDialog(selectedCategory);
            }
            return true;
        });

        return view;
    }

    private void showEditDeleteSavingCategoryDialog(DataCategorySaving category) {
        String[] options = {"Modifier", "Supprimer"};
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(category.nom)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        SavingDialog editDialog = SavingDialog.newInstance(category.id, category.nom, category.devis, category.icon);
                        editDialog.setOnCategoryAdded(cat -> loadData());
                        editDialog.show(getParentFragmentManager(), "EditSavingDialog");
                    } else if (which == 1) {
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                .setTitle("Confirmation")
                                .setMessage("Voulez-vous vraiment supprimer cet objectif ?")
                                .setPositiveButton("Oui", (d, w) -> deleteSavingCategory(category))
                                .setNegativeButton("Non", null)
                                .show();
                    }
                })
                .show();
    }

    private void deleteSavingCategory(DataCategorySaving category) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        new Thread(() -> {
            database db = database.getDatabase(requireContext());
            db.category_savingDao().deleteCategorySaving(category);
            
            if (currentUserId != null) {
                com.google.firebase.firestore.FirebaseFirestore firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance();
                firestore.collection("users").document(currentUserId).collection("saving_categories")
                        .whereEqualTo("nom", category.nom)
                        .get().addOnSuccessListener(queryDocumentSnapshots -> {
                            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                doc.getReference().delete();
                            }
                        });
            }
            
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    android.widget.Toast.makeText(getContext(), "Objectif supprimé", android.widget.Toast.LENGTH_SHORT).show();
                    loadData();
                });
            }
        }).start();
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
        format = new DecimalFormat("#,###", symbole); // I need to define format or use local

        new Thread(() -> {
            if (!isAdded() || getContext() == null) return;
            database db = database.getDatabase(requireContext());
            List<DataCategorySaving> categories = db.category_savingDao().getAllCategorySaving();
            IncomeDao incomeDao = db.incomeDao();
            ExpenseDao expenseDao = db.expenseDao();
            SavingDao savingDao = db.savingDao();

            totalExpense = expenseDao.getTotalExpense(currentUserId);
            totalEconomie = savingDao.getTotalAllSaving(currentUserId);
            totalIncome = incomeDao.getTotalIncome(currentUserId) - totalExpense - totalEconomie;

            if (isAdded() && getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    total_balance.setText("Ar " + format.format(totalIncome));
                    total_expense.setText("- Ar " + format.format(totalExpense));

                    adapter = new GridAdapter(requireContext());
                    grid.setAdapter(adapter);
                    
                    categoriesLists.clear();
                    categoriesLists.addAll(categories);

                    for (DataCategorySaving c : categories){
                        adapter.addItem(c.nom, c.icon);
                    }
                });
            }
        }).start();
    }
    
    private DecimalFormat format; // Added this to avoid error

    private void openNotifications() {
        if (!isAdded()) return;
        NotificationFragment notificationFragment = new NotificationFragment();
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, notificationFragment)
                .addToBackStack(null)
                .commit();
    }

    private void openProfile() {
        if (!isAdded()) return;
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ProfileFragment())
                .addToBackStack(null)
                .commit();
    }
}