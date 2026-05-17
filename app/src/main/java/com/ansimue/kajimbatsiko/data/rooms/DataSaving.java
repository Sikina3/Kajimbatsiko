package com.ansimue.kajimbatsiko.data.rooms;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "economie",
        foreignKeys = @ForeignKey(
                entity = DataCategorySaving.class,
                parentColumns = "id",
                childColumns = "category_id",
                onDelete = ForeignKey.CASCADE
        ),
            indices = {@Index(value = "category_id")})

public class DataSaving {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "date")
    public String date;

    @ColumnInfo(name = "category_id")
    public int categoryId;

    @ColumnInfo(name = "montant")
    public double montant;

    @ColumnInfo(name = "titre")
    public String titre;

    @ColumnInfo(name = "message")
    public String message;
}
