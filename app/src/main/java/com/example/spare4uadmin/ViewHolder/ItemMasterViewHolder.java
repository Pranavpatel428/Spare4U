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
import com.example.spare4uadmin.Model.ItemMasterModel;
import com.example.spare4uadmin.Model.MakeMasterModel;
import com.example.spare4uadmin.R;

import java.util.List;

public class ItemMasterViewHolder extends RecyclerView.Adapter<ItemMasterViewHolder.ViewHolder>{

    List<ItemMasterModel> itemMasterModels;
    Context context;

    public ItemMasterViewHolder(List<ItemMasterModel> itemMasterModels, Context context) {
        this.itemMasterModels = itemMasterModels;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_master_list_design, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.item_name_txt.setText("Item Name: " + itemMasterModels.get(position).getItem_name());

        holder.make_name_txt.setText("Make: " + itemMasterModels.get(position).getMake_name());

        holder.year_desc_txt.setText("Item Name: " + itemMasterModels.get(position).getYear_desc());

    }

    @Override
    public int getItemCount() {
        return itemMasterModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnCreateContextMenuListener{


        public TextView item_name_txt,make_name_txt,year_desc_txt;
        ItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            item_name_txt = itemView.findViewById(R.id.item_master_item_name_txt);
            make_name_txt = itemView.findViewById(R.id.item_master_make_name_txt);
            year_desc_txt = itemView.findViewById(R.id.item_master_year_txt);

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
