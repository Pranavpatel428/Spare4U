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

import com.bumptech.glide.Glide;
import com.example.spare4uadmin.Interface.ItemClickListener;
import com.example.spare4uadmin.Model.MakeMasterModel;
import com.example.spare4uadmin.Model.YearMasterModel;
import com.example.spare4uadmin.R;

import java.util.List;

public class YearMasterViewHolder extends RecyclerView.Adapter<YearMasterViewHolder.ViewHolder>{

    List<YearMasterModel> yearMasterModels;
    Context context;

    public YearMasterViewHolder(List<YearMasterModel> yearMasterModels, Context context) {
        this.yearMasterModels = yearMasterModels;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.year_master_list_design_design, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.year_description_txt.setText(yearMasterModels.get(position).getYear_description());

    }

    @Override
    public int getItemCount() {
        return yearMasterModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnCreateContextMenuListener{


        TextView year_description_txt;
        ItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            year_description_txt = itemView.findViewById(R.id.year_description_txt);

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
