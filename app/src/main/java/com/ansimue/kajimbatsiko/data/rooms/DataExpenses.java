package com.ansimue.kajimbatsiko.data.rooms;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "depense",
        foreignKeys = @ForeignKey(
        entity = DataCategory.class,
        parentColumns = "uid",
        childColumns = "category_id",
        onDelete = ForeignKey.CASCADE
    ),
        indices = {@Index(value="category_id")}
)

public class DataExpenses {
    @PrimaryKey (autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "date")
    public String date;

    @ColumnInfo(name = "montant")
    public double montant;

    @ColumnInfo(name = "titre_depense")
    public String titre_depense;

    @ColumnInfo(name = "message")
    public String message;

    @ColumnInfo(name = "category_id")
    public int categoryId;
}
