package com.teste.kajimbatsiko.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.data.FinanceData;
import com.teste.kajimbatsiko.data.dao.ExpenseDao;
import com.teste.kajimbatsiko.data.dao.IncomeDao;
import com.teste.kajimbatsiko.data.database;
import com.teste.kajimbatsiko.data.rooms.ExpenseSum;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ExpenseDao expenseDao;
    private IncomeDao incomeDao;

    public TabFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TabFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TabFragment newInstance(String param1, String param2) {
        TabFragment fragment = new TabFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private BarChart barChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab, container, false);

        TextView textView = view.findViewById(R.id.textView);
        barChart = view.findViewById(R.id.barChart);

        database db = Room.databaseBuilder(requireContext(), database.class, "finance.db")
                .allowMainThreadQueries()
                .build();
        expenseDao = db.expenseDao();
        incomeDao = db.incomeDao();

        FinanceData data = getData();
        List<ExpenseSum> expenseList = data.expenses;
        List<ExpenseSum> incomeList = data.incomes;

        int colorIncome = getResources().getColor(R.color.caribeean_green);
        int colorExpense = getResources().getColor(R.color.ocean_blue);

        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int count = Math.min(expenseList.size(), incomeList.size());
        for(int i = 0; i < count; i++){
            float expenseValue = i < expenseList.size() ? expenseList.get(i).total : 0f;
            float incomeValue = i < incomeList.size() ? incomeList.get(i).total : 0f;

            incomeEntries.add(new BarEntry(i, incomeValue));
            expenseEntries.add(new BarEntry(i, expenseValue));

            String label = i < expenseList.size() ? expenseList.get(i).date : incomeList.get(i).date;
            labels.add(label);
        }

        BarDataSet incomeDataset = new BarDataSet(incomeEntries, "Revenus");
        incomeDataset.setColor(colorIncome);
        incomeDataset.setValueTextColor(Color.BLACK);
        incomeDataset.setValueTextSize(10f);

        BarDataSet expenseDataset = new BarDataSet(expenseEntries, "Depenses");
        expenseDataset.setColor(colorExpense);
        expenseDataset.setValueTextColor(Color.BLACK);
        expenseDataset.setValueTextSize(10f);

        //Regroupement des deux datasets
        BarData datas = new BarData(incomeDataset, expenseDataset);

        //Espacement entre les barres
        float space = 0.4f;
        float barSpace = 0.05f;
        float barWidth = 0.2f;

        datas.setBarWidth(barWidth);
        barChart.setData(datas);

        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);

        XAxis xaxis = barChart.getXAxis();
        xaxis.setGranularity(1f);
        xaxis.setCenterAxisLabels(true);
        xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yaxis = barChart.getAxisLeft();
        yaxis.setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);

        barChart.getXAxis().setAxisMinimum(0f);
        barChart.getXAxis().setAxisMaximum(0f + barChart.getBarData().getGroupWidth(space, barSpace) * count);
        barChart.groupBars(0f, space, barSpace);
        barChart.setBackgroundColor(Color.TRANSPARENT);

        barChart.invalidate();

        textView.setText(mParam1);

        return view;
    }

    private FinanceData getData() {
        List<ExpenseSum> expenses;
        List<ExpenseSum> incomes;

        switch (mParam1){
            case "Journalier":
                expenses = expenseDao.getExpensesDaily();
                incomes = incomeDao.getIncomeDaily();
                break;
            case "Semaine":
                expenses = expenseDao.getExpensesWeekly();
                incomes = incomeDao.getIncomeWeekly();
                break;
            case "Mensuel":
                expenses = expenseDao.getExpensesMonthly();
                incomes = incomeDao.getIncomeMonthly();
                break;
            case "Annuel":
                expenses = expenseDao.getExpensesYearly();
                incomes = incomeDao.getIncomeYearly();
                break;
            default:
                expenses = new ArrayList<>();
                incomes = new ArrayList<>();
        }

        return new FinanceData(expenses, incomes);
    }

}