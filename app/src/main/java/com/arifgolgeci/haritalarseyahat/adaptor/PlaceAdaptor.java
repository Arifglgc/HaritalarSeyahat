package com.arifgolgeci.haritalarseyahat.adaptor;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arifgolgeci.haritalarseyahat.MapsActivity;
import com.arifgolgeci.haritalarseyahat.databinding.RecyclerRowBinding;
import com.arifgolgeci.haritalarseyahat.model.Place;

import java.util.List;

public class PlaceAdaptor extends RecyclerView.Adapter<PlaceAdaptor.PlaceHolder> {

    List<Place> placesList;
    public  PlaceAdaptor(List<Place> placeList){
        this.placesList=placeList;
    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        RecyclerRowBinding recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(viewGroup.getContext()),viewGroup,false);

        return
 new PlaceHolder(recyclerRowBinding);
    }
    @Override
    public void onBindViewHolder(@NonNull PlaceHolder placeHolder, int i) {
        placeHolder.recyclerRowBinding.recyclerViewTextView.setText(placesList.get(i).name);

        placeHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(placeHolder.itemView.getContext(),
                        MapsActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("place",placesList.get(placeHolder.getAdapterPosition())); // supheli
                placeHolder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }

    public class PlaceHolder extends RecyclerView.ViewHolder{
        RecyclerRowBinding recyclerRowBinding;
        public PlaceHolder(RecyclerRowBinding recyclerRowBinding){
            super(recyclerRowBinding.getRoot());
            this.recyclerRowBinding= recyclerRowBinding;
        }



    }
}
