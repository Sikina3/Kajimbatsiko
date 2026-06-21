package com.ansimue.kajimbatsiko.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ansimue.kajimbatsiko.EditProfileActivity;
import com.ansimue.kajimbatsiko.HelpActivity;
import com.ansimue.kajimbatsiko.LoginActivity;
import com.ansimue.kajimbatsiko.R;
import com.ansimue.kajimbatsiko.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvId, tvInitials;
    private View btnLogout, btnBack;
    private FirebaseAuth mAuth;

    public ProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        tvName = view.findViewById(R.id.tvName);
        tvId = view.findViewById(R.id.tvId);
        tvInitials = view.findViewById(R.id.tvInitials);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnBack = view.findViewById(R.id.btnBack);

        // Action de retour
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) getActivity().getOnBackPressedDispatcher().onBackPressed();
            });
        }

        // Action Menu
        view.findViewById(R.id.menuEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        view.findViewById(R.id.menuSecurity).setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Sécurité")
                    .setMessage("Fonctionnalité bientôt disponible")
                    .setPositiveButton("OK", null)
                    .show();
        });

        view.findViewById(R.id.menuSetting).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        });

        view.findViewById(R.id.menuHelp).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), HelpActivity.class));
        });

        // Action de déconnexion
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = user.getEmail();
            }
            tvName.setText(name);

            // Générer et afficher les initiales
            tvInitials.setText(getInitials(name));

            String uid = user.getUid();
            String shortId = uid.substring(0, Math.min(uid.length(), 8)).toUpperCase();
            tvId.setText("id: kj" + shortId);
        }
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < Math.min(parts.length, 2); i++) {
            if (!parts[i].isEmpty()) {
                initials.append(parts[i].charAt(0));
            }
        }
        return initials.toString().toUpperCase();
    }
}
