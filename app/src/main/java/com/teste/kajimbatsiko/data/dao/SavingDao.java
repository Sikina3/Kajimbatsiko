package com.teste.kajimbatsiko.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.teste.kajimbatsiko.data.rooms.DataSaving;

import java.util.List;

@Dao
public interface SavingDao {
    @Insert
    void insertSaving(DataSaving economie);

    @Query("SELECT * FROM economie ORDER BY date DESC")
    List<DataSaving> getAllSaving();

    @Query("SELECT IFNULL(SUM(montant), 0) FROM economie WHERE category_id = :category")
    Double getTotalSaving(int category);

    @Query("SELECT IFNULL(SUM(montant), 0) FROM economie")
    Double getTotalAllSaving();

    @Query("SELECT * FROM economie WHERE category_id = :categoryId ORDER BY date DESC")
    List<DataSaving>getSavingByCategoryId(int categoryId);
}
