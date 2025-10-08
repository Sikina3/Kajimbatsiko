package com.teste.kajimbatsiko.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.teste.kajimbatsiko.adapter.GridAdapter;
import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.data.dao.ExpenseDao;
import com.teste.kajimbatsiko.data.dao.IncomeDao;
import com.teste.kajimbatsiko.data.database;
import com.teste.kajimbatsiko.data.rooms.DataCategory;
import com.teste.kajimbatsiko.dialog.NewCategoryDialog;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CategoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CategoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CategoryFragment newInstance(String param1, String param2) {
        CategoryFragment fragment = new CategoryFragment();
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
    Double totalIncome, totalExpense;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.category, container, false);
        grid = view.findViewById(R.id.grid);
        total_balance = view.findViewById(R.id.total_balance);
        total_expense = view.findViewById(R.id.total_expense);
        String[] titles = {"Plus"};
        int[] images = {R.drawable.plus};

        adapter = new GridAdapter(getContext(), titles, images);
        grid.setAdapter(adapter);

        DecimalFormatSymbols symbole = new DecimalFormatSymbols();
        symbole.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbole);

        List<DataCategory> categoriesList = new ArrayList<>();

        new Thread(() -> {
            database db = database.getDatabase(requireContext());
            List<DataCategory> categories = db.categoryDao().getAllCategory();
            IncomeDao incomeDao = db.incomeDao();
            ExpenseDao expenseDao = db.expenseDao();

            totalExpense = expenseDao.getTotalExpense();
            totalIncome = incomeDao.getTotalIncome() - totalExpense;

            total_balance.setText("Ar " + format.format(totalIncome));
            total_expense.setText("- Ar " + format.format(totalExpense));

            requireActivity().runOnUiThread(() -> {
                categoriesList.clear();
                categoriesList.addAll(categories);
                for (DataCategory c : categories){
                    adapter.addItem(c.nom, c.icon);
                }
            });
        }).start();

        grid.setOnItemClickListener((parent, view1, position, id) -> {
            String item = (String) parent.getItemAtPosition(position);
            if ("Plus".equals(item)){
                NewCategoryDialog dialog = new NewCategoryDialog();
                dialog.show(getParentFragmentManager(), "NewCategoryDialog");
            } else {
                DataCategory selectedCategory = categoriesList.get(position - 1);
                int catId = selectedCategory.uid;
                categorie_lists categorieLists = categorie_lists.newInstance(catId);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_cate, categorieLists)
                        .addToBackStack(null)
                        .commit();
            }
        });


        return view;
    }
}