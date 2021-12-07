package com.example.spare4uadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.spare4uadmin.Constant.Constants;
import com.example.spare4uadmin.Model.MakeMasterModel;
import com.example.spare4uadmin.Model.YearMasterModel;
import com.example.spare4uadmin.ViewHolder.MakeMasterViewHolder;
import com.example.spare4uadmin.ViewHolder.YearMasterViewHolder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YearMasterPage extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton fab;
    EditText year_description_edt;
    List<YearMasterModel> yearMasterModels;
    ArrayList<String> year_id_list,year_description_list;
    String authToken,selected_year_id = "",selected_year_description = "";
    String fetch_year_url = Constants.MAIN_URL + "api/years/list";
    String submit_year_url = Constants.MAIN_URL + "api/years/create";
    String update_year_url = Constants.MAIN_URL + "api/years/update";
    ProgressDialog dialog;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    boolean check_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_year_master_page);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading Data...");
        dialog.setCancelable(false);
        dialog.show();

        recyclerView = findViewById(R.id.year_main_recycler_list);
        fab = findViewById(R.id.add_year_button);

        year_id_list = new ArrayList<>();
        year_description_list = new ArrayList<>();
        yearMasterModels = new ArrayList<>();

        preferences=getSharedPreferences("uuid",MODE_PRIVATE);
        editor = preferences.edit();
        authToken = preferences.getString("authToken","");
        Log.e("URL",fetch_year_url + submit_year_url + update_year_url);

        fab.setOnClickListener(v -> {

            OpenDialog();
        });

        FetchData();
    }

    private void FetchData() {
        StringRequest request = new StringRequest(Request.Method.GET, fetch_year_url, response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                Log.e("Response",response);
                if (jsonObject.getString("success").equals("true"))
                {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject v = jsonArray.getJSONObject(i);
                        YearMasterModel yearMasterModel = new YearMasterModel();
                        yearMasterModel.setYear_id(v.getString("YEAR_ID"));
                        yearMasterModel.setYear_description(v.getString("YEAR_DESC"));

                        year_id_list.add(v.getString("YEAR_ID"));
                        year_description_list.add(v.getString("YEAR_DESC"));

                        yearMasterModels.add(yearMasterModel);


                    }
                    YearMasterViewHolder adapter = new YearMasterViewHolder(yearMasterModels,this);
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

    private void OpenDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).setPositiveButton(getResources().getString(R.string.submit),null).setNegativeButton(getResources().getString(R.string.cancel),null).create();

        alertDialog.setTitle("Enter The Details.");
        alertDialog.setCancelable(false);

        ViewGroup viewGroup = findViewById(android.R.id.content);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.year_master_dialog_design, viewGroup, false);

        alertDialog.setView(dialogView);

        year_description_edt = dialogView.findViewById(R.id.year_description_edt);

        alertDialog.setOnShowListener(dialog1 -> {
            Button positionButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            positionButton.setOnClickListener(v -> {
                if (year_description_edt.getText().toString().isEmpty())
                {
                    year_description_edt.setError(getResources().getString(R.string.enter_year_description));
                    dialog.dismiss();
                }else {
                    dialog = new ProgressDialog(this);
                    dialog.setMessage("Loading Data...");
                    dialog.setCancelable(false);
                    dialog.show();

                    SubmitData();
                }
            });

            negativeButton.setOnClickListener(v -> {
                alertDialog.dismiss();
            });
        });

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;

        alertDialog.show();
    }

    private void OpenEditDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).setPositiveButton(getResources().getString(R.string.submit),null).setNegativeButton(getResources().getString(R.string.cancel),null).create();

        alertDialog.setTitle("Enter The Details.");
        alertDialog.setCancelable(false);

        ViewGroup viewGroup = findViewById(android.R.id.content);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.year_master_dialog_design, viewGroup, false);

        alertDialog.setView(dialogView);

        year_description_edt = dialogView.findViewById(R.id.year_description_edt);

        year_description_edt.setText(selected_year_description);

        alertDialog.setOnShowListener(dialog1 -> {
            Button positionButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            positionButton.setOnClickListener(v -> {
                if (year_description_edt.getText().toString().isEmpty())
                {
                    year_description_edt.setError(getResources().getString(R.string.enter_year_description));
                    dialog.dismiss();
                }else {
                    dialog = new ProgressDialog(this);
                    dialog.setMessage("Loading Data...");
                    dialog.setCancelable(false);
                    dialog.show();

                    SubmitEditData();
                }
            });

            negativeButton.setOnClickListener(v -> {
                alertDialog.dismiss();
            });
        });

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;

        alertDialog.show();
    }

    private void SubmitData() {
        StringRequest request = new StringRequest(Request.Method.POST, submit_year_url, response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("success").equals("true")) {

                    Toast.makeText(this, "Year Added Successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(YearMasterPage.this, YearMasterPage.class);
                    startActivity(intent);

                    dialog.dismiss();
                } else {
                    Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            } catch (JSONException e) {
                dialog.dismiss();
                Toast.makeText(this, String.valueOf(e), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }, error -> {
            dialog.dismiss();
            Toast.makeText(this, String.valueOf(error), Toast.LENGTH_SHORT).show();
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> params = new HashMap<String, String>();
                params.put("desc",year_description_edt.getText().toString());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Authorization", authToken);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(request);
    }

    private void SubmitEditData() {
        StringRequest request = new StringRequest(Request.Method.POST, update_year_url, response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("success").equals("true")) {

                    Toast.makeText(this, "Year Updated Successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(YearMasterPage.this, YearMasterPage.class);
                    startActivity(intent);

                    dialog.dismiss();
                } else {
                    Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            } catch (JSONException e) {
                dialog.dismiss();
                Toast.makeText(this, String.valueOf(e), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }, error -> {
            dialog.dismiss();
            Toast.makeText(this, String.valueOf(error), Toast.LENGTH_SHORT).show();
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> params = new HashMap<String, String>();
                params.put("id",selected_year_id);
                params.put("desc",year_description_edt.getText().toString());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Authorization", authToken);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(getResources().getString(R.string.edit)))
        {
            selected_year_id = year_id_list.get(item.getOrder());
            selected_year_description = year_description_list.get(item.getOrder());

            OpenEditDialog();
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