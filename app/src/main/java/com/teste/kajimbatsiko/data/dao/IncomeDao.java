package com.teste.kajimbatsiko.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.teste.kajimbatsiko.data.rooms.DataIncome;

import java.util.List;

@Dao
public interface IncomeDao {
    @Insert
    void insertIncome(DataIncome revenue);

    @Query("SELECT * FROM revenue ORDER BY date DESC")
    List<DataIncome> getAllIncome();

    @Query("SELECT IFNULL(SUM(montant), 0) FROM revenue")
    Double getTotalIncome();

}
