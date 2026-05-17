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
import com.ansimue.kajimbatsiko.data.rooms.DataExpenses;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private List<DataExpenses> listes;
    private Context context;
    private int categoryIcon;

    public interface OnClickListener {
        void onClick(DataExpenses expense);
    }

    public interface OnLongClickListener {
        void onLongClick(DataExpenses expense);
    }

    private OnClickListener onClickListener;
    private OnLongClickListener onLongClickListener;

    public void setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(OnLongClickListener listener) {
        this.onLongClickListener = listener;
    }

    public CategoryAdapter(Context context, List<DataExpenses> listes, int categoryIcon) {
        this.context = context;
        this.listes = listes;
        this.categoryIcon = categoryIcon;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView titre, date, type, montant;
        View divider1, divider2;

        public ViewHolder(View view) {
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_transaction, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {
        DecimalFormatSymbols symbole = new DecimalFormatSymbols();
        symbole.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbole);

        DataExpenses expenses = listes.get(position);

        // L'icône est maintenant passée par le fragment, plus d'accès DB ici !
        holder.image.setImageResource(categoryIcon != 0 ? categoryIcon : R.drawable.money);
        holder.titre.setText(expenses.titre_depense);
        holder.date.setText(expenses.date);
        holder.montant.setText("- Ar " + format.format(expenses.montant));

        holder.itemView.setOnClickListener(v -> {
            if (onClickListener != null) onClickListener.onClick(expenses);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onLongClickListener != null) onLongClickListener.onLongClick(expenses);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listes.size();
    }
}
