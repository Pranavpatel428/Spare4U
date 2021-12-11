package com.example.spare4uadmin;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.spare4uadmin.Constant.Constants;
import com.example.spare4uadmin.Model.CountryModel;
import com.example.spare4uadmin.Model.MakeMasterModel;
import com.example.spare4uadmin.ViewHolder.CountryViewHolder;
import com.example.spare4uadmin.ViewHolder.MakeMasterViewHolder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakeMasterPage extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton fab;
    EditText company_name_edt,company_url_edt;
    AutoCompleteTextView country_name_dropdown;
    ImageView company_logo_view;
    Button select_company_logo;
    List<MakeMasterModel> makeMasterModels;
    ArrayAdapter country_adapter;
    HashMap<String,String> temp_country_list;
    ArrayList<String> make_country_id_list;
    ArrayList<String> make_id_list,company_name_list,country_id_list,country_name_list,company_logo_list,company_url_list;
    ProgressDialog dialog;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String edit_make_id = "",edit_company_name = "",edit_country_id = "",edit_country_name = "",edi_company_url = "",edit_company_logo_url = "";
    String authToken,selected_country_id_drop = "",selected_country_name_drop = "",selected_company_name,selected_make_id,selected_company_logo;
    boolean check_status;
    Uri picUri,picUri1;
    String mediaPath = "",mediaPath1 = "";
    String fetch_make_url = Constants.MAIN_URL + "api/company/list";
    String fetch_country_list_url = Constants.MAIN_URL + "api/country/list";
    String submit_make_url = Constants.MAIN_URL + "api/company/create";
    String update_make_url = Constants.MAIN_URL + "api/company/update";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_master_page);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading Data...");
        dialog.setCancelable(false);
        dialog.show();

        recyclerView = findViewById(R.id.make_main_recycler_list);
        fab = findViewById(R.id.add_make_button);

        make_id_list = new ArrayList<>();
        make_country_id_list = new ArrayList<>();
        company_name_list = new ArrayList<>();
        country_id_list = new ArrayList<>();
        country_name_list = new ArrayList<>();
        company_logo_list = new ArrayList<>();
        company_url_list = new ArrayList<>();
        makeMasterModels = new ArrayList<>();
        temp_country_list = new HashMap<>();

        preferences=getSharedPreferences("uuid",MODE_PRIVATE);
        editor = preferences.edit();
        authToken = preferences.getString("authToken","");

        fab.setOnClickListener(v -> {
            dialog = new ProgressDialog(this);
            dialog.setMessage("Loading Data...");
            dialog.setCancelable(false);
            dialog.show();

            OpenDialog();
        });
        FetchCountryData();
        FetchData();
    }

    private void FetchCountryData() {
        StringRequest request = new StringRequest(Request.Method.GET, fetch_country_list_url, response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("success").equals("true"))
                {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject v = jsonArray.getJSONObject(i);

                        country_id_list.add(v.getString("COUNTRY_ID"));
                        country_name_list.add(v.getString("COUNTRY_NAME"));

                        temp_country_list.put(v.getString("COUNTRY_ID"),v.getString("COUNTRY_NAME"));

                    }

                    FetchData();
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
        request.setRetryPolicy(new DefaultRetryPolicy(5000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(request);
    }

    private void FetchData() {
        StringRequest request = new StringRequest(Request.Method.GET, fetch_make_url, response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("success").equals("true"))
                {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject v = jsonArray.getJSONObject(i);
                        MakeMasterModel makeMasterModel = new MakeMasterModel();
                        makeMasterModel.setMake_id(v.getString("ID"));
                        makeMasterModel.setCompany_logo_url(jsonObject.getString("imagerooturl")+v.getString("COMPANY_LOGO_URL"));
                        makeMasterModel.setCompany_name(v.getString("MAKE_NAME"));
                        makeMasterModel.setCountry_id(v.getString("COUNTRY_ID"));
                        makeMasterModel.setCompany_url(v.getString("COMPANY_URL"));

                        make_id_list.add(v.getString("ID"));
                        make_country_id_list.add(v.getString("COUNTRY_ID"));
                        company_name_list.add(v.getString("MAKE_NAME"));
                        company_url_list.add(v.getString("COMPANY_URL"));
                        company_logo_list.add(jsonObject.getString("imagerooturl")+ v.getString("COMPANY_LOGO_URL"));

                        makeMasterModels.add(makeMasterModel);


                    }
                    MakeMasterViewHolder adapter = new MakeMasterViewHolder(makeMasterModels,this);
                    recyclerView.setLayoutManager(new GridLayoutManager(this,2));
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

        View dialogView = LayoutInflater.from(this).inflate(R.layout.make_master_dialog_design, viewGroup, false);

        alertDialog.setView(dialogView);

        company_name_edt = dialogView.findViewById(R.id.company_name_edt);
        company_url_edt = dialogView.findViewById(R.id.company_url_edt);
        country_name_dropdown = dialogView.findViewById(R.id.country_dropdown_edt);
        select_company_logo = dialogView.findViewById(R.id.company_logo_btn);
        company_logo_view = dialogView.findViewById(R.id.company_logo_image_view);

        country_name_dropdown.setOnItemClickListener((parent, view, position, id) -> {
            country_name_dropdown.showDropDown();
            selected_country_id_drop = country_id_list.get(position);
            selected_country_name_drop = country_name_list.get(position);
        });

        country_name_dropdown.setOnClickListener(v -> {
            country_name_dropdown.showDropDown();
        });

        select_company_logo.setOnClickListener(v -> {
            startActivityForResult(getPickImageChooserIntent(), 0);
        });

        country_adapter = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,country_name_list);
        country_name_dropdown.setAdapter(country_adapter);
        country_name_dropdown.setCursorVisible(false);

        alertDialog.setOnShowListener(dialog1 -> {
            Button positionButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            positionButton.setOnClickListener(v -> {
                if (company_name_edt.getText().toString().isEmpty())
                {
                    company_name_edt.setError(getResources().getString(R.string.entry_company_name));
                    dialog.dismiss();
                }else if (selected_country_id_drop.equals(""))
                {
                    dialog.dismiss();
                    country_name_dropdown.setError(getResources().getString(R.string.select_country_name));
                }else {
                    dialog = new ProgressDialog(this);
                    dialog.setMessage("Loading Data...");
                    dialog.setCancelable(false);
                    dialog.show();

                    Log.e("Path",mediaPath);

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

        View dialogView = LayoutInflater.from(this).inflate(R.layout.make_master_dialog_design, viewGroup, false);

        alertDialog.setView(dialogView);

        company_name_edt = dialogView.findViewById(R.id.company_name_edt);
        company_url_edt = dialogView.findViewById(R.id.company_url_edt);
        country_name_dropdown = dialogView.findViewById(R.id.country_dropdown_edt);
        select_company_logo = dialogView.findViewById(R.id.company_logo_btn);
        company_logo_view = dialogView.findViewById(R.id.company_logo_image_view);

        company_name_edt.setText(edit_company_name);
        company_url_edt.setText(edi_company_url);
        country_name_dropdown.setText(edit_country_name);
        Glide.with(this).load(edit_company_logo_url).into(company_logo_view);

        country_name_dropdown.setOnItemClickListener((parent, view, position, id) -> {
            country_name_dropdown.showDropDown();
            selected_country_id_drop = country_id_list.get(position);
            selected_country_name_drop = country_name_list.get(position);
        });

        country_name_dropdown.setOnClickListener(v -> {
            country_name_dropdown.showDropDown();
        });

        select_company_logo.setOnClickListener(v -> {
            startActivityForResult(getPickImageChooserIntent(), 1);
        });

        country_adapter = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,country_name_list);
        country_name_dropdown.setAdapter(country_adapter);
        country_name_dropdown.setCursorVisible(false);

        alertDialog.setOnShowListener(dialog1 -> {
            Button positionButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            positionButton.setOnClickListener(v -> {
                if (company_name_edt.getText().toString().isEmpty())
                {
                    company_name_edt.setError(getResources().getString(R.string.entry_company_name));
                    dialog.dismiss();
                }else if (edit_country_id.equals(""))
                {
                    dialog.dismiss();
                    country_name_dropdown.setError(getResources().getString(R.string.select_country_name));
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
        StringRequest request = new StringRequest(Request.Method.POST, submit_make_url, response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("success").equals("true")) {

                    Toast.makeText(this, "Country Name Added Successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MakeMasterPage.this, MakeMasterPage.class);
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
                params.put("companyname",company_name_edt.getText().toString());
                params.put("coutnryid",selected_country_id_drop);
                params.put("companylogo",mediaPath);
                params.put("companyurl",company_url_edt.getText().toString());
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
        StringRequest request = new StringRequest(Request.Method.POST, update_make_url, response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("success").equals("true")) {

                    Toast.makeText(this, "Country Name Updated Successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MakeMasterPage.this, MakeMasterPage.class);
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
                params.put("id",edit_make_id);
                params.put("companyname",company_name_edt.getText().toString());
                params.put("coutnryid",edit_country_id);
                params.put("companylogo",mediaPath1);
                params.put("companyurl",company_url_edt.getText().toString());
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
            edit_make_id = make_id_list.get(item.getOrder());
            edit_country_id = make_country_id_list.get(item.getOrder());
            edit_country_name = temp_country_list.get(edit_country_id);
            edit_company_name = company_name_list.get(item.getOrder());
            edi_company_url = company_url_list.get(item.getOrder());
            edit_company_logo_url = company_logo_list.get(item.getOrder());

            OpenEditDialog();


        }
        return super.onContextItemSelected(item);
    }

    public Intent getPickImageChooserIntent() {

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        // collect all camera intents
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File f = new File(getExternalCacheDir().getPath(), "Company_Logo.png");
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this,getApplicationContext().getPackageName() + ".provider",f));
        allIntents.add(captureIntent);

        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        Intent mainIntent = new Intent();
        for (Intent intent : allIntents)
        {
            mainIntent = intent;
        }

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalCacheDir();

        if (getImage != null) {
                outputFileUri = Uri.fromFile(new File(getImage.getPath(), "Company_Logo.png"));
            //Log.e("U",String.valueOf(outputFileUri));
        }
        return outputFileUri;
    }

    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK) {

            if (getPickImageResultUri(data) != null) {
                Bitmap myBitmap = null;
                picUri = getPickImageResultUri(data);
                try {
                    if (picUri.getPath().contains(getExternalCacheDir().getPath()))
                    {
                        myBitmap = checkImageSize(this,picUri);


                    }else {
                        myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri);

                    }

                    company_logo_view.setImageBitmap(myBitmap);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 50,baos);
                    byte[] b = baos.toByteArray();
                    mediaPath = Base64.encodeToString(b, Base64.DEFAULT);

                } catch (IOException e) {
                    Log.e("Catch", String.valueOf(e));
                    e.printStackTrace();
                }
            }
        }else if (requestCode == 1 && resultCode == RESULT_OK) {

            if (getPickImageResultUri(data) != null) {
                Bitmap myBitmap = null;
                picUri1 = getPickImageResultUri(data);
                try {
                    if (picUri1.getPath().contains(getExternalCacheDir().getPath()))
                    {
                        myBitmap = checkImageSize(this,picUri1);


                    }else {
                        myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri1);

                    }

                    company_logo_view.setImageBitmap(myBitmap);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 50,baos);
                    byte[] b = baos.toByteArray();
                    mediaPath1 = Base64.encodeToString(b, Base64.DEFAULT);

                } catch (IOException e) {
                    Log.e("Catch", String.valueOf(e));
                    e.printStackTrace();
                }
            }
        }else {
            Toast.makeText(this, "You haven't picked an Image", Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap checkImageSize (Context context, Uri selectedImage) throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(img, selectedImage);
        return img;
    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {
        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this,HomePage.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}