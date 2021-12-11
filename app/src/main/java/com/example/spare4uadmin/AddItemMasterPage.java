package com.example.spare4uadmin;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import android.view.View;
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
import com.example.spare4uadmin.Constant.Constants;
import com.example.spare4uadmin.Model.ItemMasterModel;
import com.example.spare4uadmin.ViewHolder.ItemMasterViewHolder;

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

public class AddItemMasterPage extends AppCompatActivity {

    AutoCompleteTextView make_drop_down,year_drop_down,side_drop_down,group_drop_down;
    EditText item_name_edt,item_description_edt,item_price_edt,item_video_url_edt;
    ImageView image_1_img,image_2_img,image_3_img,image_4_img,image_5_img;
    Button image_1_btn,image_2_btn,image_3_btn,image_4_btn,image_5_btn,submit_btn;
    ArrayAdapter make_adapter,year_adapter,side_adapter,group_adapter;
    ArrayList<String> make_id_list,make_name_list,year_id_list,year_name_list;
    ArrayList<String> side_id_list,side_name_list,group_id_list,group_name_list;
    String selected_make_id,selected_year_id,selected_side_id,selected_group_id;
    Uri picUri1,picUri2,picUri3,picUri4,picUri5;
    String mediaPath1 = "",mediaPath2 = "",mediaPath3 = "",mediaPath4 = "",mediaPath5 = "";
    String fetch_make_url = Constants.MAIN_URL + "api/company/list";
    String fetch_year_url = Constants.MAIN_URL + "api/years/list";
    String fetch_side_url = Constants.MAIN_URL + "api/side/list";
    String fetch_group_url = Constants.MAIN_URL + "api/group/list";
    String submit_data_url = Constants.MAIN_URL + "api/item/create";
    String authToken;
    ProgressDialog dialog;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    boolean check_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item_master_page);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading Data...");
        dialog.setCancelable(false);
        dialog.show();

        make_drop_down = findViewById(R.id.make_name_drop_down);
        year_drop_down = findViewById(R.id.year_name_drop_down);
        side_drop_down = findViewById(R.id.side_name_drop_down);
        group_drop_down = findViewById(R.id.group_name_drop_down);

        item_name_edt = findViewById(R.id.item_name_edt);
        item_description_edt = findViewById(R.id.item_description_edt);
        item_price_edt = findViewById(R.id.item_price_edt);
        item_video_url_edt = findViewById(R.id.item_video_url_edt);

        image_1_img = findViewById(R.id.image_1_image);
        image_2_img = findViewById(R.id.image_2_image);
        image_3_img = findViewById(R.id.image_3_image);
        image_4_img = findViewById(R.id.image_4_image);
        image_5_img = findViewById(R.id.image_5_image);

        image_1_btn = findViewById(R.id.image_1_btn);
        image_2_btn = findViewById(R.id.image_2_btn);
        image_3_btn = findViewById(R.id.image_3_btn);
        image_4_btn = findViewById(R.id.image_4_btn);
        image_5_btn = findViewById(R.id.image_5_btn);
        submit_btn = findViewById(R.id.item_master_new_btn);

        make_id_list = new ArrayList<>();
        make_name_list = new ArrayList<>();
        year_id_list = new ArrayList<>();
        year_name_list = new ArrayList<>();
        side_id_list = new ArrayList<>();
        side_name_list = new ArrayList<>();
        group_id_list = new ArrayList<>();
        group_name_list = new ArrayList<>();

        preferences=getSharedPreferences("uuid",MODE_PRIVATE);
        editor = preferences.edit();
        authToken = preferences.getString("authToken","");

        make_drop_down.setOnItemClickListener((parent, view, position, id) -> {
            make_drop_down.showDropDown();
            selected_make_id = make_id_list.get(position);
        });

        make_drop_down.setOnClickListener(v -> {
            make_drop_down.showDropDown();
        });

        year_drop_down.setOnItemClickListener((parent, view, position, id) -> {
            year_drop_down.showDropDown();
            selected_year_id = year_id_list.get(position);
        });

        year_drop_down.setOnClickListener(v -> {
            year_drop_down.showDropDown();
        });

        side_drop_down.setOnItemClickListener((parent, view, position, id) -> {
            side_drop_down.showDropDown();
            selected_side_id = side_id_list.get(position);
        });

        side_drop_down.setOnClickListener(v -> {
            side_drop_down.showDropDown();
        });

        group_drop_down.setOnItemClickListener((parent, view, position, id) -> {
            group_drop_down.showDropDown();
            selected_group_id = group_id_list.get(position);
        });

        group_drop_down.setOnClickListener(v -> {
            group_drop_down.showDropDown();
        });

        image_1_btn.setOnClickListener(v -> {
            startActivityForResult(getPickImageChooserIntent(), 1);
        });

        image_2_btn.setOnClickListener(v -> {
            startActivityForResult(getPickImageChooserIntent(), 2);
        });

        image_3_btn.setOnClickListener(v -> {
            startActivityForResult(getPickImageChooserIntent(), 3);
        });

        image_4_btn.setOnClickListener(v -> {
            startActivityForResult(getPickImageChooserIntent(), 4);
        });

        image_5_btn.setOnClickListener(v -> {
            startActivityForResult(getPickImageChooserIntent(), 5);
        });

        submit_btn.setOnClickListener(v -> {
            dialog = new ProgressDialog(this);
            dialog.setMessage("Loading Data...");
            dialog.setCancelable(false);
            dialog.show();

            if (item_name_edt.getText().toString().isEmpty())
            {
                item_name_edt.setError(getResources().getString(R.string.enter_item_name));
                dialog.dismiss();
            }else {
                SubmitData();
            }
        });

        FetchMakeData();
    }

    private void SubmitData() {
        StringRequest request = new StringRequest(Request.Method.POST, submit_data_url, response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("success").equals("true")) {

                    Toast.makeText(this, "Item Added Successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, ItemMasterPage.class);
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
                params.put("companyid",selected_make_id);
                params.put("groupid",selected_group_id);
                params.put("yearid",selected_year_id);
                params.put("sideid",selected_side_id);
                params.put("itemname",item_name_edt.getText().toString());
                params.put("itemdesc",item_description_edt.getText().toString());
                params.put("itemprice",item_price_edt.getText().toString());
                params.put("image1",mediaPath1);
                params.put("image2",mediaPath2);
                params.put("image3",mediaPath3);
                params.put("image4",mediaPath4);
                params.put("image5",mediaPath5);
                params.put("videourl",item_video_url_edt.getText().toString());
                params.put("auth_data","");
                params.put("auth_user","");
                params.put("suth_datetime","");
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

    private void FetchMakeData() {
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

                        make_id_list.add(v.getString("ID"));
                        make_name_list.add(v.getString("MAKE_NAME"));

                    }
                    make_adapter = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,make_name_list);
                    make_drop_down.setAdapter(make_adapter);
                    make_drop_down.setCursorVisible(false);

                    FetchYearData();


                }else {
                    dialog.dismiss();
                    Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
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

    private void FetchYearData() {
        StringRequest request = new StringRequest(Request.Method.GET, fetch_year_url, response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("success").equals("true"))
                {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject v = jsonArray.getJSONObject(i);

                        year_id_list.add(v.getString("YEAR_ID"));
                        year_name_list.add(v.getString("YEAR_DESC"));

                    }
                    year_adapter = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,year_name_list);
                    year_drop_down.setAdapter(year_adapter);
                    year_drop_down.setCursorVisible(false);

                    FetchSideData();


                }else {
                    dialog.dismiss();
                    Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
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

    private void FetchSideData() {
        StringRequest request = new StringRequest(Request.Method.GET, fetch_side_url, response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("success").equals("true"))
                {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject v = jsonArray.getJSONObject(i);

                        side_id_list.add(v.getString("SIDE_ID"));
                        side_name_list.add(v.getString("SIDE_DESC"));

                    }
                    side_adapter = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,side_name_list);
                    side_drop_down.setAdapter(side_adapter);
                    side_drop_down.setCursorVisible(false);

                    FetchGroupData();


                }else {
                    dialog.dismiss();
                    Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
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

    private void FetchGroupData() {
        StringRequest request = new StringRequest(Request.Method.GET, fetch_group_url, response -> {
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("success").equals("true"))
                {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject v = jsonArray.getJSONObject(i);

                        group_id_list.add(v.getString("GROUP_ID"));
                        group_name_list.add(v.getString("GROUP_DESC"));

                    }
                    group_adapter = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,group_name_list);
                    group_drop_down.setAdapter(group_adapter);
                    group_drop_down.setCursorVisible(false);

                    dialog.dismiss();


                }else {
                    dialog.dismiss();
                    Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
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

        if (requestCode == 1 && resultCode == RESULT_OK) {

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

                    image_1_img.setImageBitmap(myBitmap);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 50,baos);
                    byte[] b = baos.toByteArray();
                    mediaPath1 = Base64.encodeToString(b, Base64.DEFAULT);

                } catch (IOException e) {
                    Log.e("Catch", String.valueOf(e));
                    e.printStackTrace();
                }
            }
        }else if (requestCode == 2 && resultCode == RESULT_OK) {

            if (getPickImageResultUri(data) != null) {
                Bitmap myBitmap = null;
                picUri2 = getPickImageResultUri(data);
                try {
                    if (picUri2.getPath().contains(getExternalCacheDir().getPath()))
                    {
                        myBitmap = checkImageSize(this,picUri2);


                    }else {
                        myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri2);

                    }

                    image_2_img.setImageBitmap(myBitmap);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 50,baos);
                    byte[] b = baos.toByteArray();
                    mediaPath2 = Base64.encodeToString(b, Base64.DEFAULT);

                } catch (IOException e) {
                    Log.e("Catch", String.valueOf(e));
                    e.printStackTrace();
                }
            }
        }else if (requestCode == 3 && resultCode == RESULT_OK) {

            if (getPickImageResultUri(data) != null) {
                Bitmap myBitmap = null;
                picUri3 = getPickImageResultUri(data);
                try {
                    if (picUri3.getPath().contains(getExternalCacheDir().getPath()))
                    {
                        myBitmap = checkImageSize(this,picUri3);


                    }else {
                        myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri3);

                    }

                    image_3_img.setImageBitmap(myBitmap);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 50,baos);
                    byte[] b = baos.toByteArray();
                    mediaPath3 = Base64.encodeToString(b, Base64.DEFAULT);

                } catch (IOException e) {
                    Log.e("Catch", String.valueOf(e));
                    e.printStackTrace();
                }
            }
        }else if (requestCode == 4 && resultCode == RESULT_OK) {

            if (getPickImageResultUri(data) != null) {
                Bitmap myBitmap = null;
                picUri4 = getPickImageResultUri(data);
                try {
                    if (picUri4.getPath().contains(getExternalCacheDir().getPath()))
                    {
                        myBitmap = checkImageSize(this,picUri4);


                    }else {
                        myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri4);

                    }

                    image_4_img.setImageBitmap(myBitmap);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 50,baos);
                    byte[] b = baos.toByteArray();
                    mediaPath4 = Base64.encodeToString(b, Base64.DEFAULT);

                } catch (IOException e) {
                    Log.e("Catch", String.valueOf(e));
                    e.printStackTrace();
                }
            }
        }else if (requestCode == 5 && resultCode == RESULT_OK) {

            if (getPickImageResultUri(data) != null) {
                Bitmap myBitmap = null;
                picUri5 = getPickImageResultUri(data);
                try {
                    if (picUri5.getPath().contains(getExternalCacheDir().getPath()))
                    {
                        myBitmap = checkImageSize(this,picUri5);


                    }else {
                        myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri5);

                    }

                    image_5_img.setImageBitmap(myBitmap);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 50,baos);
                    byte[] b = baos.toByteArray();
                    mediaPath5 = Base64.encodeToString(b, Base64.DEFAULT);

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
        Intent i = new Intent(this,ItemMasterPage.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}