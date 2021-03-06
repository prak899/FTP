package in.pm.ftp.FTP;
/**
 * Created by Prakash on 6/13/2021.
 */

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.jibble.simpleftp.SimpleFTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import in.pm.ftp.R;
import in.pm.ftp.Spalsh;


public class ImageUploadActivity extends AppCompatActivity {

    Bitmap thumbnailImage;
    String filePath = "";
    public static String strFileName;
    private final int THUMBSIZE = 600;
    private String filePathpic;
    ImagePickUpDialog dialog;
    private static final String TAG = "XfileX";
    File file;
    ImageView civProfilePic;
    Button uploadimage;
    TextInputEditText editTextImageName;


    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 300;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();*/
        setContentView(R.layout.activity_image_upload);

        civProfilePic = (ImageView) findViewById(R.id.profile_pic);
        editTextImageName = findViewById(R.id.imageName);
        uploadimage =  findViewById(R.id.uploadbtn);
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);

        dialog = new ImagePickUpDialog(this);
        civProfilePic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                checkPermission(Manifest.permission.CAMERA, PERMISSION_REQUEST_CODE);
                dialog.show();

            }
        });
        uploadimage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e("click","upload");
                if (editTextImageName.getText().toString().length() >0) {
                    SimpleDateFormat s = new SimpleDateFormat("dd-MM-yy-hh-mm-ss");
                    String Date = s.format(new Date());

                    strFileName = editTextImageName.getText().toString();

                    UploadFile async = new UploadFile();
                    async.execute();

                }
                else
                {
                    Toast.makeText(ImageUploadActivity.this, "Enter Image Name", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    Bitmap fbAvatarBitmap = null;
    public static String getPath(Uri uri, Activity context) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    public static File saveBitmapToFile(File file) {
        try {

// BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 2;
// factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
//Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

// The new size we want to scale to
            final int REQUIRED_SIZE = 75;

// Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);
            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

// here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 90
                    , outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {

        super.onActivityResult(requestCode, resultCode, returnedIntent);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {

                case 1:

                    if (returnedIntent != null) {

                        Uri selectedImage = returnedIntent.getData();
                        filePath = getPath(selectedImage, this);
                        filePathpic = getPath(selectedImage, this);
                        Log.e("filepath", "" + filePath);


                        if (filePath != null) {
                            file = new File(filePath);
                            saveBitmapToFile(file);
                            //Util.saveBitmapToFile(file);
                            Log.e("Selected Image Path", "" + selectedImage);

                            thumbnailImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(filePath),
                                    THUMBSIZE, THUMBSIZE);
                            long length = file.length();

                            civProfilePic.setImageBitmap(thumbnailImage);


                        } else if (filePath == null) {
                            Toast.makeText(this, "you have selected image from cloud please download the image and  come back",
                                    Toast.LENGTH_LONG).show();
                        }


                    }
                    break;
                case 2:

                    Log.e("inCase2", "in");
                    /*********** Load Captured Image And Data Start ****************/
                    int targetW = civProfilePic.getWidth();
                    int targetH = civProfilePic.getHeight();

                    // Get the dimensions of the bitmap
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inJustDecodeBounds = true;
                    if (ImagePickUpDialog.strAbsolutePath.isEmpty()) {
                        BitmapFactory.decodeFile(ImagePickUpDialog.strAbsolutePath, bmOptions);
                    }

                    int photoW = bmOptions.outWidth;
                    int photoH = bmOptions.outHeight;

                    // Determine how much to scale down the image
                    int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

                    // Decode the image file into a Bitmap sized to fill the View
                    bmOptions.inJustDecodeBounds = false;
                    bmOptions.inSampleSize = scaleFactor;
                    bmOptions.inPurgeable = true;

                    civProfilePic.setVisibility(View.VISIBLE);
                    if (!ImagePickUpDialog.strAbsolutePath.isEmpty()) {

                        Bitmap bitmap = BitmapFactory.decodeFile(ImagePickUpDialog.strAbsolutePath, bmOptions);


                        civProfilePic.setImageBitmap(bitmap);
                    }
                    filePath = ImagePickUpDialog.strAbsolutePath;
                    filePathpic = ImagePickUpDialog.strAbsolutePath;
                    Log.e("Selected Image Path cam", "" + filePathpic);
                    break;
            }
        }
    }

    class UploadFile extends AsyncTask<File, Integer, String> {
        ProgressDialog dialog;

        @Override
        protected String doInBackground(File... params) {
            Log.e("FTP","doInBackground");

            try {
                SimpleFTP ftp = new SimpleFTP();

                // Connect to an FTP server on port 21.
                ftp.connect("your addres", 21, "username", "password");

                ftp.bin();
                ftp.cwd("service.cosmicvas.com/ReciengEntry/RecievingIMG/");
                Log.d(TAG, "doInBackground: " + strFileName+".jpg");
                ftp.stor(new FileInputStream(new File(filePath)), strFileName + ".jpg");
                ftp.disconnect();


            } catch (Exception e) {
                e.printStackTrace();

                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.rootLayout), "Something Went Wrong...", Snackbar.LENGTH_LONG);
                snackbar.show();
                dialog.dismiss();
            }
            return null;
        }



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e("FTP","onPreExecute");
            dialog = new ProgressDialog(ImageUploadActivity.this);
            dialog.setMessage("Please wait...");
            dialog.setCancelable(false);
            dialog.show();

        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            Log.e("FTP","onPostExecute");
            super.onPostExecute(result);
            dialog.dismiss();
            Log.d("XuploadX", "onPostExecute: Upload success"+result);
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.rootLayout), "Upload successfully", Snackbar.LENGTH_LONG);
            snackbar.show();
            civProfilePic.setBackgroundResource(R.drawable.ftp);
            editTextImageName.setText(null);
        }

    }


    // Function to check and request permission
    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] { permission }, requestCode);
        }
        else {
            //Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}