package com.teste.kajimbatsiko.fragments;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.adapter.TransactionAdapter;
import com.teste.kajimbatsiko.data.dao.ExpenseDao;
import com.teste.kajimbatsiko.data.dao.IncomeDao;
import com.teste.kajimbatsiko.data.database;
import com.teste.kajimbatsiko.data.rooms.DataExpenses;
import com.teste.kajimbatsiko.data.rooms.DataIncome;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransactionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TransactionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TransactionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TransactionFragment newInstance(String param1, String param2) {
        TransactionFragment fragment = new TransactionFragment();
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

    private LinearLayout but_income, but_expense;
    private ImageView but_ajout;
    private RecyclerView affiche_revenue;
    private ImageView image_revenue, image_expense;
    private TextView text_revenue, text_depense, total_balance;

    private enum Mode { INCOME, EXPENSE }
    private Mode currentMode = Mode.INCOME;
    database db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.transaction, container, false);

        but_income = view.findViewById(R.id.but_income);
        but_expense = view.findViewById(R.id.but_expense);
        but_ajout = view.findViewById(R.id.but_ajout);
        affiche_revenue = view.findViewById(R.id.affiche_revenue);

        text_depense = view.findViewById(R.id.text_depense);
        text_revenue = view.findViewById(R.id.text_revenue);
        image_expense = view.findViewById(R.id.image_expense);
        image_revenue = view.findViewById(R.id.image_income);
        total_balance = view.findViewById(R.id.total_balance);
        updateIU();
        updateTransactionList();

        but_ajout.setOnClickListener(v -> {
            Form form_fragment = Form.newInstance();

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, form_fragment)
                    .addToBackStack(null)
                    .commit();

        });

        but_income.setOnClickListener(v -> {
            currentMode = Mode.INCOME;
            updateIU();
            updateTransactionList();
        });

        but_expense.setOnClickListener(v -> {
            currentMode = Mode.EXPENSE;
            updateIU();
            updateTransactionList();
        });

        return view;
    }

    private void updateIU(){
        if (currentMode == Mode.INCOME){
            but_income.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.ocean_blue)
            );
            but_expense.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.honeydew)
            );
            text_revenue.setTextColor(getResources().getColor(R.color.white));
            text_depense.setTextColor(getResources().getColor(R.color.black));

            image_revenue.setImageTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.white)
            );
            image_expense.setImageTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.ocean_blue)
            );

            but_ajout.setVisibility(View.VISIBLE);
        } else {
            but_income.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.honeydew)
            );
            but_expense.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.ocean_blue)
            );
            text_revenue.setTextColor(getResources().getColor(R.color.black));
            text_depense.setTextColor(getResources().getColor(R.color.white));

            image_revenue.setImageTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.caribeean_green)
            );
            image_expense.setImageTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.white)
            );

            but_ajout.setVisibility(View.GONE);
        }
    }

    private void updateTransactionList() {
        List<Object> transactionList = new ArrayList<>();
        db = Room.databaseBuilder(requireContext(), database.class, "finance.db")
                .allowMainThreadQueries()
                .build();

        DecimalFormatSymbols symbole = new DecimalFormatSymbols();
        symbole.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbole);

        if (currentMode == Mode.INCOME) {
            IncomeDao incomeDao = db.incomeDao();
            ExpenseDao expense = db.expenseDao();
            List<DataIncome> incomes = incomeDao.getAllIncome();
            transactionList.addAll(incomes);

            double totalIncome = incomeDao.getTotalIncome() - expense.getTotalExpense();
            total_balance.setText("Ar " + String.valueOf(format.format(totalIncome)));

        } else {
            ExpenseDao expenseDao = db.expenseDao();
            List<DataExpenses> expenses = expenseDao.getAllExpense();
            transactionList.addAll(expenses);
        }

        // Mettre à jour l'adapter
        TransactionAdapter adapter = new TransactionAdapter(requireContext(), transactionList);
        affiche_revenue.setAdapter(adapter);
        affiche_revenue.setLayoutManager(new LinearLayoutManager(getContext()));
    }


}