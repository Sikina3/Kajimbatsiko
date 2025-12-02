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

    // MODIFIÉ: Tri par date chronologique décroissante
    @Query("SELECT * FROM depense ORDER BY " +
            "substr(date, 7, 4) || '-' || " +
            "CASE substr(date, 4, 2) " +
            "  WHEN 'janvier' THEN '01' WHEN 'février' THEN '02' " +
            "  WHEN 'mars' THEN '03' WHEN 'avril' THEN '04' " +
            "  WHEN 'mai' THEN '05' WHEN 'juin' THEN '06' " +
            "  WHEN 'juillet' THEN '07' WHEN 'août' THEN '08' " +
            "  WHEN 'septembre' THEN '09' WHEN 'octobre' THEN '10' " +
            "  WHEN 'novembre' THEN '11' WHEN 'décembre' THEN '12' " +
            "  ELSE '00' END || '-' || " +
            "substr('00' || substr(date, 1, 2), -2, 2) DESC")
    List<DataExpenses> getAllExpense();

    @Query("SELECT IFNULL(SUM(montant), 0) FROM depense")
    Double getTotalExpense();

    // MODIFIÉ: Tri par date chronologique décroissante
    @Query("SELECT * FROM depense WHERE category_id = :categoryId ORDER BY " +
            "substr(date, 7, 4) || '-' || " +
            "CASE substr(date, 4, 2) " +
            "  WHEN 'janvier' THEN '01' WHEN 'février' THEN '02' " +
            "  WHEN 'mars' THEN '03' WHEN 'avril' THEN '04' " +
            "  WHEN 'mai' THEN '05' WHEN 'juin' THEN '06' " +
            "  WHEN 'juillet' THEN '07' WHEN 'août' THEN '08' " +
            "  WHEN 'septembre' THEN '09' WHEN 'octobre' THEN '10' " +
            "  WHEN 'novembre' THEN '11' WHEN 'décembre' THEN '12' " +
            "  ELSE '00' END || '-' || " +
            "substr('00' || substr(date, 1, 2), -2, 2) DESC")
    List<DataExpenses> getExpenseByCategoryId(int categoryId);

    @Query("SELECT c.name FROM depense d INNER JOIN categorie c ON d.category_id = c.uid WHERE d.category_id = :categoryId")
    List<String> getCategoryNamesByExpense(int categoryId);

    //Requête pour barchart
    @Query("SELECT date AS datte, SUM(montant) AS total FROM depense GROUP BY date ORDER BY date ASC")
    List<ExpenseSum> getExpensesDaily();

    @Query("SELECT strftime('%Y-%W', date) AS date, SUM(montant) AS total FROM depense GROUP BY strftime('%Y-%W', date) ORDER BY date ASC")
    List<ExpenseSum> getExpensesWeekly();

    @Query("SELECT strftime('%Y-%m', date) AS date, SUM(montant) AS total FROM depense GROUP BY strftime('%Y-%m', date) ORDER BY date ASC")
    List<ExpenseSum> getExpensesMonthly();

    @Query("SELECT strftime('%Y', date) AS date, SUM(montant) AS total FROM depense GROUP BY strftime('%Y', date) ORDER BY date ASC")
    List<ExpenseSum> getExpensesYearly();
}