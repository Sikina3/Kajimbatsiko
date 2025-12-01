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

    @Query("SELECT * FROM revenue ORDER BY date DESC")
    List<DataIncome> getAllIncome();

    @Query("SELECT IFNULL(SUM(montant), 0) FROM revenue")
    Double getTotalIncome();

    //Requette pour barchart
    @Query("SELECT date AS datte, SUM(montant) AS total FROM revenue GROUP BY date ORDER BY date ASC")
    List<ExpenseSum> getIncomeDaily();

    @Query("SELECT strftime('%Y-%W', date) AS date, SUM(montant) AS total FROM revenue GROUP BY strftime('%Y-%W', date) ORDER BY date ASC")
    List<ExpenseSum> getIncomeWeekly();

    @Query("SELECT strftime('%Y-%m', date) AS date, SUM(montant) AS total FROM revenue GROUP BY strftime('%Y-%m', date) ORDER BY date ASC")
    List<ExpenseSum> getIncomeMonthly();

    @Query("SELECT strftime('%Y', date) AS date, SUM(montant) AS total FROM revenue GROUP BY strftime('%Y', date) ORDER BY date ASC")
    List<ExpenseSum> getIncomeYearly();

}
