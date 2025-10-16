package com.teste.kajimbatsiko.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.teste.kajimbatsiko.data.rooms.DataCategorySaving;

import java.util.List;

@Dao
public interface Category_SavingDao {
    @Query("SELECT * FROM categorie_economie")
    List<DataCategorySaving> getAllCategorySaving();

    @Insert
    void insertCategorySaving(DataCategorySaving categories_saving);

    @Query("SELECT icon FROM categorie_economie WHERE id = :category")
    int getIconCategorySaving(int category);

    @Query("SELECT nom FROM categorie_economie WHERE id = :category")
    String getCategorySavingName(int category);}
