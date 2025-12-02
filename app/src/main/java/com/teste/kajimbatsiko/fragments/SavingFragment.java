package com.teste.kajimbatsiko.fragments;

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

import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.adapter.GridAdapter;
import com.teste.kajimbatsiko.data.dao.ExpenseDao;
import com.teste.kajimbatsiko.data.dao.IncomeDao;
import com.teste.kajimbatsiko.data.dao.SavingDao;
import com.teste.kajimbatsiko.data.database;
import com.teste.kajimbatsiko.data.rooms.DataCategorySaving;
import com.teste.kajimbatsiko.dialog.SavingDialog;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SavingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SavingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SavingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SavingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SavingFragment newInstance(String param1, String param2) {
        SavingFragment fragment = new SavingFragment();
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

    GridView grid;
    GridAdapter adapter;
    TextView total_balance, total_expense;
    Double totalIncome, totalExpense, totalEconomie;
    Button add_new;
    ImageView notifIcon;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.saving, container, false);
        grid = view.findViewById(R.id.grid);
        total_balance = view.findViewById(R.id.total_balance);
        total_expense = view.findViewById(R.id.total_expense);
        add_new = view.findViewById(R.id.button);
        notifIcon = view.findViewById(R.id.imageView2);

        notifIcon.setOnClickListener(v -> openNotifications());

        add_new.setOnClickListener(v -> {
            SavingDialog dialog = new SavingDialog();
            dialog.show(getParentFragmentManager(), "SavingDialog");
        });

        DecimalFormatSymbols symbole = new DecimalFormatSymbols();
        symbole.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbole);

        List<DataCategorySaving> categoriesLists = new ArrayList<>();

        new Thread(() -> {
            database db = database.getDatabase(requireContext());
            List<DataCategorySaving> categories = db.category_savingDao().getAllCategorySaving();
            IncomeDao incomeDao = db.incomeDao();
            ExpenseDao expenseDao = db.expenseDao();
            SavingDao savingDao = db.savingDao();

            totalExpense = expenseDao.getTotalExpense();
            totalEconomie = savingDao.getTotalAllSaving();
            totalIncome = incomeDao.getTotalIncome() - totalExpense - totalEconomie;

            total_balance.setText("Ar " + format.format(totalIncome));
            total_expense.setText("- Ar " + format.format(totalExpense));

            adapter = new GridAdapter(requireContext());
            grid.setAdapter(adapter);
            requireActivity().runOnUiThread(() -> {
                categoriesLists.clear();
                categoriesLists.addAll(categories);

                for (DataCategorySaving c : categories){
                    adapter.addItem(c.nom, c.icon);
                }
            });
        }).start();

        grid.setOnItemClickListener((parent, view1, position, id) -> {
            DataCategorySaving selectedCategory = categoriesLists.get(position);
            int cat_id = selectedCategory.id;
            Saving_List saving_list = Saving_List.newInstance(cat_id);
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_saving, saving_list)
                    .addToBackStack(null)
                    .commit();

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