package com.ansimue.kajimbatsiko.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.ansimue.kajimbatsiko.R;
import com.ansimue.kajimbatsiko.data.FinanceData;
import com.ansimue.kajimbatsiko.data.dao.ExpenseDao;
import com.ansimue.kajimbatsiko.data.dao.IncomeDao;
import com.ansimue.kajimbatsiko.data.database;
import com.ansimue.kajimbatsiko.data.rooms.ExpenseSum;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TabFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public TabFragment() {
    }

    public static TabFragment newInstance(String param1, String param2) {
        TabFragment fragment = new TabFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private BarChart barChart;
    private TextView periodTitle, totalIncomeText, totalExpenseText, balanceText;

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
        View view = inflater.inflate(R.layout.fragment_tab_improved, container, false);

        periodTitle = view.findViewById(R.id.periodTitle);
        totalIncomeText = view.findViewById(R.id.totalIncome);
        totalExpenseText = view.findViewById(R.id.totalExpense);
        balanceText = view.findViewById(R.id.balance);
        barChart = view.findViewById(R.id.barChart);

        periodTitle.setText("Analyse " + mParam1);

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
            ExpenseDao expenseDao = db.expenseDao();
            IncomeDao incomeDao = db.incomeDao();

            FinanceData data = fetchData(expenseDao, incomeDao);

            if (isAdded() && getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    setupChart(data);
                    calculateTotals(data);
                });
            }
        }).start();
    }

    private FinanceData fetchData(ExpenseDao expenseDao, IncomeDao incomeDao) {
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

    private void setupChart(FinanceData data) {
        if (!isAdded()) return;
        List<ExpenseSum> expenseList = data.expenses;
        List<ExpenseSum> incomeList = data.incomes;

        int colorIncome = getResources().getColor(R.color.caribeean_green);
        int colorExpense = getResources().getColor(R.color.ocean_blue);

        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int maxSize = Math.max(expenseList.size(), incomeList.size());

        for(int i = 0; i < maxSize; i++){
            float expenseValue = i < expenseList.size() ? expenseList.get(i).total : 0f;
            float incomeValue = i < incomeList.size() ? incomeList.get(i).total : 0f;

            incomeEntries.add(new BarEntry(i, incomeValue));
            expenseEntries.add(new BarEntry(i, expenseValue));

            String label = "";
            if (i < expenseList.size() && expenseList.get(i).date != null) {
                label = formatLabel(expenseList.get(i).date);
            } else if (i < incomeList.size() && incomeList.get(i).date != null) {
                label = formatLabel(incomeList.get(i).date);
            }
            labels.add(label);
        }

        BarDataSet incomeDataset = new BarDataSet(incomeEntries, "Revenus");
        incomeDataset.setColor(colorIncome);
        incomeDataset.setValueTextColor(Color.BLACK);
        incomeDataset.setValueTextSize(9f);
        incomeDataset.setValueFormatter(new MoneyValueFormatter());

        BarDataSet expenseDataset = new BarDataSet(expenseEntries, "Dépenses");
        expenseDataset.setColor(colorExpense);
        expenseDataset.setValueTextColor(Color.BLACK);
        expenseDataset.setValueTextSize(9f);
        expenseDataset.setValueFormatter(new MoneyValueFormatter());

        BarData barData = new BarData(incomeDataset, expenseDataset);

        float barSpace = 0.05f;
        float groupSpace = 0.3f;
        float barWidth = 0.3f;

        barData.setBarWidth(barWidth);
        barChart.setData(barData);

        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.animateY(1000);
        barChart.setDrawGridBackground(false);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelRotationAngle(-45);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setValueFormatter(new MoneyAxisFormatter());

        barChart.getAxisRight().setEnabled(false);

        Legend legend = barChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);

        if (maxSize > 0) {
            barChart.getXAxis().setAxisMinimum(0f);
            barChart.getXAxis().setAxisMaximum(0f + barChart.getBarData().getGroupWidth(groupSpace, barSpace) * maxSize);
            barChart.groupBars(0f, groupSpace, barSpace);
        }

        barChart.invalidate();
    }

    private void calculateTotals(FinanceData data) {
        if (!isAdded()) return;
        double totalIncome = 0;
        double totalExpense = 0;

        for (ExpenseSum income : data.incomes) {
            totalIncome += income.total;
        }

        for (ExpenseSum expense : data.expenses) {
            totalExpense += expense.total;
        }

        double balance = totalIncome - totalExpense;

        DecimalFormat df = new DecimalFormat("#,###");
        totalIncomeText.setText("Ar " + df.format(totalIncome));
        totalExpenseText.setText("Ar " + df.format(totalExpense));
        balanceText.setText("Ar " + df.format(balance));

        if (balance < 0) {
            balanceText.setTextColor(Color.RED);
        } else {
            balanceText.setTextColor(getResources().getColor(R.color.caribeean_green));
        }
    }

    private String formatLabel(String date) {
        if (date == null) return "";

        switch (mParam1) {
            case "Journalier":
                String[] parts = date.split("-");
                if (parts.length == 3) {
                    String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
                            "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};
                    try {
                        int month = Integer.parseInt(parts[1]) - 1;
                        return parts[2] + " " + months[month];
                    } catch (Exception e) { return date; }
                }
                return date;
            case "Semaine":
                return "S" + date.split("-")[1];
            case "Mensuel":
                String[] monthParts = date.split("-");
                if (monthParts.length == 2) {
                    String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
                            "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};
                    try {
                        int m = Integer.parseInt(monthParts[1]) - 1;
                        return months[m] + " " + monthParts[0].substring(2);
                    } catch (Exception e) { return date; }
                }
                return date;
            case "Annuel":
                return date;
            default:
                return date;
        }
    }

    private class MoneyValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            if (value == 0) return "";
            return String.format("%.0fk", value / 1000);
        }
    }

    private class MoneyAxisFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            if (value >= 1000000) {
                return String.format("%.1fM", value / 1000000);
            } else if (value >= 1000) {
                return String.format("%.0fk", value / 1000);
            }
            return String.format("%.0f", value);
        }
    }
}
