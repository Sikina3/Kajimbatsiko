package com.ansimue.kajimbatsiko.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ansimue.kajimbatsiko.R;
import com.ansimue.kajimbatsiko.data.rooms.DataCategory;
import com.ansimue.kajimbatsiko.data.rooms.DataExpenses;
import com.ansimue.kajimbatsiko.data.rooms.DataIncome;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    public static final int Type_income = 0, Type_expense = 1;
    private List<Object> transactions;
    private Context context;
    private Map<Integer, DataCategory> categoryMap;

    public interface OnClickListener {
        void onClick(Object item);
    }

    public interface OnLongClickListener {
        void onLongClick(Object item);
    }

    private OnClickListener onClickListener;
    private OnLongClickListener onLongClickListener;

    public void setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(OnLongClickListener listener) {
        this.onLongClickListener = listener;
    }

    public TransactionAdapter(Context context, List<Object> transactions, Map<Integer, DataCategory> categoryMap) {
        this.transactions = transactions;
        this.context = context;
        this.categoryMap = categoryMap;
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
    public int getItemViewType(int position) {
        Object item = transactions.get(position);
        if (item instanceof DataIncome) {
            return Type_income;
        } else if (item instanceof DataExpenses) {
            return Type_expense;
        } else {
            return -1;
        }
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull TransactionAdapter.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        Object item = transactions.get(position);

        DecimalFormatSymbols symbole = new DecimalFormatSymbols();
        symbole.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbole);

        if (viewType == Type_income) {
            DataIncome income = (DataIncome) item;
            holder.image.setImageResource(R.drawable.money);
            holder.titre.setText(income.titre_revenue);
            holder.date.setText(income.date);
            holder.type.setText(income.type);
            holder.montant.setText("Ar " + format.format(income.montant));
        } else if (viewType == Type_expense) {
            DataExpenses expenses = (DataExpenses) item;
            
            DataCategory category = categoryMap != null ? categoryMap.get(expenses.categoryId) : null;
            int icon = category != null ? category.icon : R.drawable.money;
            String nomCategory = category != null ? category.nom : "Dépense";

            holder.image.setImageResource(icon);
            holder.titre.setText(expenses.titre_depense);
            holder.date.setText(expenses.date);
            holder.type.setText(nomCategory);
            holder.montant.setText("- Ar " + format.format(expenses.montant));
        }

        holder.itemView.setOnClickListener(v -> {
            if (onClickListener != null) onClickListener.onClick(item);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onLongClickListener != null) onLongClickListener.onLongClick(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }
}
