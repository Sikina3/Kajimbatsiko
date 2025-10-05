package com.teste.kajimbatsiko.data.rooms;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorie")
public class DataCategory {
    @PrimaryKey (autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "name")
    public String nom;

    @ColumnInfo(name = "icon")
    public int icon;
}
