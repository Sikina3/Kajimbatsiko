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
import com.google.firebase.auth.FirebaseAuth;

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
    ImageView but_notif, profileIcon;
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
        profileIcon = view.findViewById(R.id.imageView3);

        but_notif.setOnClickListener(v -> openNotifications());
        profileIcon.setOnClickListener(v -> openProfile());
        
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
                    categorie_lists fragment = categorie_lists.newInstance(catId);
                    
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
        
        grid.setOnItemLongClickListener((parent, view1, position, id) -> {
            String item = (String) parent.getItemAtPosition(position);
            if (!"Plus".equals(item)) {
                if (position - 1 >= 0 && position - 1 < categoriesList.size()) {
                    DataCategory selectedCategory = categoriesList.get(position - 1);
                    showEditDeleteCategoryDialog(selectedCategory);
                }
            }
            return true;
        });

        return view;
    }

    private void showEditDeleteCategoryDialog(DataCategory category) {
        String[] options = {"Modifier", "Supprimer"};
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(category.nom)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        NewCategoryDialog editDialog = NewCategoryDialog.newInstance(category.uid, category.nom, category.icon);
                        editDialog.setOnCategoryAdded(cat -> loadData());
                        editDialog.show(getParentFragmentManager(), "EditCategoryDialog");
                    } else if (which == 1) {
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                .setTitle("Confirmation")
                                .setMessage("Voulez-vous vraiment supprimer cette catégorie ?")
                                .setPositiveButton("Oui", (d, w) -> deleteCategory(category))
                                .setNegativeButton("Non", null)
                                .show();
                    }
                })
                .show();
    }

    private void deleteCategory(DataCategory category) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        new Thread(() -> {
            database db = database.getDatabase(requireContext());
            db.categoryDao().deleteCategory(category);
            
            if (currentUserId != null) {
                com.google.firebase.firestore.FirebaseFirestore firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance();
                firestore.collection("users").document(currentUserId).collection("categories")
                        .whereEqualTo("nom", category.nom)
                        .get().addOnSuccessListener(queryDocumentSnapshots -> {
                            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                doc.getReference().delete();
                            }
                        });
            }
            
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    android.widget.Toast.makeText(getContext(), "Catégorie supprimée", android.widget.Toast.LENGTH_SHORT).show();
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

            String userId = "";
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }

            totalExpense = expenseDao.getTotalExpense(userId);
            totalEconomie = savingDao.getTotalAllSaving(userId);
            totalIncome = incomeDao.getTotalIncome(userId) - totalExpense - totalEconomie;

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

    private void openProfile() {
        if (!isAdded()) return;
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ProfileFragment())
                .addToBackStack(null)
                .commit();
    }
}
