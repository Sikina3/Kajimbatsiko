package com.teste.kajimbatsiko.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.adapter.SavingAdapter;
import com.teste.kajimbatsiko.data.dao.Category_SavingDao;
import com.teste.kajimbatsiko.data.dao.ExpenseDao;
import com.teste.kajimbatsiko.data.dao.IncomeDao;
import com.teste.kajimbatsiko.data.dao.SavingDao;
import com.teste.kajimbatsiko.data.database;
import com.teste.kajimbatsiko.data.rooms.DataSaving;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Saving_List#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Saving_List extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Saving_List() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Saving_List.
     */
    // TODO: Rename and change types and number of parameters
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            categoryId = getArguments().getInt("categorySaving_id", -1);
        }
    }

    private TextView text_objectif, text_atteint, text_nomCat;
    private Button btn_new;
    private ImageView btn_retour, but_notif;
    private RecyclerView affiche_economie;
    private PieChart pieChart;
    private double reste;
    database db;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_saving__list, container, false);
        text_objectif = view.findViewById(R.id.text_objectif);
        text_atteint = view.findViewById(R.id.text_atteint);
        text_nomCat = view.findViewById(R.id.cat_name);
        btn_new = view.findViewById(R.id.button);
        btn_retour = view.findViewById(R.id.but_retour);
        affiche_economie = view.findViewById(R.id.affiche_economie);
        pieChart = view.findViewById(R.id.pieChart);

        btn_retour.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btn_new.setOnClickListener(v -> {
            saving_form formulaire = saving_form.newInstance();

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, formulaire)
                    .addToBackStack(null)
                    .commit();
        });

        db = Room.databaseBuilder(requireContext(), database.class, "finance.db")
                .allowMainThreadQueries()
                .build();

        Category_SavingDao cate = db.category_savingDao();
        SavingDao saving = db.savingDao();
        String category_name =cate.getCategorySavingName(categoryId);
        text_nomCat.setText(category_name);

        DecimalFormatSymbols symbole = new DecimalFormatSymbols();
        symbole.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbole);

        Double devise = cate.getDevis(categoryId);
        text_objectif.setText("Ar " + format.format(devise));
        Double atteint = saving.getTotalSaving(categoryId);
        text_atteint.setText("Ar " + format.format(atteint));

        reste = devise - atteint;

        pieChart.addPieSlice(new PieModel("Progress", atteint.floatValue(),0xFF0068FF ));
        pieChart.addPieSlice(new PieModel("Reste", (float) reste, 0xFFE0E0E0));

        List<DataSaving> dataSavingList = saving.getSavingByCategoryId(categoryId);

        SavingAdapter adapter = new SavingAdapter(requireContext(), dataSavingList);
        affiche_economie.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        affiche_economie.setAdapter(adapter);

        return view;
    }
}