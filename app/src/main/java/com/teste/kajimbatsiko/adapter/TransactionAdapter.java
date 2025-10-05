package com.teste.kajimbatsiko.adapter;

import android.annotation.SuppressLint;
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
import com.teste.kajimbatsiko.data.dao.ExpenseDao;
import com.teste.kajimbatsiko.data.database;
import com.teste.kajimbatsiko.data.rooms.DataExpenses;
import com.teste.kajimbatsiko.data.rooms.DataIncome;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    public static  final int Type_income = 0, Type_expense = 1;
    private List<Object> transactions;
    database db;
    private Context context;

    // Ici on définit notre modèle simple pour la transaction
    public static class Transaction {
        public int imageRes;
        public String titre;
        public String date;
        public String type;
        public String montant;

        public Transaction(int imageRes, String titre, String date, String type, String montant) {
            this.imageRes = imageRes;
            this.titre = titre;
            this.date = date;
            this.type = type;
            this.montant = montant;
        }
    }

    public TransactionAdapter(Context context, List<Object> transactions) {
        this.transactions = transactions;
        this.context = context;

        db = Room.databaseBuilder(context, database.class, "finance.db")
                .allowMainThreadQueries()
                .build();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView titre, date, type, montant;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageView8);
            titre = itemView.findViewById(R.id.titre);
            date = itemView.findViewById(R.id.date);
            type = itemView.findViewById(R.id.types);
            montant = itemView.findViewById(R.id.montants);
        }
    }

    @NonNull
    @Override
    public TransactionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == Type_income){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_transaction, parent, false);
        } else {
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_transaction, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        Object item = transactions.get(position);
        if (item instanceof DataIncome) {
            return Type_income;
        } else if(item instanceof DataExpenses){
            return Type_expense;
        } else {
            return -1;
        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull TransactionAdapter.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        DecimalFormatSymbols symbole = new DecimalFormatSymbols();
        symbole.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbole);

        if (viewType == Type_income) {
            DataIncome income = (DataIncome) transactions.get(position);
            holder.image.setImageResource(R.drawable.money);
            holder.titre.setText(income.titre_revenue);
            holder.date.setText(income.date);
            holder.type.setText(income.type);
            holder.montant.setText("Ar " + format.format(income.montant));
        } else if (viewType == Type_expense){
            DataExpenses expenses = (DataExpenses) transactions.get(position);
            CategoryDao daoCate = db.categoryDao();
            int icon = daoCate.getIconCategory(expenses.categoryId);
            String nomCategory = daoCate.getCategoryName(expenses.categoryId);

            holder.image.setImageResource(icon);
            holder.titre.setText(expenses.titre_depense);
            holder.date.setText(expenses.date);
            holder.type.setText(nomCategory);
            holder.montant.setText("- Ar " + format.format(expenses.montant));
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }
}
