package com.example.spare4uadmin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.spare4uadmin.Constant.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginPage extends AppCompatActivity {

    EditText user_name_edt,password_edt;
    Button login_btn;
    String login_db_url = Constants.MAIN_URL + "api/user/auth/login";
    String user_name;
    ProgressDialog dialog;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();

        user_name_edt = findViewById(R.id.user_name_edt);
        password_edt = findViewById(R.id.password_edt);

        login_btn = findViewById(R.id.login_btn);

        preferences=getSharedPreferences("uuid",MODE_PRIVATE);
        editor = preferences.edit();
        user_name = preferences.getString("user_name_sp","");
        Log.e("Token",preferences.getString("Token",""));

        if (!user_name.isEmpty())
        {
            Intent intent = new Intent(LoginPage.this,HomePage.class);
            startActivity(intent);
        }

        login_btn.setOnClickListener(v -> {

            dialog = new ProgressDialog(this);
            dialog.setMessage("Checking...");
            dialog.setCancelable(false);
            dialog.show();

            if (user_name_edt.getText().toString().isEmpty())
            {
                user_name_edt.setError("Please Enter Username.");
                dialog.dismiss();
            }else if (password_edt.getText().toString().isEmpty())
            {
                password_edt.setError("Please Enter Password");
                dialog.dismiss();
            }else {
                SubmitData();
            }
        });
    }

    private void SubmitData() {
        StringRequest request = new StringRequest(Request.Method.POST, login_db_url, response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("success").equals("true"))
                {
                    editor.putString("user_name_sp",user_name_edt.getText().toString()).apply();
                    editor.putString("authToken",jsonObject.getJSONObject("token").getString("authToken")).apply();
                    editor.putString("refreshtoken",jsonObject.getJSONObject("token").getString("refreshtoken")).apply();
                    Toast.makeText(this, "Login Successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginPage.this,HomePage.class);
                    startActivity(intent);

                    dialog.dismiss();
                }else {
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
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> params = new HashMap<String, String>();
                params.put("mobile",user_name_edt.getText().toString());
                params.put("password",password_edt.getText().toString());
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(5000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}