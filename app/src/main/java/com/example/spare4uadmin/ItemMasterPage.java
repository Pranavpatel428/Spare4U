package com.example.spare4uadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.spare4uadmin.Constant.Constants;
import com.example.spare4uadmin.Model.ItemMasterModel;
import com.example.spare4uadmin.Model.SideMasterModel;
import com.example.spare4uadmin.ViewHolder.ItemMasterViewHolder;
import com.example.spare4uadmin.ViewHolder.SideMasterViewHolder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemMasterPage extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton fab;
    EditText side_description_edt;
    List<ItemMasterModel> itemMasterModels;
    ArrayList<String> item_id_list,item_name_list,make_id_list,make_name_list,year_id_list,year_desc_list;
    String authToken,selected_item_id = "",selected_item_name = "";
    String fetch_item_url = Constants.MAIN_URL + "api/item/list";
    String submit_item_url = Constants.MAIN_URL + "api/side/create";
    String update_side_url = Constants.MAIN_URL + "api/side/update";
    ProgressDialog dialog;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    boolean check_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_master_page);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading Data...");
        dialog.setCancelable(false);
        dialog.show();

        recyclerView = findViewById(R.id.item_main_recycler_list);
        fab = findViewById(R.id.add_item_button);

        item_id_list = new ArrayList<>();
        item_name_list = new ArrayList<>();
        make_id_list = new ArrayList<>();
        make_name_list = new ArrayList<>();
        year_id_list = new ArrayList<>();
        year_desc_list = new ArrayList<>();
        itemMasterModels = new ArrayList<>();

        preferences=getSharedPreferences("uuid",MODE_PRIVATE);
        editor = preferences.edit();
        authToken = preferences.getString("authToken","");

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this,AddItemMasterPage.class);
            startActivity(intent);
        });

        FetchData();
    }

    private void FetchData() {
        StringRequest request = new StringRequest(Request.Method.GET, fetch_item_url, response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("success").equals("true"))
                {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject v = jsonArray.getJSONObject(i);
                        ItemMasterModel itemMasterModel = new ItemMasterModel();
                        itemMasterModel.setItem_id(v.getString("ITEM_ID"));
                        itemMasterModel.setItem_name(v.getString("ITEM_NAME"));
                        itemMasterModel.setMake_name(v.getJSONObject("makeMaster").getString("company_name"));
                        itemMasterModel.setYear_desc(v.getJSONObject("yearMaster").getString("YEAR_DESC"));

                        item_id_list.add(v.getString("ITEM_ID"));
                        item_name_list.add(v.getString("ITEM_NAME"));
                        make_name_list.add(v.getJSONObject("makeMaster").getString("company_name"));
                        year_desc_list.add(v.getJSONObject("yearMaster").getString("YEAR_DESC"));

                        itemMasterModels.add(itemMasterModel);


                    }
                    ItemMasterViewHolder adapter = new ItemMasterViewHolder(itemMasterModels,this);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(adapter);
                    recyclerView.setHasFixedSize(true);

                    dialog.dismiss();
                }else {
                    dialog.dismiss();
                    Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                    if (jsonObject.getString("status").equals("400"))
                    {
                        CheckTokenPage checkTokenPage = new CheckTokenPage(this);
                        check_status = checkTokenPage.CheckToken();
                        if (check_status)
                        {
                            FetchData();
                        }
                    }
                }
            } catch (JSONException e) {
                dialog.dismiss();
                Toast.makeText(this, String.valueOf(e), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        },error -> {
            dialog.dismiss();
            Toast.makeText(this, String.valueOf(error), Toast.LENGTH_SHORT).show();
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> params = new HashMap<String, String>();
                params.put("Authorization",authToken);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(20000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(getResources().getString(R.string.edit)))
        {
            editor.putString("selected_item_id",item_id_list.get(item.getOrder()));
            Intent intent = new Intent(this,EditItemMasterPage.class);
            startActivity(intent);

        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this,HomePage.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}