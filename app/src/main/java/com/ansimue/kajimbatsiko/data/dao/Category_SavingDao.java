package com.ansimue.kajimbatsiko.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ansimue.kajimbatsiko.data.rooms.DataCategorySaving;

import java.util.List;

@Dao
public interface Category_SavingDao {
    @Query("SELECT * FROM categorie_economie")
    List<DataCategorySaving> getAllCategorySaving();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCategorySaving(DataCategorySaving categories_saving);

    @Update
    void updateCategorySaving(DataCategorySaving categories_saving);

    @Delete
    void deleteCategorySaving(DataCategorySaving categories_saving);

    @Query("SELECT icon FROM categorie_economie WHERE id = :category")
    int getIconCategorySaving(int category);

    @Query("SELECT nom FROM categorie_economie WHERE id = :category")
    String getCategorySavingName(int category);

    @Query("SELECT devise FROM categorie_economie WHERE id = :category")
    Double getDevis(int category);
}
