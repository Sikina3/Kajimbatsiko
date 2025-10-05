package com.teste.kajimbatsiko.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.teste.kajimbatsiko.R;

import java.util.ArrayList;
import java.util.Arrays;

public class GridAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> titles;
    private ArrayList<Integer> images;

    public  GridAdapter(Context context, String[] titles, int[] images){
        this.context = context;
        this.titles = new ArrayList<>(Arrays.asList(titles));
        this.images = new ArrayList<>();
        for(int i : images) this.images.add(i);
    }

    @Override
    public int getCount(){
        return titles.size();
    }

    @Override
    public Object getItem(int position){
        return titles.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    public void addItem(String title, int image){
        titles.add(title);
        images.add(image);
        notifyDataSetInvalidated();
    }

    @Override
    public View getView(int position, View convert, ViewGroup parent){
        if (convert == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convert = inflater.inflate(R.layout.grid_item, parent, false);
        }

        ImageView image = convert.findViewById(R.id.item_image);
        TextView text = convert.findViewById(R.id.item_text);

        image.setImageResource(images.get(position));
        text.setText(titles.get(position));

        return convert;
    }
}
