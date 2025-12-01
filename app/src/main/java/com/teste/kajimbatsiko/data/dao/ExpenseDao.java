package com.teste.kajimbatsiko.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.teste.kajimbatsiko.data.rooms.DataExpenses;
import com.teste.kajimbatsiko.data.rooms.ExpenseSum;

import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    void insertExpense(DataExpenses depense);

    @Query("SELECT * FROM depense ORDER BY date DESC")
    List<DataExpenses> getAllExpense();

    @Query("SELECT IFNULL(SUM(montant), 0) FROM depense")
    Double getTotalExpense();

    @Query("SELECT * FROM depense WHERE category_id = :categoryId ORDER BY date DESC")
    List<DataExpenses> getExpenseByCategoryId(int categoryId);

    @Query("SELECT c.name FROM depense d INNER JOIN categorie c ON d.category_id = c.uid WHERE d.category_id = :categoryId")
    List<String> getCategoryNamesByExpense(int categoryId);

    //Requette pour barchart
    @Query("SELECT date AS datte, SUM(montant) AS total FROM depense GROUP BY date ORDER BY date ASC")
    List<ExpenseSum> getExpensesDaily();

    @Query("SELECT strftime('%Y-%W', date) AS date, SUM(montant) AS total FROM depense GROUP BY strftime('%Y-%W', date) ORDER BY date ASC")
    List<ExpenseSum> getExpensesWeekly();

    @Query("SELECT strftime('%Y-%m', date) AS date, SUM(montant) AS total FROM depense GROUP BY strftime('%Y-%m', date) ORDER BY date ASC")
    List<ExpenseSum> getExpensesMonthly();

    @Query("SELECT strftime('%Y', date) AS date, SUM(montant) AS total FROM depense GROUP BY strftime('%Y', date) ORDER BY date ASC")
    List<ExpenseSum> getExpensesYearly();
}
