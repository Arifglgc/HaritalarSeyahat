package com.arifgolgeci.haritalarseyahat;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.arifgolgeci.haritalarseyahat.model.Place;
import com.arifgolgeci.haritalarseyahat.roomDb.PlaceDao;
import com.arifgolgeci.haritalarseyahat.roomDb.PlaceDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.arifgolgeci.haritalarseyahat.databinding.ActivityMapsBinding;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    LocationManager locationManager;
    LocationListener locationListener;

    SharedPreferences sharedPreferences;
    boolean info;

    PlaceDatabase db;
    PlaceDao placeDao;

    Double selectedLongitude;
    Double selectedLatitude;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    Place selectedPlace;

    ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        registerLauncher();
         sharedPreferences = this.getSharedPreferences("com.arifgolgeci.haritalarseyahat",MODE_PRIVATE);
         info=false;

         db= Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places")
                 //.allowMainThreadQueries() bu kod da cal??s??r ama bizim amac??m??z buray?? main threadde ??al????t??rmaya zorlamak degil
                 .build();
         placeDao =db.placeDao();

         selectedLatitude=0.0;
         selectedLongitude=0.0;
        binding.saveButton.setEnabled((false));
    }


    /**
     * Manipulates the map once available.
      * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        Intent intent=getIntent();
        String intentInfo =intent.getStringExtra("info");

        if (intentInfo.equals("new")){
         binding.saveButton.setVisibility(View.VISIBLE);
         binding.deleteButton.setVisibility(View.GONE);
            locationManager = (LocationManager)(this.getSystemService(Context.LOCATION_SERVICE));
            locationListener= new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    info= sharedPreferences.getBoolean("info",false);

                    if (info==false){
                        LatLng userLocation=new LatLng(location.getLatitude(),location.getAltitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
                        sharedPreferences.edit().putBoolean("info",true).apply();


                    }
                    // System.out.println("Location"+ location.toString());
                }
                @Override
                public void onProviderEnabled(@NonNull String provider) {

                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }
            };

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                    Snackbar.make(binding.getRoot(),"Permission needed for maps", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // request permission
                            permissionLauncher.launch((Manifest.permission.ACCESS_FINE_LOCATION));
                        }
                    }).show();
                } else{
                    //request permission
                    permissionLauncher.launch((Manifest.permission.ACCESS_FINE_LOCATION));

                }
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location lastLocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation!=null){
                    LatLng lastUserLocation=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));

                }
                mMap.setMyLocationEnabled((true));

            }
        }
        else{
            mMap.clear();
           selectedPlace =(Place)intent.getSerializableExtra("place");
           LatLng latLng= new LatLng(selectedPlace.latitude,selectedPlace.longitude);
           mMap.addMarker(new MarkerOptions().position(latLng).title(selectedPlace.name));
           mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

           binding.placeNameText.setText((selectedPlace.name));
           binding.saveButton.setVisibility(View.GONE);
           binding.deleteButton.setVisibility(View.VISIBLE);
        }



    //        Place place=new Place();




        // Add a marker in Sydney and move the camera
         // LatLng sydney = new LatLng(-34, 151);
        //  mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,15));
    }

    private void registerLauncher(){
        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {

            @Override
            public void onActivityResult(Boolean result) {
                if (result ){
                    // permission granted
                    if (ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                    Location lastLocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastLocation!=null){
                        LatLng lastUserLocation=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                    }
                }
                else{
                    // permission denied
                    Toast.makeText(MapsActivity.this,"Permission needed!", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));
        selectedLatitude = latLng.latitude;
        selectedLongitude = latLng.longitude;

        binding.saveButton.setEnabled((true)); // butonun kullan??lmas?? istenmeze(yani se??im yap??lmad??ysa


    }
    public void save(View view){
        Place place =new Place(binding.placeNameText.getText().toString(),selectedLatitude,selectedLongitude);

        //threading--> Main(UI), Default(CPU intensive) yuksek ismler), IO (Network, database)

         // placeDao.insert(place).subscribeOn(Schedulers.io()).subscribe(); --> islemi io threadde gerceklestir

        //disposable
        compositeDisposable.add(placeDao.insert(place)
        .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MapsActivity.this::handleResponse)
        );


    }
    private void handleResponse(){
        Intent intent =new Intent(MapsActivity.this,MainActivity.class);
        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }
    public void delete(View view)
    {
        if (selectedPlace!=null ) {
            compositeDisposable.add(placeDao.delete(selectedPlace)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(MapsActivity.this::handleResponse)
            );
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
