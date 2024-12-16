package com.example.geophotoapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {
    private ImageView imagen;
    private EditText et1;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude, longitude;

    public void solicitarPermiso(String permiso, int codigoPermiso) {
        if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permiso}, codigoPermiso);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
        }
        if (requestCode == 11) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                }

            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        imagen=findViewById(R.id.imageView);
        et1=findViewById(R.id.et1);
        solicitarPermiso(Manifest.permission.CAMERA,11);
        solicitarPermiso(Manifest.permission.ACCESS_FINE_LOCATION, 1);
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
    }

    public void ver(View view) {
        Intent intento = new Intent(this, Actividad2.class);
        startActivity(intento);
    }


    public void recuperarFoto(View view) {
        File foto = new File(getFilesDir()+"/"+et1.getText().toString());
        Bitmap bitmap= BitmapFactory.decodeFile(foto.getAbsolutePath());
        imagen.setImageBitmap(bitmap);
        try {
            ExifInterface exif = new ExifInterface(foto.getAbsolutePath());
            float[] latLong = new float[2];
            exif.getLatLong(latLong);
            latitude=latLong[0];
            longitude=latLong[1];
            Log.d("app","valor:"+latitude+","+longitude);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tomarFoto(View view) {
        Intent intento = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File foto = new File(getFilesDir(),et1.getText().toString());
        Uri uri = FileProvider.getUriForFile(this,getPackageName()+".fileprovider", foto);
        intento.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intento, 11);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 11 && resultCode == RESULT_OK) {
            File foto = new File(getFilesDir(), et1.getText().toString());
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                solicitarPermiso(Manifest.permission.ACCESS_FINE_LOCATION,1);
                return;
            }
            locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, null, getApplicationContext().getMainExecutor(), new Consumer<Location>() {
                @Override
                public void accept(Location location) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    try {
                        ExifInterface exif = new ExifInterface(foto.getAbsolutePath());
                        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convertToExifFormat(latitude));
                        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertToExifFormat(longitude));
                        exif.setAttribute( ExifInterface.TAG_GPS_LATITUDE_REF, latitude >= 0 ? "N" : "S");
                        exif.setAttribute( ExifInterface.TAG_GPS_LONGITUDE_REF, longitude >= 0 ? "E" : "W");
                        exif.saveAttributes();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    public void verUbicacion(View view) {
        String uri = String.format("geo:%f,%f?q=%f,%f(Ubicación de %s)", latitude, longitude,latitude,longitude,et1.getText().toString());
        Uri gmmIntentUri=Uri.parse(uri);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    private String convertToExifFormat(double coordinate) {
        if(coordinate<0){
            coordinate=-coordinate;
        }
        int degreesInt = (int)coordinate;
        double minutes = (coordinate - degreesInt) * 60;
        int minutesInt = (int) minutes;
        double seconds = (minutes - minutesInt) * 60 * 1000;
        int secondsInt=(int) seconds;
        return String.format("%d/1,%d/1,%d/1000", degreesInt, minutesInt, secondsInt);
    }


}