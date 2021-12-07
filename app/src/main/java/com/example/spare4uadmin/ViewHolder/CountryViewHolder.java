package com.example.spare4uadmin.ViewHolder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spare4uadmin.Interface.ItemClickListener;
import com.example.spare4uadmin.Model.CountryModel;
import com.example.spare4uadmin.R;

import java.util.List;

public class CountryViewHolder extends RecyclerView.Adapter<CountryViewHolder.ViewHolder>{

    List<CountryModel> countryModels;
    Context context;

    public CountryViewHolder(List<CountryModel> countryModels, Context context) {
        this.countryModels = countryModels;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.country_list_design, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.country_name_txt.setText("Country Name: " + countryModels.get(position).getCountry_name());

        holder.continent_name_txt.setText("Continent Name: " + countryModels.get(position).getContinent_name());

    }

    @Override
    public int getItemCount() {
        return countryModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnCreateContextMenuListener{


        public TextView country_name_txt,continent_name_txt;
        ItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            country_name_txt = itemView.findViewById(R.id.country_name_txt);
            continent_name_txt = itemView.findViewById(R.id.continent_name_txt);

            //itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        public  void setItemClickListener (ItemClickListener itemClickListener)
        {
            this.itemClickListener=itemClickListener;
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

            contextMenu.add(0,0,getAdapterPosition(), R.string.edit);
        }
    }
}
