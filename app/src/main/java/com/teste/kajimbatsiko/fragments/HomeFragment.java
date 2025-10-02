package com.teste.kajimbatsiko.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.data.dao.IncomeDao;
import com.teste.kajimbatsiko.data.database;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private PieChart piechart;
    database db;
    IncomeDao incomeDao ;
    TextView total_income, total_expense;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.home, container, false);

        piechart = rootview.findViewById(R.id.piechart);
        total_income = rootview.findViewById(R.id.total_income);
        total_expense = rootview.findViewById(R.id.total_expense);

        int objectif = 1000;
        int progress = 300;
        int reste = objectif - progress;

        piechart.addPieSlice(new PieModel("Progress", progress, 0xFF0068FF));
        piechart.addPieSlice(new PieModel("Reste", reste, 0xFFE0E0E0));

        piechart.startAnimation();

        db = Room.databaseBuilder(requireContext(), database.class, "finance.db")
                .allowMainThreadQueries()
                .build();

        incomeDao = db.incomeDao();
        double totalIncome = incomeDao.getTotalIncome();

        total_income.setText(String.valueOf(totalIncome));

        return rootview;
    }
}