package com.ansimue.kajimbatsiko.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ansimue.kajimbatsiko.data.rooms.DataExpenses;
import com.ansimue.kajimbatsiko.data.rooms.ExpenseSum;

import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertExpense(DataExpenses depense); // Retourne long pour récupérer l'ID généré

    @Update
    void updateExpense(DataExpenses depense);

    @Delete
    void deleteExpense(DataExpenses depense);

    @Query("DELETE FROM depense")
    void deleteAllExpenses();

    @Query("UPDATE depense SET user_id = :userId WHERE user_id IS NULL")
    void linkOrphanExpensesToUser(String userId);

    @Query("SELECT * FROM depense WHERE user_id = :userId ORDER BY " +
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
    List<DataExpenses> getAllExpense(String userId);

    @Query("SELECT IFNULL(SUM(montant), 0) FROM depense WHERE user_id = :userId")
    Double getTotalExpense(String userId);

    @Query("SELECT * FROM depense WHERE category_id = :categoryId AND user_id = :userId ORDER BY " +
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
    List<DataExpenses> getExpenseByCategoryId(int categoryId, String userId);

    @Query("SELECT c.nom FROM depense d INNER JOIN categorie c ON d.category_id = c.uid WHERE d.category_id = :categoryId")
    List<String> getCategoryNamesByExpense(int categoryId);

    @Query("SELECT date AS date, SUM(montant) AS total FROM depense WHERE user_id = :userId GROUP BY date ORDER BY date ASC")
    List<ExpenseSum> getExpensesDaily(String userId);

    @Query("SELECT strftime('%Y-%W', date) AS date, SUM(montant) AS total FROM depense WHERE user_id = :userId GROUP BY strftime('%Y-%W', date) ORDER BY date ASC")
    List<ExpenseSum> getExpensesWeekly(String userId);

    @Query("SELECT strftime('%Y-%m', date) AS date, SUM(montant) AS total FROM depense WHERE user_id = :userId GROUP BY strftime('%Y-%m', date) ORDER BY date ASC")
    List<ExpenseSum> getExpensesMonthly(String userId);

    @Query("SELECT strftime('%Y', date) AS date, SUM(montant) AS total FROM depense WHERE user_id = :userId GROUP BY strftime('%Y', date) ORDER BY date ASC")
    List<ExpenseSum> getExpensesYearly(String userId);
}
