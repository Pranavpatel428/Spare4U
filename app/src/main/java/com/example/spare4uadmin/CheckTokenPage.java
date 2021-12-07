package com.example.spare4uadmin;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.spare4uadmin.Constant.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CheckTokenPage {

    String authToken,refreshToken;
    String check_token_url;
    Context context;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    boolean check_status;

    public CheckTokenPage(Context context) {
        preferences = context.getSharedPreferences("uuid",MODE_PRIVATE);
        editor = preferences.edit();
        this.context = context;
        check_token_url = Constants.MAIN_URL + "api/user/auth/refresh";
        authToken = preferences.getString("authToken","");
        refreshToken = preferences.getString("refreshtoken","");
        CheckToken();
    }

    public boolean CheckToken() {
        StringRequest request = new StringRequest(Request.Method.POST, check_token_url,response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("success").equals("true"))
                {
                    editor.putString("authToken",jsonObject.getString("authtoken")).apply();
                    check_status = true;
                }else {
                    check_status = false;
                    if (jsonObject.getString("status").equals("400"))
                    {
                        Toast.makeText(context, "Token Expire. Login Again.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(context,LoginPage.class);
                        context.startActivity(intent);
                    }else {
                        Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (JSONException e) {
                check_status = false;
                Toast.makeText(context, String.valueOf(e), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }, error -> {
            check_status = false;
            Toast.makeText(context, String.valueOf(error), Toast.LENGTH_SHORT).show();
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> params = new HashMap<String, String>();
                params.put("authtoken",authToken);
                params.put("reftoken",refreshToken);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(5000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(context).add(request);
        return check_status;
    }
}
