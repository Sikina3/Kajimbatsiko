package com.teste.kajimbatsiko.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.teste.kajimbatsiko.data.rooms.DataIncome;
import com.teste.kajimbatsiko.data.rooms.ExpenseSum;

import java.util.List;

@Dao
public interface IncomeDao {
    @Insert
    void insertIncome(DataIncome revenue);

    // MODIFIÉ: Tri par date chronologique décroissante
    @Query("SELECT * FROM revenue ORDER BY " +
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
    List<DataIncome> getAllIncome();

    @Query("SELECT IFNULL(SUM(montant), 0) FROM revenue")
    Double getTotalIncome();

    //Requête pour barchart
    @Query("SELECT date AS datte, SUM(montant) AS total FROM revenue GROUP BY date ORDER BY date ASC")
    List<ExpenseSum> getIncomeDaily();

    @Query("SELECT strftime('%Y-%W', date) AS date, SUM(montant) AS total FROM revenue GROUP BY strftime('%Y-%W', date) ORDER BY date ASC")
    List<ExpenseSum> getIncomeWeekly();

    @Query("SELECT strftime('%Y-%m', date) AS date, SUM(montant) AS total FROM revenue GROUP BY strftime('%Y-%m', date) ORDER BY date ASC")
    List<ExpenseSum> getIncomeMonthly();

    @Query("SELECT strftime('%Y', date) AS date, SUM(montant) AS total FROM revenue GROUP BY strftime('%Y', date) ORDER BY date ASC")
    List<ExpenseSum> getIncomeYearly();
}