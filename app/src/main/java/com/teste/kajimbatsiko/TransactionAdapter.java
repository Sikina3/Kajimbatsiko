package com.teste.kajimbatsiko;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

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

    private List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionAdapter.ViewHolder holder, int position) {
        Transaction t = transactions.get(position);
        holder.image.setImageResource(t.imageRes);
        holder.titre.setText(t.titre);
        holder.date.setText(t.date);
        holder.type.setText(t.type);
        holder.montant.setText(t.montant);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }
}
