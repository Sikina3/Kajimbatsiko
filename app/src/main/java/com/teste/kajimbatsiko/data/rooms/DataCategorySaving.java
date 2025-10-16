package com.teste.kajimbatsiko.data.rooms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorie_economie")
public class DataCategorySaving {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "nom")
    public String nom;

    @ColumnInfo(name = "icon")
    public int icon;

    @ColumnInfo(name = "devise")
    public double devis;
}
