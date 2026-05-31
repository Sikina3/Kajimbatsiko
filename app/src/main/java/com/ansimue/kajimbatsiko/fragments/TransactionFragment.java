package com.ansimue.kajimbatsiko.fragments;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view. View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ansimue.kajimbatsiko.R;
import com.ansimue.kajimbatsiko.adapter.TransactionAdapter;
import com.ansimue.kajimbatsiko.data.dao.ExpenseDao;
import com.ansimue.kajimbatsiko.data.dao.IncomeDao;
import com.ansimue.kajimbatsiko.data.dao.SavingDao;
import com.ansimue.kajimbatsiko.data.database;
import com.ansimue.kajimbatsiko.data.rooms.DataCategory;
import com.ansimue.kajimbatsiko.data.rooms.DataExpenses;
import com.ansimue.kajimbatsiko.data.rooms.DataIncome;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionFragment extends Fragment {

    private LinearLayout but_income, but_expense;
    private ImageView but_ajout, but_ajout_depense, notifIcon, profileIcon;
    private RecyclerView affiche_revenue;
    private ImageView image_revenue, image_expense;
    private TextView text_revenue, text_depense, total_balance;
    private String currentUserId;

    private enum Mode {
        INCOME, EXPENSE
    }

    private Mode currentMode = Mode.INCOME;
    private database db;

    public TransactionFragment() {
    }

    public static TransactionFragment newInstance() {
        return new TransactionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transaction, container, false);

        but_income = view.findViewById(R.id.but_income);
        but_expense = view.findViewById(R.id.but_expense);
        but_ajout = view.findViewById(R.id.but_ajout);
        but_ajout_depense = view.findViewById(R.id.but_ajout_depense);
        affiche_revenue = view.findViewById(R.id.affiche_revenue);
        text_depense = view.findViewById(R.id.text_depense);
        text_revenue = view.findViewById(R.id.text_revenue);
        image_expense = view.findViewById(R.id.image_expense);
        image_revenue = view.findViewById(R.id.image_income);
        total_balance = view.findViewById(R.id.total_balance);
        notifIcon = view.findViewById(R.id.imageView2);
        profileIcon = view.findViewById(R.id.imageView3);

        db = database.getDatabase(requireContext());

        notifIcon.setOnClickListener(v -> openNotifications());
        profileIcon.setOnClickListener(v -> openProfile());

        but_ajout.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new Form())
                    .addToBackStack(null)
                    .commit();
        });

        but_ajout_depense.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, form_depense.newInstance())
                    .addToBackStack(null)
                    .commit();
        });

        but_income.setOnClickListener(v -> {
            currentMode = Mode.INCOME;
            updateUI();
            updateTransactionList();
        });

        but_expense.setOnClickListener(v -> {
            currentMode = Mode.EXPENSE;
            updateUI();
            updateTransactionList();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
        updateTransactionList();
    }

    private void openNotifications() {
        if (!isAdded()) return;
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new NotificationFragment())
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

    private void updateUI() {
        if (!isAdded()) return;
        if (currentMode == Mode.INCOME) {
            but_income.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.ocean_blue));
            but_expense.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.honeydew));
            text_revenue.setTextColor(getResources().getColor(R.color.white));
            text_depense.setTextColor(getResources().getColor(R.color.black));
            image_revenue.setImageTintList(ContextCompat.getColorStateList(requireContext(), R.color.white));
            image_expense.setImageTintList(ContextCompat.getColorStateList(requireContext(), R.color.ocean_blue));
            but_ajout.setVisibility(View.VISIBLE);
            but_ajout_depense.setVisibility(View.GONE);
        } else {
            but_income.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.honeydew));
            but_expense.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.ocean_blue));
            text_revenue.setTextColor(getResources().getColor(R.color.black));
            text_depense.setTextColor(getResources().getColor(R.color.white));
            image_revenue.setImageTintList(ContextCompat.getColorStateList(requireContext(), R.color.caribeean_green));
            image_expense.setImageTintList(ContextCompat.getColorStateList(requireContext(), R.color.white));
            but_ajout.setVisibility(View.GONE);
            but_ajout_depense.setVisibility(View.VISIBLE);
        }
    }

    private void updateTransactionList() {
        new Thread(() -> {
            if (!isAdded() || getContext() == null) return;
            List<Object> transactionList = new ArrayList<>();
            database dbLocal = database.getDatabase(requireContext());
            IncomeDao incomeDao = dbLocal.incomeDao();
            ExpenseDao expenseDao = dbLocal.expenseDao();
            SavingDao savingDao = dbLocal.savingDao();

            double totalExp = expenseDao.getTotalExpense(currentUserId);
            double totalInc = incomeDao.getTotalIncome(currentUserId);
            double totalSav = savingDao.getTotalAllSaving(currentUserId);
            double balance = totalInc - totalExp - totalSav;

            Map<Integer, DataCategory> categoryMap = new HashMap<>();
            if (currentMode == Mode.EXPENSE) {
                List<DataCategory> allCategories = dbLocal.categoryDao().getAllCategory();
                for (DataCategory cat : allCategories) {
                    categoryMap.put(cat.uid, cat);
                }
            }

            if (currentMode == Mode.INCOME) {
                transactionList.addAll(incomeDao.getAllIncome(currentUserId));
            } else {
                transactionList.addAll(expenseDao.getAllExpense(currentUserId));
            }

            if (isAdded() && getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || getContext() == null) return;
                    
                    DecimalFormatSymbols symbole = new DecimalFormatSymbols();
                    symbole.setGroupingSeparator(' ');
                    DecimalFormat format = new DecimalFormat("#,###", symbole);
                    total_balance.setText("Ar " + format.format(balance));

                    TransactionAdapter adapter = new TransactionAdapter(getContext(), transactionList, categoryMap);
                    
                    adapter.setOnClickListener(item -> {
                        if (item instanceof DataIncome) {
                            Form form = Form.newInstance(((DataIncome) item).uid);
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, form)
                                    .addToBackStack(null).commit();
                        } else if (item instanceof DataExpenses) {
                            form_depense form = form_depense.newInstance(((DataExpenses) item).categoryId, ((DataExpenses) item).uid);
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, form)
                                    .addToBackStack(null).commit();
                        }
                    });

                    adapter.setOnLongClickListener(item -> {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Suppression")
                                .setMessage("Voulez-vous supprimer cet élément ?")
                                .setPositiveButton("Oui", (dialog, id) -> {
                                    new Thread(() -> {
                                        if (item instanceof DataIncome) dbLocal.incomeDao().deleteIncome((DataIncome) item);
                                        else if (item instanceof DataExpenses) dbLocal.expenseDao().deleteExpense((DataExpenses) item);
                                        
                                        // Synchro avec Firebase
                                        deleteFromFirebase(item);
                                        
                                        if (isAdded()) {
                                            requireActivity().runOnUiThread(() -> {
                                                Toast.makeText(getContext(), "Supprimé", Toast.LENGTH_SHORT).show();
                                                updateTransactionList();
                                            });
                                        }
                                    }).start();
                                })
                                .setNegativeButton("Non", null)
                                .show();
                    });

                    affiche_revenue.setAdapter(adapter);
                    affiche_revenue.setLayoutManager(new LinearLayoutManager(getContext()));
                });
            }
        }).start();
    }

    private void deleteFromFirebase(Object item) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (currentUserId == null) return;

        com.google.firebase.firestore.FirebaseFirestore firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance();

        if (item instanceof DataIncome) {
            DataIncome income = (DataIncome) item;
            firestore.collection("users").document(currentUserId).collection("incomes")
                    .whereEqualTo("titre", income.titre_revenue)
                    .whereEqualTo("date", income.date)
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            doc.getReference().delete();
                        }
                    });
        } else if (item instanceof DataExpenses) {
            DataExpenses expense = (DataExpenses) item;
            firestore.collection("users").document(currentUserId).collection("expenses")
                    .whereEqualTo("titre", expense.titre_depense)
                    .whereEqualTo("date", expense.date)
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            doc.getReference().delete();
                        }
                    });
        }
    }
}
