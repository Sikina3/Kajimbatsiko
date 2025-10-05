package com.teste.kajimbatsiko.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.data.dao.CategoryDao;
import com.teste.kajimbatsiko.data.database;
import com.teste.kajimbatsiko.data.rooms.DataCategory;
import com.teste.kajimbatsiko.data.rooms.DataExpenses;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private List<DataExpenses> listes;
    database db;
    private Context context;

    public CategoryAdapter(Context context, List<DataExpenses> listes) {
        this.context = context;
        this.listes = listes;

        // Initialiser la DB une seule fois
        db = Room.databaseBuilder(context, database.class, "finance.db")
                .allowMainThreadQueries()
                .build();
    }

    public static class ViewHolder extends  RecyclerView.ViewHolder{
        ImageView image;
        TextView titre, date, type, montant;
        View divider1, divider2;

        public ViewHolder(View view){
            super(view);
            image = view.findViewById(R.id.imageView8);
            titre = view.findViewById(R.id.titre);
            date = view.findViewById(R.id.date);
            type = view.findViewById(R.id.types);
            montant = view.findViewById(R.id.montants);
            divider1 = view.findViewById(R.id.divider4);
            divider2 = view.findViewById(R.id.divider5);

            type.setVisibility(View.GONE);
            divider1.setVisibility(View.GONE);
            divider2.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_transaction, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {
        DecimalFormatSymbols symbole = new DecimalFormatSymbols();
        symbole.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbole);
        CategoryDao dao_cate = db.categoryDao();

        DataExpenses expenses = (DataExpenses) listes.get(position);
        int iconRes = dao_cate.getIconCategory(expenses.categoryId);

        holder.image.setImageResource(iconRes);
        holder.titre.setText(expenses.titre_depense);
        holder.date.setText(expenses.date);
        holder.montant.setText("- Ar " + format.format(expenses.montant));

    }

    @Override
    public int getItemCount() {
        return listes.size();
    }

}
