package com.ansimue.kajimbatsiko.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ansimue.kajimbatsiko.fragments.TabFragment;

public class AnalyseAdapter extends FragmentStateAdapter {
    public AnalyseAdapter(@NonNull Fragment fragment){
        super(fragment);
    }

    @NonNull
    @Override
    public  Fragment createFragment(int position){
        String text = "";
        switch (position){
            case 0: text = "Journalier"; break;
            case 1: text = "Semaine"; break;
            case 2: text = "Mensuel"; break;
            case 3: text= "Annuel"; break;
        }
        return TabFragment.newInstance(text, "");
    }

    @Override
    public  int getItemCount(){
        return 4;
    }
}
