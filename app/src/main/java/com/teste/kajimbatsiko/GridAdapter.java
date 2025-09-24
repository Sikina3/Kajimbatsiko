package com.teste.kajimbatsiko;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter {
    private Context context;
    private String[] title;
    private int[] images;

    public  GridAdapter(Context context, String[] title, int[] images){
        this.context = context;
        this.title = title;
        this.images = images;
    }

    @Override
    public int getCount(){
        return title.length;
    }

    @Override
    public Object getItem(int position){
        return title[position];
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convert, ViewGroup parent){
        if (convert == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convert = inflater.inflate(R.layout.grid_item, parent, false);
        }

        ImageView image = convert.findViewById(R.id.item_image);
        TextView text = convert.findViewById(R.id.item_text);

        image.setImageResource(images[position]);
        text.setText(title[position]);

        return convert;
    }
}
