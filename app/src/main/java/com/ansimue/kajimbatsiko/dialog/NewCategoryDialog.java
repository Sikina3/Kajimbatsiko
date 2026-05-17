package com.ansimue.kajimbatsiko.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ansimue.kajimbatsiko.R;
import com.ansimue.kajimbatsiko.data.database;
import com.ansimue.kajimbatsiko.data.rooms.DataCategory;

public class NewCategoryDialog extends DialogFragment {
    private int selectedIcon = R.drawable.food;
    private ImageView selectedImageView = null;

    public interface OnCategoryAdded{
        void onCategoryAdded(DataCategory category);
    }

    private OnCategoryAdded listener;

    public void setOnCategoryAdded(OnCategoryAdded listener){
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_category, container, false);
        EditText input_name = view.findViewById(R.id.inputCategory);
        EditText input_devise = view.findViewById(R.id.inputDevise);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        LinearLayout iconContainer = view.findViewById(R.id.iconContainer);

        input_devise.setVisibility(View.GONE);
        int[] icons = {
                R.drawable.food,
                R.drawable.money,
                R.drawable.avion,
                R.drawable.cadeau,
                R.drawable.economie,
                R.drawable.entrainement,
                R.drawable.louer,
                R.drawable.maison,
                R.drawable.mariage,
                R.drawable.medoc,
                R.drawable.patisseri,
                R.drawable.transport,
                R.drawable.voiture
        };

        for (int icon : icons){
            ImageView image = new ImageView(getContext());
            image.setImageResource(icon);

            int size = dpToPx(50);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(dpToPx(8),0, dpToPx(8), 0);
            image.setLayoutParams(params);

            image.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            image.setBackgroundResource(R.drawable.icon_not_selected);

            image.setOnClickListener(v -> {
                selectedIcon = icon;
                if (selectedImageView != null) {
                    selectedImageView.setBackgroundResource(R.drawable.icon_not_selected);
                }

                image.setBackgroundResource(R.drawable.icon_selected);
                selectedImageView = image;
                Toast.makeText(getContext(), "Icon Selectionné", Toast.LENGTH_SHORT).show();
            });
            iconContainer.addView(image);
        }

        btnSave.setOnClickListener(v -> {
            String category = input_name.getText().toString().trim();
            if (!category.isEmpty()){
                DataCategory datacategory = new DataCategory();
                datacategory.nom = category;
                datacategory.icon = selectedIcon;

                new Thread(() -> {
                    database db = database.getDatabase(requireContext());
                    db.categoryDao().insertCategory(datacategory);

                    if(listener != null){
                        requireActivity().runOnUiThread(() -> listener.onCategoryAdded(datacategory));
                    }
                }).start();

                Toast.makeText(getContext(), "Enregistrer: " + category, Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                input_name.setError("Il faut remplir le champ!");
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }

    private int dpToPx(int dp){
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onStart(){
        super.onStart();

        if(getDialog() != null && getDialog().getWindow() != null){
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
