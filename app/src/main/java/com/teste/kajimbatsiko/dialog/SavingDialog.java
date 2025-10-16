package com.teste.kajimbatsiko.dialog;

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

import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.data.database;
import com.teste.kajimbatsiko.data.rooms.DataCategorySaving;

public class SavingDialog extends DialogFragment {
    private int selectedIcon = R.drawable.food;

    public interface  OnCategoryAdded{
        void onCategoryAdded(DataCategorySaving saving);
    }

    private OnCategoryAdded listener;

    public void setOnCategoryAdded(OnCategoryAdded listener){
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.new_category, container, false);
        EditText input_name = view.findViewById(R.id.inputCategory);
        EditText input_devise = view.findViewById(R.id.inputDevise);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        LinearLayout iconContainer = view.findViewById(R.id.iconContainer);

        input_devise.setVisibility(View.VISIBLE);

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
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 150);
            params.setMargins(10,0, 10, 0);
            image.setLayoutParams(params);

            image.setOnClickListener(v -> {
                selectedIcon = icon;
                Toast.makeText(getContext(), "Icon Selectionné", Toast.LENGTH_SHORT).show();
            });
            iconContainer.addView(image);
        }

        btnSave.setOnClickListener(v -> {
            String category_nom = input_name.getText().toString().trim();
            String category_devis = input_devise.getText().toString().trim();

            if(!category_nom.isEmpty() || !category_devis.isEmpty()){
                DataCategorySaving data = new DataCategorySaving();
                data.nom = category_nom;
                data.devis = Double.parseDouble(category_devis);
                data.icon = selectedIcon;

                new Thread(() -> {
                    database db = database.getDatabase(requireContext());
                    db.category_savingDao().insertCategorySaving(data);

                    if (listener != null){
                        requireActivity().runOnUiThread(() -> listener.onCategoryAdded(data));
                    }
                }).start();

                Toast.makeText(getContext(), "Enregistrer: " + category_nom, Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                input_name.setError("Il faut remplir le champ");
                input_devise.setError("Il faut remplir le champ");
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null){
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

}
