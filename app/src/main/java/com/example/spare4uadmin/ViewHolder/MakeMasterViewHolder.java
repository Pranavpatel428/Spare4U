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
import com.example.spare4uadmin.Model.CountryModel;
import com.example.spare4uadmin.Model.MakeMasterModel;
import com.example.spare4uadmin.R;

import java.util.List;

public class MakeMasterViewHolder extends RecyclerView.Adapter<MakeMasterViewHolder.ViewHolder>{

    List<MakeMasterModel> makeMasterModels;
    Context context;

    public MakeMasterViewHolder(List<MakeMasterModel> makeMasterModels, Context context) {
        this.makeMasterModels = makeMasterModels;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.make_master_list_design, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.company_name_txt.setText(makeMasterModels.get(position).getCompany_name());

        Glide.with(context).load(makeMasterModels.get(position).getCompany_logo_url()).into(holder.company_logo_image);

    }

    @Override
    public int getItemCount() {
        return makeMasterModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnCreateContextMenuListener{


        public TextView company_name_txt;
        ImageView company_logo_image;
        ItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            company_name_txt = itemView.findViewById(R.id.company_name_txt);
            company_logo_image = itemView.findViewById(R.id.company_logo_image);

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
