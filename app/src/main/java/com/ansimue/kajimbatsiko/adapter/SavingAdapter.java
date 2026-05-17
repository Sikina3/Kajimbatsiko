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
import com.ansimue.kajimbatsiko.data.rooms.DataSaving;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Map;

public class SavingAdapter extends RecyclerView.Adapter<SavingAdapter.ViewHolder> {
    private List<DataSaving> listes;
    private Context context;
    private Map<Integer, Integer> iconMap;

    public interface OnClickListener {
        void onClick(DataSaving saving);
    }

    public interface OnLongClickListener {
        void onLongClick(DataSaving saving);
    }

    private OnClickListener onClickListener;
    private OnLongClickListener onLongClickListener;

    public void setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(OnLongClickListener listener) {
        this.onLongClickListener = listener;
    }

    public SavingAdapter(Context context, List<DataSaving> listes, Map<Integer, Integer> iconMap) {
        this.context = context;
        this.listes = listes;
        this.iconMap = iconMap;
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
    public SavingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_transaction, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SavingAdapter.ViewHolder holder, int position) {
        DecimalFormatSymbols symbole = new DecimalFormatSymbols();
        symbole.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbole);

        DataSaving saving = listes.get(position);
        Integer iconRes = iconMap != null ? iconMap.get(saving.categoryId) : null;

        if (iconRes != null) {
            holder.image.setImageResource(iconRes);
        } else {
            holder.image.setImageResource(R.drawable.money); // Default icon
        }

        holder.titre.setText(saving.titre);
        holder.date.setText(saving.date);
        holder.montant.setText("Ar " + format.format(saving.montant));

        holder.itemView.setOnClickListener(v -> {
            if (onClickListener != null) onClickListener.onClick(saving);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onLongClickListener != null) onLongClickListener.onLongClick(saving);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listes.size();
    }
}
