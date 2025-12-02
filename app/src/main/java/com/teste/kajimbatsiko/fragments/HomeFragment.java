package com.teste.kajimbatsiko.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.data.dao.Category_SavingDao;
import com.teste.kajimbatsiko.data.dao.ExpenseDao;
import com.teste.kajimbatsiko.data.dao.IncomeDao;
import com.teste.kajimbatsiko.data.dao.SavingDao;
import com.teste.kajimbatsiko.data.database;
import com.teste.kajimbatsiko.data.rooms.DataCategorySaving;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public HomeFragment() {
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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

    private PieChart piechart;
    private database db;
    private IncomeDao incomeDao;
    private ExpenseDao expenseDao;
    private SavingDao savingDao;
    private Category_SavingDao categorySavingDao;

    private TextView total_income, total_expense, salutation;
    private TextView monthlyIncome, weeklyExpense;
    private TextView savingTitle, savingProgress, savingGoal;
    private ImageView notifIcon;
    private LinearLayout savingCard;

    @SuppressLint("SetTextI18n")
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
        savingCard = rootview.findViewById(R.id.savingCard);
        savingTitle = rootview.findViewById(R.id.savingTitle);
        savingProgress = rootview.findViewById(R.id.savingProgress);
        savingGoal = rootview.findViewById(R.id.savingGoal);

        // Rendre l'icône notification cliquable
        notifIcon.setOnClickListener(v -> openNotifications());

        // Initialiser la base de données
        db = database.getDatabase(requireContext());
        incomeDao = db.incomeDao();
        expenseDao = db.expenseDao();
        savingDao = db.savingDao();
        categorySavingDao = db.category_savingDao();

        // Formatter pour les montants
        DecimalFormatSymbols symbole = new DecimalFormatSymbols();
        symbole.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbole);

        // Définir la salutation selon l'heure
        setSalutation();

        // Charger les données en arrière-plan
        new Thread(() -> {
            // Totaux généraux
            double totalExpense = expenseDao.getTotalExpense();
            double totalEconomie = savingDao.getTotalAllSaving();
            double totalIncome = incomeDao.getTotalIncome() - totalExpense - totalEconomie;

            // Revenue mensuel (mois actuel)
            double monthlyIncomeAmount = getMonthlyIncome();

            // Dépense hebdomadaire (7 derniers jours) dans catégorie "Repas"
            double weeklyFoodExpense = getWeeklyFoodExpense();

            // Économie la plus proche d'être atteinte
            SavingGoalData closestSaving = getClosestSavingGoal();

            requireActivity().runOnUiThread(() -> {
                total_income.setText("Ar " + format.format(totalIncome));
                total_expense.setText("- Ar " + format.format(totalExpense));
                monthlyIncome.setText("Ar " + format.format(monthlyIncomeAmount));
                weeklyExpense.setText("- Ar " + format.format(weeklyFoodExpense));

                // Afficher le diagramme de l'économie
                if (closestSaving != null) {
                    savingCard.setVisibility(View.VISIBLE);
                    displaySavingGoal(closestSaving, format);
                } else {
                    savingCard.setVisibility(View.GONE);
                }
            });
        }).start();

        return rootview;
    }

    private void setSalutation() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Bonjour";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Bon après-midi";
        } else {
            greeting = "Bonsoir";
        }
        salutation.setText(greeting);
    }

    private double getMonthlyIncome() {
        // Format du mois actuel: "2025-01"
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.FRENCH);
        String currentMonth = monthFormat.format(Calendar.getInstance().getTime());

        List<com.teste.kajimbatsiko.data.rooms.DataIncome> allIncomes = incomeDao.getAllIncome();
        double total = 0;

        for (com.teste.kajimbatsiko.data.rooms.DataIncome income : allIncomes) {
            if (income.date != null && income.date.startsWith(currentMonth)) {
                total += income.montant;
            }
        }

        return total;
    }

    private double getWeeklyFoodExpense() {
        // Date d'il y a 7 jours
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        long weekAgo = cal.getTimeInMillis();

        List<com.teste.kajimbatsiko.data.rooms.DataExpenses> allExpenses = expenseDao.getAllExpense();
        double total = 0;

        // Trouver la catégorie "food" ou "Repas"
        List<com.teste.kajimbatsiko.data.rooms.DataCategory> categories = db.categoryDao().getAllCategory();
        int foodCategoryId = -1;

        for (com.teste.kajimbatsiko.data.rooms.DataCategory cat : categories) {
            if (cat.nom != null && (cat.nom.toLowerCase().contains("repas") ||
                    cat.nom.toLowerCase().contains("food") ||
                    cat.nom.toLowerCase().contains("nourriture"))) {
                foodCategoryId = cat.uid;
                break;
            }
        }

        if (foodCategoryId == -1) return 0;

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);

        for (com.teste.kajimbatsiko.data.rooms.DataExpenses expense : allExpenses) {
            if (expense.categoryId == foodCategoryId && expense.date != null) {
                try {
                    long expenseTime = sdf.parse(expense.date).getTime();
                    if (expenseTime >= weekAgo) {
                        total += expense.montant;
                    }
                } catch (Exception e) {
                    // Ignorer les dates mal formatées
                }
            }
        }

        return total;
    }

    private SavingGoalData getClosestSavingGoal() {
        List<DataCategorySaving> savingCategories = categorySavingDao.getAllCategorySaving();

        SavingGoalData closest = null;
        double smallestGap = Double.MAX_VALUE;

        for (DataCategorySaving category : savingCategories) {
            double saved = savingDao.getTotalSaving(category.id);
            double goal = category.devis;
            double gap = goal - saved;

            // Seulement si pas encore atteint et le plus proche
            if (gap > 0 && gap < smallestGap) {
                smallestGap = gap;
                closest = new SavingGoalData(
                        category.nom,
                        saved,
                        goal,
                        category.icon
                );
            }
        }

        return closest;
    }

    private void displaySavingGoal(SavingGoalData data, DecimalFormat format) {
        savingTitle.setText(data.title);
        savingProgress.setText("Ar " + format.format(data.current));
        savingGoal.setText("Objectif: Ar " + format.format(data.goal));

        // Configurer le PieChart
        piechart.clearChart();

        float progress = (float) data.current;
        float remaining = (float) (data.goal - data.current);

        piechart.addPieSlice(new PieModel("Atteint", progress, 0xFF0068FF));
        piechart.addPieSlice(new PieModel("Reste", remaining, 0xFFE0E0E0));
        piechart.startAnimation();
    }

    private void openNotifications() {
        NotificationFragment notificationFragment = new NotificationFragment();
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, notificationFragment)
                .addToBackStack(null)
                .commit();
    }

    // Classe helper pour les données d'économie
    private static class SavingGoalData {
        String title;
        double current;
        double goal;
        int icon;

        SavingGoalData(String title, double current, double goal, int icon) {
            this.title = title;
            this.current = current;
            this.goal = goal;
            this.icon = icon;
        }
    }
}