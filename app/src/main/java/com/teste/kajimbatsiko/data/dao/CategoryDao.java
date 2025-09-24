package com.teste.kajimbatsiko.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.teste.kajimbatsiko.data.rooms.DataCategory;

import java.util.List;

@Dao
public interface CategoryDao {
    @Query("SELECT * FROM categorie")
    List<DataCategory> getAllCategory();

    @Insert
    void insertAllCategory(DataCategory... categories);

    @Delete
    void deleteCategory(DataCategory categorie);
}
