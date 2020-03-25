package xyz.peke2.imagepickertest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.content.Intent;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.Button;
import android.view.View;
import android.net.Uri;
import android.widget.ImageView;

import java.io.Console;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_GET = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        final Button button = findViewById("ButtonImageSelector");
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
//        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        //  権限の確認ダイアログが開かなくなった(？)ので、自前で対応してみる
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            System.out.println("mylog:外部ストレージの権限が無い");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, new String[]{permission}, 0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else {
            // Permission has already been granted
        }

    }

    public void onClick(View v){
        selectImage();
    }

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode!=REQUEST_IMAGE_GET || resultCode!= RESULT_OK) return;
        Uri uri = data.getData();
        System.out.println(String.format("mylog:画像パス[%s]", uri.toString()));

        String id = DocumentsContract.getDocumentId(uri);
        String[] ids = id.split(":");
        String sid = ids[ids.length-1];

//        Cursor csr = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, "_id=?", new String[]{sid}, null);
        Cursor csr = getContentResolver().query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            new String[]{MediaStore.Images.Media.DATA},
            "_id=?",
            new String[]{sid},
                null
        );

        if( csr.moveToFirst() ){
            String str = csr.getString(0);
            System.out.println(str);
            ImageView img = (ImageView)findViewById(R.id.imageView);
            img.setImageURI(uri);

            File file = new File(str);
            int size = (int)file.length();
            byte[] buffer = new byte[size];

            // targetSdkVersion29 + Android10 で権限エラーで読み込めないパターン
//            try(FileInputStream fs = new FileInputStream(str)){
//                DataInputStream input = new DataInputStream(fs);
//                input.read(buffer,0, size);
//                input.close();
//                fs.close();
//                System.out.println("File load completed");
//            }catch(IOException e){
//                System.out.println(e.getMessage());
//            }
            // targetSdkVersion29 + Android10 で読み込めるパターン
            try(InputStream inputStream = getContentResolver().openInputStream(uri)){
                inputStream.read(buffer,0, size);
                inputStream.close();
                System.out.println("Content load completed");
            }catch(IOException e){
                System.out.println("content error:"+e.getMessage());
            }
        }
        csr.close();

    }
}


