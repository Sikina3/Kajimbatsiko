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

    public GridAdapter(Context context) {
        this.context = context;
        this.titles = new ArrayList<>();  // <-- important
        this.images = new ArrayList<>();
    }

    @Override
    public int getCount(){
        return titles != null ? titles.size() : 0;
    }

    @Override
    public Object getItem(int position){
        return titles != null ? titles.get(position) : null;
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    public void addItem(String title, int image){
        if (titles == null) titles = new ArrayList<>();
        if (images == null) images = new ArrayList<>();

        titles.add(title);
        images.add(image);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convert, ViewGroup parent){
        if (convert == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convert = inflater.inflate(R.layout.grid_item, parent, false);
        }

        ImageView image = convert.findViewById(R.id.item_image);
        TextView text = convert.findViewById(R.id.item_text);

        if (position < images.size()) {
            image.setImageResource(images.get(position));
        }
        if (position < titles.size()) {
            text.setText(titles.get(position));
        }

        return convert;
    }
}
