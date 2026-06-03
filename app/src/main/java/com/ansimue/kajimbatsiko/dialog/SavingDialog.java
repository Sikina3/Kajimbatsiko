package com.ansimue.kajimbatsiko.dialog;

import android.os.Bundle;
import android.util.Log;
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
import com.ansimue.kajimbatsiko.data.rooms.DataCategorySaving;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SavingDialog extends DialogFragment {
    private int selectedIcon = R.drawable.food;
    private ImageView selectedImageView = null;
    private int editId = -1;
    private String editNom = null;
    private double editDevis = 0.0;

    public static SavingDialog newInstance(int id, String nom, double devis, int icon) {
        SavingDialog dialog = new SavingDialog();
        Bundle args = new Bundle();
        args.putInt("edit_id", id);
        args.putString("edit_nom", nom);
        args.putDouble("edit_devis", devis);
        args.putInt("edit_icon", icon);
        dialog.setArguments(args);
        return dialog;
    }

    public interface OnCategoryAdded {
        void onCategoryAdded(DataCategorySaving saving);
    }

    private OnCategoryAdded listener;

    public void setOnCategoryAdded(OnCategoryAdded listener) {
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

        input_devise.setVisibility(View.VISIBLE);
        input_devise.setHint("Objectif (Ar)");

        if (getArguments() != null) {
            editId = getArguments().getInt("edit_id", -1);
            editNom = getArguments().getString("edit_nom");
            editDevis = getArguments().getDouble("edit_devis", 0.0);
            selectedIcon = getArguments().getInt("edit_icon", R.drawable.food);
        }

        if (editNom != null) {
            input_name.setText(editNom);
            input_devise.setText(String.valueOf(editDevis));
        }

        int[] icons = {
                R.drawable.lucide_baby,
                R.drawable.lucide_party,
                R.drawable.mingcute_love,
                R.drawable.food,
                R.drawable.money,
                R.drawable.lucide_biceps,
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
                R.drawable.voiture,
                R.drawable.lucide_beer,
                R.drawable.lucide_wifi,
                R.drawable.lucide_wallet,
                R.drawable.lucide_popcorn,
                R.drawable.lucide_laptop,
                R.drawable.lucide_charger,
                R.drawable.lucide_dumbbell,
                R.drawable.lucide_coffee,
                R.drawable.lucide_clapperboard,
                R.drawable.lucide_cigarette,
                R.drawable.lucide_church,
                R.drawable.lucide_cctv,
                R.drawable.lucide_cat,
                R.drawable.lucide_cake,
                R.drawable.lucide_business,
                R.drawable.lucide_book,
                R.drawable.lucide_bed,
                R.drawable.lucide_ambulance
        };

        for (int icon : icons) {
            ImageView image = new ImageView(getContext());
            image.setImageResource(icon);

            int size = dpToPx(50);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(dpToPx(8), 0, dpToPx(8), 0);
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
            });
            if (icon == selectedIcon) {
                image.setBackgroundResource(R.drawable.icon_selected);
                selectedImageView = image;
            }
            iconContainer.addView(image);
        }

        btnSave.setOnClickListener(v -> {
            String category_nom = input_name.getText().toString().trim();
            String category_devis = input_devise.getText().toString().trim();

            if (!category_nom.isEmpty() && !category_devis.isEmpty()) {
                try {
                    DataCategorySaving data = new DataCategorySaving();
                    data.nom = category_nom;
                    data.devis = Double.parseDouble(category_devis);
                    data.icon = selectedIcon;

                    android.content.Context context = getContext();
                    androidx.fragment.app.FragmentActivity activity = getActivity();

                    new Thread(() -> {
                        if (context == null) return;
                        database db = database.getDatabase(context);
                        if (editId != -1) {
                            data.id = editId;
                            db.category_savingDao().updateCategorySaving(data);
                        } else {
                            long id = db.category_savingDao().insertCategorySaving(data);
                            data.id = (int) id;
                        }

                        syncSavingCategoryToFirestore(data);

                        if (listener != null && activity != null) {
                            activity.runOnUiThread(() -> listener.onCategoryAdded(data));
                        }
                    }).start();

                    Toast.makeText(context, "Objectif enregistré et synchronisé", Toast.LENGTH_SHORT).show();
                    dismiss();
                } catch (NumberFormatException e) {
                    input_devise.setError("Montant invalide");
                }
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());
        return view;
    }

    private void syncSavingCategoryToFirestore(DataCategorySaving category) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("id", category.id);
        data.put("nom", category.nom);
        data.put("devis", category.devis);
        data.put("icon", category.icon);
        data.put("userId", userId);

        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .collection("saving_categories")
                .document("scat_" + category.id)
                .set(data)
                .addOnFailureListener(e -> Log.e("FirestoreDebug", "Erreur synchro scat : " + e.getMessage()));
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}