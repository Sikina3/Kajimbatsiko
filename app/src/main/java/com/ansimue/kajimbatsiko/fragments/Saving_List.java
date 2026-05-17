package com.ansimue.kajimbatsiko.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ansimue.kajimbatsiko.R;
import com.ansimue.kajimbatsiko.adapter.SavingAdapter;
import com.ansimue.kajimbatsiko.data.dao.Category_SavingDao;
import com.ansimue.kajimbatsiko.data.dao.SavingDao;
import com.ansimue.kajimbatsiko.data.database;
import com.ansimue.kajimbatsiko.data.rooms.DataSaving;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Saving_List extends Fragment {

    public Saving_List() {}

    public static Saving_List newInstance(int categoryId) {
        Saving_List fragment = new Saving_List();
        Bundle args = new Bundle();
        args.putInt("categorySaving_id", categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    private int categoryId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getInt("categorySaving_id", -1);
        }
    }

    private TextView text_objectif, text_atteint, text_nomCat;
    private Button btn_new;
    private ImageView btn_retour;
    private RecyclerView affiche_economie;
    private PieChart pieChart;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saving__list, container, false);
        text_objectif = view.findViewById(R.id.text_objectif);
        text_atteint = view.findViewById(R.id.text_atteint);
        text_nomCat = view.findViewById(R.id.cat_name);
        btn_new = view.findViewById(R.id.button);
        btn_retour = view.findViewById(R.id.but_retour);
        affiche_economie = view.findViewById(R.id.affiche_economie);
        pieChart = view.findViewById(R.id.pieChart);

        btn_retour.setOnClickListener(v -> {
            if (isAdded()) requireActivity().getSupportFragmentManager().popBackStack();
        });

        btn_new.setOnClickListener(v -> {
            saving_form formulaire = saving_form.newInstance(categoryId);
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, formulaire)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        if (!isAdded() || getContext() == null) return;

        DecimalFormatSymbols symbole = new DecimalFormatSymbols();
        symbole.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbole);

        new Thread(() -> {
            if (!isAdded() || getContext() == null) return;
            database db = database.getDatabase(requireContext());
            Category_SavingDao cateDao = db.category_savingDao();
            SavingDao savingDao = db.savingDao();
            
            String category_name = cateDao.getCategorySavingName(categoryId);
            int categoryIcon = cateDao.getIconCategorySaving(categoryId);
            Double devise = cateDao.getDevis(categoryId);
            Double atteint = savingDao.getTotalSaving(categoryId);
            List<DataSaving> dataSavingList = savingDao.getSavingByCategoryId(categoryId);
            double reste = devise - atteint;

            Map<Integer, Integer> iconMap = new HashMap<>();
            iconMap.put(categoryId, categoryIcon);

            if (isAdded() && getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    text_nomCat.setText(category_name);
                    text_objectif.setText("Ar " + format.format(devise));
                    
                    if (atteint >= devise) {
                        text_atteint.setTextColor(getResources().getColor(R.color.caribeean_green));
                        text_atteint.setText("Objectif Atteint: Ar " + format.format(atteint));
                    } else {
                        text_atteint.setTextColor(getResources().getColor(R.color.caribeean_green));
                        text_atteint.setText("Ar " + format.format(atteint));
                    }

                    pieChart.clearChart();
                    pieChart.addPieSlice(new PieModel("Progress", atteint.floatValue(), 0xFF0068FF));
                    pieChart.addPieSlice(new PieModel("Reste", (float) (reste < 0 ? 0 : reste), 0xFFE0E0E0));
                    pieChart.startAnimation();

                    SavingAdapter adapter = new SavingAdapter(requireContext(), dataSavingList, iconMap);
                    
                    adapter.setOnClickListener(saving -> {
                        saving_form form = saving_form.newInstance(categoryId, saving.id);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, form)
                                .addToBackStack(null).commit();
                    });

                    adapter.setOnLongClickListener(saving -> {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Suppression")
                                .setMessage("Voulez-vous supprimer ce dépôt d'épargne ?")
                                .setPositiveButton("Oui", (dialog, which) -> {
                                    new Thread(() -> {
                                        database.getDatabase(requireContext()).savingDao().deleteSaving(saving);
                                        if (isAdded()) {
                                            requireActivity().runOnUiThread(() -> {
                                                Toast.makeText(getContext(), "Supprimé", Toast.LENGTH_SHORT).show();
                                                loadData();
                                            });
                                        }
                                    }).start();
                                })
                                .setNegativeButton("Non", null)
                                .show();
                    });

                    affiche_economie.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
                    affiche_economie.setAdapter(adapter);
                });
            }
        }).start();
    }
}
