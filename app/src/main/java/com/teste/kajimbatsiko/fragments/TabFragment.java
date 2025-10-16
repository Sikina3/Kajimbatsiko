package com.teste.kajimbatsiko.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teste.kajimbatsiko.R;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TabFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TabFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TabFragment newInstance(String param1, String param2) {
        TabFragment fragment = new TabFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private BarChart barChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab, container, false);

        TextView textView = view.findViewById(R.id.textView);
        barChart = view.findViewById(R.id.barchart);

        float[] income = {5.2f, 0.5f, 6.3f, 0.3f, 10.2f, 1.0f, 2.5f};
        float[] expenses = {2.1f, 3.2f, 4.5f, 5.0f, 8.5f, 0.5f, 5.0f};
        int colorIncome = getResources().getColor(R.color.caribeean_green);
        int colorExpense = getResources().getColor(R.color.ocean_blue);

        for (int i = 0; i< income.length; i++){
            barChart.addBar(new BarModel(income[i], colorIncome));
            barChart.addBar(new BarModel(expenses[i], colorExpense));
        }

        barChart.setShowValues(false);
        barChart.setShowDecimal(false);
        barChart.startAnimation();
        barChart.setOnTouchListener((v, event) -> {
            ViewPager2 pager = requireActivity().findViewById(R.id.viewPage);
            pager.requestDisallowInterceptTouchEvent(true);
            return false;
        });

        textView.setText(mParam1);

        return view;
    }
}