package com.sgabhart.gimmeabreak;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by spencer on 12/19/17.
 */

public class PuzzleAdapter extends RecyclerView.Adapter<PuzzleAdapter.ViewHolder> {
    private ArrayList<Puzzle> dataset = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public CardView cv;

        public ViewHolder(CardView v){
            super(v);
            cv = v;
        }
    } // ViewHolder

    public PuzzleAdapter(ArrayList<Puzzle> newDataset){
        dataset = newDataset;
    } // Constructor

    @Override
    public PuzzleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        CardView v = (CardView) LayoutInflater.from(parent.getContext()).
                inflate(R.layout.puzzle_card_view, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        RelativeLayout rl = (RelativeLayout)holder.cv.getChildAt(0);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });

        TextView tvDay = (TextView)rl.getChildAt(0);
        TextView tvDate = (TextView)rl.getChildAt(1);

        tvDay.setText(dataset.get(position).getDay());
        tvDate.setText(dataset.get(position).getDate());
    }

    @Override
    public int getItemCount(){
        return dataset.size();
    }

}
