package com.ansimue.kajimbatsiko.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.ansimue.kajimbatsiko.R;
import com.ansimue.kajimbatsiko.data.dao.Category_SavingDao;
import com.ansimue.kajimbatsiko.data.dao.ExpenseDao;
import com.ansimue.kajimbatsiko.data.dao.IncomeDao;
import com.ansimue.kajimbatsiko.data.dao.SavingDao;
import com.ansimue.kajimbatsiko.data.database;
import com.ansimue.kajimbatsiko.data.rooms.DataCategorySaving;
import com.google.firebase.auth.FirebaseAuth;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private PieChart piechart;
    private database db;
    private IncomeDao incomeDao;
    private ExpenseDao expenseDao;
    private SavingDao savingDao;
    private Category_SavingDao categorySavingDao;

    private TextView total_income, total_expense, salutation;
    private TextView monthlyIncome, weeklyExpense;
    private TextView savingTitle, savingProgress, savingGoal;
    private ImageView notifIcon, profileIcon;
    private LinearLayout savingCard;
    private DecimalFormat format;
    private String currentUserId;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.home, container, false);

        // Initialisation des vues
        piechart = rootview.findViewById(R.id.piechart);
        total_income = rootview.findViewById(R.id.total_income);
        total_expense = rootview.findViewById(R.id.total_expense);
        salutation = rootview.findViewById(R.id.salutation);
        monthlyIncome = rootview.findViewById(R.id.textView11);
        weeklyExpense = rootview.findViewById(R.id.textView13);
        notifIcon = rootview.findViewById(R.id.imageView2);
        profileIcon = rootview.findViewById(R.id.imageView3);
        savingCard = rootview.findViewById(R.id.savingCard);
        savingTitle = rootview.findViewById(R.id.savingTitle);
        savingProgress = rootview.findViewById(R.id.savingProgress);
        savingGoal = rootview.findViewById(R.id.savingGoal);

        notifIcon.setOnClickListener(v -> openNotifications());
        profileIcon.setOnClickListener(v -> openProfile());

        db = database.getDatabase(requireContext());
        incomeDao = db.incomeDao();
        expenseDao = db.expenseDao();
        savingDao = db.savingDao();
        categorySavingDao = db.category_savingDao();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        DecimalFormatSymbols symbole = new DecimalFormatSymbols();
        symbole.setGroupingSeparator(' ');
        format = new DecimalFormat("#,###", symbole);

        setSalutation();

        return rootview;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            // Totaux
            double totalExp = expenseDao.getTotalExpense(currentUserId);
            double totalSav = savingDao.getTotalAllSaving(currentUserId);
            double totalInc = incomeDao.getTotalIncome(currentUserId);
            
            // Solde réel (ce qui reste après dépenses et épargne)
            double balance = totalInc - totalExp - totalSav;

            // Stats spécifiques
            double monthlyInc = calculateMonthlyIncome();
            double weeklyFoodExp = calculateWeeklyFoodExpense();

            SavingGoalData closestSaving = getClosestSavingGoal();

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    total_income.setText("Ar " + format.format(balance));
                    total_expense.setText("- Ar " + format.format(totalExp));
                    monthlyIncome.setText("Ar " + format.format(monthlyInc));
                    weeklyExpense.setText("- Ar " + format.format(weeklyFoodExp));

                    if (closestSaving != null) {
                        savingCard.setVisibility(View.VISIBLE);
                        displaySavingGoal(closestSaving);
                    }
                });
            }
        }).start();
    }

    private double calculateMonthlyIncome() {
        Calendar now = Calendar.getInstance();
        int month = now.get(Calendar.MONTH);
        int year = now.get(Calendar.YEAR);

        List<com.ansimue.kajimbatsiko.data.rooms.DataIncome> all = incomeDao.getAllIncome(currentUserId);
        double sum = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);

        for (com.ansimue.kajimbatsiko.data.rooms.DataIncome inc : all) {
            try {
                Date d = sdf.parse(inc.date);
                Calendar c = Calendar.getInstance();
                c.setTime(d);
                if (c.get(Calendar.MONTH) == month && c.get(Calendar.YEAR) == year) {
                    sum += inc.montant;
                }
            } catch (Exception ignored) {}
        }
        return sum;
    }

    private double calculateWeeklyFoodExpense() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        long sevenDaysAgo = cal.getTimeInMillis();

        List<com.ansimue.kajimbatsiko.data.rooms.DataExpenses> all = expenseDao.getAllExpense(currentUserId);
        double sum = 0;
        
        // Chercher ID catégorie nourriture
        int foodId = -1;
        List<com.ansimue.kajimbatsiko.data.rooms.DataCategory> cats = db.categoryDao().getAllCategory();
        for (com.ansimue.kajimbatsiko.data.rooms.DataCategory cat : cats) {
            String n = cat.nom.toLowerCase();
            if (n.contains("repas") || n.contains("food") || n.contains("nourriture") || n.contains("manger")) {
                foodId = cat.uid;
                break;
            }
        }

        if (foodId == -1) return 0;

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);
        for (com.ansimue.kajimbatsiko.data.rooms.DataExpenses exp : all) {
            if (exp.categoryId == foodId) {
                try {
                    Date d = sdf.parse(exp.date);
                    if (d.getTime() >= sevenDaysAgo) {
                        sum += exp.montant;
                    }
                } catch (Exception ignored) {}
            }
        }
        return sum;
    }

    private void displaySavingGoal(SavingGoalData data) {
        savingTitle.setText(data.title);
        savingProgress.setText("Ar " + format.format(data.current));
        savingGoal.setText("Objectif: Ar " + format.format(data.goal));

        piechart.clearChart();
        piechart.addPieSlice(new PieModel("Atteint", (float) data.current, 0xFF0068FF));
        piechart.addPieSlice(new PieModel("Reste", (float) (data.goal - data.current), 0xFFE0E0E0));
        piechart.startAnimation();
    }

    private SavingGoalData getClosestSavingGoal() {
        List<DataCategorySaving> categories = categorySavingDao.getAllCategorySaving();
        SavingGoalData closest = null;
        double minGap = Double.MAX_VALUE;

        for (DataCategorySaving cat : categories) {
            double saved = savingDao.getTotalSaving(cat.id, currentUserId);
            double goal = cat.devis;
            double gap = goal - saved;

            if (gap > 0 && gap < minGap) {
                minGap = gap;
                closest = new SavingGoalData(cat.nom, saved, goal, cat.icon);
            }
        }
        return closest;
    }

    private void setSalutation() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String g = (hour >= 5 && hour < 12) ? "Bonjour" : (hour >= 12 && hour < 18) ? "Bon après-midi" : "Bonsoir";
        salutation.setText(g);
    }

    private void openNotifications() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NotificationFragment())
                .addToBackStack(null).commit();
    }

    private void openProfile() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ProfileFragment())
                .addToBackStack(null).commit();
    }

    private static class SavingGoalData {
        String title; double current; double goal; int icon;
        SavingGoalData(String t, double c, double g, int i) { title = t; current = c; goal = g; icon = i; }
    }
}
