package com.example.geophotoapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class Actividad2 extends AppCompatActivity {
    private ListView lv1;
    private ImageView iv1;
    private String [] archivos;
    private ArrayAdapter<String> adaptador;
    private double latitude, longitude;
    private File foto;
    String nombFoto;

    public void solicitarPermiso(String permiso, int codigoPermiso) {
        if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permiso}, codigoPermiso);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_actividad2);
        File dir = getFilesDir();
        archivos=dir.list();
        adaptador= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,archivos);
        lv1=(ListView) findViewById(R.id.listv);
        lv1.setAdapter(adaptador);
        iv1=(ImageView) findViewById(R.id.imageView2);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }
        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bitmap bitmap1 = BitmapFactory.decodeFile(getFilesDir()+"/"+archivos[i]);
                nombFoto=archivos[i];
                iv1.setImageBitmap(bitmap1);
                foto = new File(getFilesDir()+"/"+archivos[i]);
                try {
                    ExifInterface exif = new ExifInterface(foto.getAbsolutePath());
                    Log.d("app","url: "+foto.getAbsolutePath());
                    float[] latLong = new float[2];
                    exif.getLatLong(latLong);
                    Log.d("app", "valor: "+latLong[0]);
                    latitude=latLong[0];
                    longitude=latLong[1];
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void verUbicacion(View view) {
        String uri = String.format("geo:%f,%f?q=%f,%f(Ubicación de %s)", latitude, longitude,latitude,longitude,nombFoto);
        Uri gmmIntentUri=Uri.parse(uri);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivityForResult(intent, 1);
    }
}