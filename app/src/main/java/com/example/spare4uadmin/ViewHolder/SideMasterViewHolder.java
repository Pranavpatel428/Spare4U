package com.example.spare4uadmin.ViewHolder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spare4uadmin.Interface.ItemClickListener;
import com.example.spare4uadmin.Model.GroupMasterModel;
import com.example.spare4uadmin.Model.SideMasterModel;
import com.example.spare4uadmin.R;

import java.util.List;

public class SideMasterViewHolder extends RecyclerView.Adapter<SideMasterViewHolder.ViewHolder>{

    List<SideMasterModel> sideMasterModels;
    Context context;

    public SideMasterViewHolder(List<SideMasterModel> sideMasterModels, Context context) {
        this.sideMasterModels = sideMasterModels;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.side_master_list_design_design, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.side_description_txt.setText(sideMasterModels.get(position).getSide_description());

    }

    @Override
    public int getItemCount() {
        return sideMasterModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnCreateContextMenuListener{


        TextView side_description_txt;
        ItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            side_description_txt = itemView.findViewById(R.id.side_description_txt);

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
