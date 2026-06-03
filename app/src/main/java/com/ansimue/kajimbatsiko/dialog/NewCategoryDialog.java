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
import com.ansimue.kajimbatsiko.data.rooms.DataCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NewCategoryDialog extends DialogFragment {
    private int selectedIcon = R.drawable.food;
    private ImageView selectedImageView = null;
    private int editId = -1;
    private String editNom = null;

    public static NewCategoryDialog newInstance(int uid, String nom, int icon) {
        NewCategoryDialog dialog = new NewCategoryDialog();
        Bundle args = new Bundle();
        args.putInt("edit_id", uid);
        args.putString("edit_nom", nom);
        args.putInt("edit_icon", icon);
        dialog.setArguments(args);
        return dialog;
    }

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

        if (getArguments() != null) {
            editId = getArguments().getInt("edit_id", -1);
            editNom = getArguments().getString("edit_nom");
            selectedIcon = getArguments().getInt("edit_icon", R.drawable.food);
        }

        if (editNom != null) {
            input_name.setText(editNom);
        }

        input_devise.setVisibility(View.GONE);
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
                Toast.makeText(getContext(), "Icon Sélectionné", Toast.LENGTH_SHORT).show();
            });
            if (icon == selectedIcon) {
                image.setBackgroundResource(R.drawable.icon_selected);
                selectedImageView = image;
            }
            iconContainer.addView(image);
        }

        btnSave.setOnClickListener(v -> {
            String category = input_name.getText().toString().trim();
            if (!category.isEmpty()){
                DataCategory datacategory = new DataCategory();
                datacategory.nom = category;
                datacategory.icon = selectedIcon;

                android.content.Context context = getContext();
                androidx.fragment.app.FragmentActivity activity = getActivity();

                new Thread(() -> {
                    if (context == null) return;
                    database db = database.getDatabase(context);
                    if (editId != -1) {
                        datacategory.uid = editId;
                        db.categoryDao().updateCategory(datacategory);
                    } else {
                        long id = db.categoryDao().insertCategory(datacategory);
                        datacategory.uid = (int) id;
                    }

                    syncCategoryToFirestore(datacategory);

                    if(listener != null && activity != null){
                        activity.runOnUiThread(() -> listener.onCategoryAdded(datacategory));
                    }
                }).start();

                Toast.makeText(context, "Catégorie enregistrée et synchronisée", Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                input_name.setError("Il faut remplir le champ!");
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }

    private void syncCategoryToFirestore(DataCategory category) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("id", category.uid);
        data.put("nom", category.nom);
        data.put("icon", category.icon);
        data.put("userId", userId);

        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .collection("categories")
                .document("cat_" + category.uid)
                .set(data)
                .addOnFailureListener(e -> Log.e("FirestoreDebug", "Erreur synchro catégorie : " + e.getMessage()));
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
