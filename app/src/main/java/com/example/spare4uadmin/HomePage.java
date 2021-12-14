package com.example.spare4uadmin;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.spare4uadmin.Constant.Constants;
import com.example.spare4uadmin.Model.CountryModel;
import com.example.spare4uadmin.ViewHolder.CountryViewHolder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomePage extends AppCompatActivity {

    String fetch_country_url = Constants.MAIN_URL + "api/country/list";
    String submit_country_url = Constants.MAIN_URL + "api/country/create";
    String update_country_url = Constants.MAIN_URL + "api/country/update";
    EditText country_name_edt,continent_name_edt;
    DrawerLayout drawerLayout;
    RecyclerView recyclerView;
    FloatingActionButton fab;
    String authToken;
    CountryViewHolder adapter;
    List<CountryModel> countryModels;
    ArrayList<String> country_name_list,country_id_list,continent_name_list;
    ProgressDialog dialog;
    SharedPreferences preferences;
    static SharedPreferences.Editor editor;
    private ArrayList permissionsToRequest;
    private ArrayList permissions = new ArrayList();
    private final static int ALL_PERMISSIONS_RESULT = 107;
    String edit,selected_country_id,selected_country_name,selected_continent_name;
    boolean check_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();

        permissions.add(CAMERA);
        permissions.add(WRITE_EXTERNAL_STORAGE);
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(RECORD_AUDIO);
        permissionsToRequest = findUnAskedPermissions(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (permissionsToRequest.size() > 0)
                requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading Data...");
        dialog.setCancelable(false);
        dialog.show();

        drawerLayout = findViewById(R.id.drawer_layout);
        recyclerView = findViewById(R.id.country_main_recycler_list);
        fab = findViewById(R.id.add_country_button);

        preferences=getSharedPreferences("uuid",MODE_PRIVATE);
        editor = preferences.edit();
        authToken = preferences.getString("authToken","");

        country_id_list = new ArrayList<>();
        country_name_list = new ArrayList<>();
        continent_name_list = new ArrayList<>();
        countryModels = new ArrayList<>();

        fab.setOnClickListener(v -> {
            //OpenDialog();
            Intent intent = new Intent(this,DemoAudioPage.class);
            startActivity(intent);
        });

        FetchData();
    }

    private void FetchData() {
        StringRequest request = new StringRequest(Request.Method.GET, fetch_country_url, response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("success").equals("true"))
                {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject v = jsonArray.getJSONObject(i);
                        CountryModel countryModel = new CountryModel();
                        countryModel.setCountry_id(v.getString("COUNTRY_ID"));
                        countryModel.setCountry_name(v.getString("COUNTRY_NAME"));
                        countryModel.setContinent_name(v.getString("CONTINENT"));

                        country_id_list.add(v.getString("COUNTRY_ID"));
                        country_name_list.add(v.getString("COUNTRY_NAME"));
                        continent_name_list.add(v.getString("CONTINENT"));

                        countryModels.add(countryModel);

                    }
                    adapter = new CountryViewHolder(countryModels,HomePage.this);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Enter The Details.");
        alertDialog.setCancelable(false);

        ViewGroup viewGroup = findViewById(android.R.id.content);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.country_dialog_design, viewGroup, false);

        alertDialog.setView(dialogView);

        country_name_edt = dialogView.findViewById(R.id.country_name_edt);
        continent_name_edt = dialogView.findViewById(R.id.continent_name_edt);

        alertDialog.setPositiveButton(getResources().getString(R.string.submit),((dialogInterface, i) -> {
            if (country_name_edt.getText().toString().isEmpty())
            {
                country_name_edt.setError(getResources().getString(R.string.enter_country_name));
                dialog.dismiss();
            }else if (continent_name_edt.getText().toString().isEmpty())
            {
                dialog.dismiss();
                continent_name_edt.setError(getResources().getString(R.string.enter_continent_name));
            }else {
                dialog = new ProgressDialog(this);
                dialog.setMessage("Loading Data...");
                dialog.setCancelable(false);
                dialog.show();

                SubmitData();
            }
        }));

        alertDialog.setNegativeButton(getResources().getString(R.string.cancel),((dialogInterface, i) -> {
            dialogInterface.dismiss();
        }));

        AlertDialog dialog1 = alertDialog.create();

        dialog1.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;

        dialog1.show();
    }

    private void OpenEditDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Enter The Details.");
        alertDialog.setCancelable(false);

        ViewGroup viewGroup = findViewById(android.R.id.content);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.country_dialog_design, viewGroup, false);

        alertDialog.setView(dialogView);

        country_name_edt = dialogView.findViewById(R.id.country_name_edt);
        continent_name_edt = dialogView.findViewById(R.id.continent_name_edt);

        country_name_edt.setText(selected_country_name);
        continent_name_edt.setText(selected_continent_name);

        alertDialog.setPositiveButton(getResources().getString(R.string.submit),((dialogInterface, i) -> {
            if (country_name_edt.getText().toString().isEmpty())
            {
                country_name_edt.setError(getResources().getString(R.string.enter_country_name));
                dialog.dismiss();
            }else if (continent_name_edt.getText().toString().isEmpty())
            {
                dialog.dismiss();
                continent_name_edt.setError(getResources().getString(R.string.enter_continent_name));
            }else {
                dialog = new ProgressDialog(this);
                dialog.setMessage("Loading Data...");
                dialog.setCancelable(false);
                dialog.show();

                SubmitEditData();
            }
        }));

        alertDialog.setNegativeButton(getResources().getString(R.string.cancel),((dialogInterface, i) -> {
            dialogInterface.dismiss();
        }));

        AlertDialog dialog1 = alertDialog.create();

        dialog1.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;

        dialog1.show();
    }

    private void SubmitData() {
        StringRequest request = new StringRequest(Request.Method.POST, submit_country_url, response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("success").equals("true")) {

                    Toast.makeText(this, "Country Name Added Successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(HomePage.this, HomePage.class);
                    startActivity(intent);

                    dialog.dismiss();
                } else {
                    Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            } catch (JSONException e) {
                //dialog.dismiss();
                Toast.makeText(this, String.valueOf(e), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }, error -> {
            //dialog.dismiss();
            Toast.makeText(this, String.valueOf(error), Toast.LENGTH_SHORT).show();
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> params = new HashMap<String, String>();
                params.put("name",country_name_edt.getText().toString());
                params.put("continent",continent_name_edt.getText().toString());
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
        StringRequest request = new StringRequest(Request.Method.POST, update_country_url, response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("success").equals("true")) {

                    Toast.makeText(this, "Country Name Updated Successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(HomePage.this, HomePage.class);
                    startActivity(intent);

                    dialog.dismiss();
                } else {
                    Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            } catch (JSONException e) {
                //dialog.dismiss();
                Toast.makeText(this, String.valueOf(e), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }, error -> {
            //dialog.dismiss();
            Toast.makeText(this, String.valueOf(error), Toast.LENGTH_SHORT).show();
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> params = new HashMap<String, String>();
                params.put("id",selected_country_id);
                params.put("name",country_name_edt.getText().toString());
                params.put("continent",continent_name_edt.getText().toString());
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
            edit = "1";
            selected_country_id = country_id_list.get(item.getOrder());
            selected_country_name = country_name_list.get(item.getOrder());
            selected_continent_name = continent_name_list.get(item.getOrder());
            OpenEditDialog();
        }
        return super.onContextItemSelected(item);
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();
        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public void ClickMenu(View view){
        openDrawer(drawerLayout);
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void ClickLogo(View view){
        closeDrawer(drawerLayout);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)){

            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void ClickProfile(View view){
        //rediretActivity(this,MakeMasterPage.class);
    }

    public void ClickCountry(View view){
        rediretActivity(this,MakeMasterPage.class);
    }

    public void ClickYear(View view){
        rediretActivity(this,YearMasterPage.class);
    }

    public void ClickGroup(View view){
        rediretActivity(this,GroupMasterPage.class);
    }

    public void ClickSide(View view){
        rediretActivity(this,SideMasterPage.class);
    }

    public void ClickItem(View view){
        rediretActivity(this,ItemMasterPage.class);
    }

    public void ClickLogout(View view){
        logout(this);
    }

    public static void logout(final Activity activity) {
        editor.clear().apply();
        /*editor.remove("supplier_id").apply();
        editor.remove("user_name_sp").apply();
        editor.remove("Supplier_Token").apply();*/
        //editor.putString("Token",notification_token).apply();
        Toast.makeText(activity, "Logout", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(activity,LoginPage.class);
        activity.startActivity(intent);

    }

    public static void rediretActivity(Activity activity, Class aClass) {
        Intent intent = new Intent(activity,aClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}