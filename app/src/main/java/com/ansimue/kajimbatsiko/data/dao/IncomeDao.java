package com.ansimue.kajimbatsiko.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ansimue.kajimbatsiko.data.rooms.DataIncome;
import com.ansimue.kajimbatsiko.data.rooms.ExpenseSum;

import java.util.List;

@Dao
public interface IncomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertIncome(DataIncome revenue);

    @Update
    void updateIncome(DataIncome revenue);

    @Delete
    void deleteIncome(DataIncome revenue);

    @Query("DELETE FROM revenue")
    void deleteAllIncomes();

    @Query("UPDATE revenue SET user_id = :userId WHERE user_id IS NULL")
    void linkOrphanIncomeToUser(String userId);

    @Query("SELECT * FROM revenue WHERE user_id = :userId ORDER BY " +
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
    List<DataIncome> getAllIncome(String userId);

    @Query("SELECT IFNULL(SUM(montant), 0) FROM revenue WHERE user_id = :userId")
    Double getTotalIncome(String userId);

    @Query("SELECT date AS date, SUM(montant) AS total FROM revenue WHERE user_id = :userId GROUP BY date ORDER BY date ASC")
    List<ExpenseSum> getIncomeDaily(String userId);

    @Query("SELECT strftime('%Y-%W', date) AS date, SUM(montant) AS total FROM revenue WHERE user_id = :userId GROUP BY strftime('%Y-%W', date) ORDER BY date ASC")
    List<ExpenseSum> getIncomeWeekly(String userId);

    @Query("SELECT strftime('%Y-%m', date) AS date, SUM(montant) AS total FROM revenue WHERE user_id = :userId GROUP BY strftime('%Y-%m', date) ORDER BY date ASC")
    List<ExpenseSum> getIncomeMonthly(String userId);

    @Query("SELECT strftime('%Y', date) AS date, SUM(montant) AS total FROM revenue WHERE user_id = :userId GROUP BY strftime('%Y', date) ORDER BY date ASC")
    List<ExpenseSum> getIncomeYearly(String userId);
}
