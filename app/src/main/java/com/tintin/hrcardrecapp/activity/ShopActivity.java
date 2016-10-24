package com.tintin.hrcardrecapp.activity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tintin.hrcardrecapp.R;
import com.tintin.hrcardrecapp.model.ShopLocForm;
import com.tintin.hrcardrecapp.util.ErrorDialog;

import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class ShopActivity extends FragmentActivity implements OnMapReadyCallback, android.location.LocationListener {

    private static final String LOG_ACTIVITY_TAG = "TinTin_ShopSet";
    private static final long MIN_CLICK_INTERVAL = 1000;
    private final Context INSERT_ACTIVITY_CONTEXT = this;
    private final float DEFAULT_ZOOM_LEVEL = 18.5f;
    private long mLastClickTime;

    private GoogleMap mMap;
    private double your_Latitude = 0; //latitude (-90 ~ 90)
    private double your_Longitude = 0; //longitude (-180 ~ 180)
    private double shop_Latitude = 0; //shop latitude
    private double shop_Longitude = 0; //shop longitude

    //GPS location
    private LocationManager locationManager;

    private ShopLocForm shoplocform;

    private Button btn_shopset;
    private TextView txt_shopinfo;

    private int find_times;
    private int not_find_times;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        //show the form value
        Intent intent = getIntent();
        shoplocform = (ShopLocForm) intent.getExtras().getSerializable("ShopLocform");

        //initialize object
        initViewObj();
        //find the shop location from shoplocform
        checkShopLocByGPS();
        initViewMapObj(Double.parseDouble(shoplocform.getGrp_lat())
                , Double.parseDouble(shoplocform.getGrp_lng()));  //initialize google map corresponding shop loc

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    public void setYourLatLng(double lat, double log) {
        this.your_Latitude = lat;
        this.your_Longitude = log;
    }

    public void setShopLatLng(double lat, double log) {
        this.shop_Latitude = lat;
        this.shop_Longitude = log;
    }

    public void initViewObj() {
        btn_shopset = (Button) findViewById(R.id.btn_shopset);
        txt_shopinfo = (TextView) findViewById(R.id.txt_shopinfo);

        String msg = "目前分店:" + shoplocform.getGrpname();
        //set title
        txt_shopinfo.setText(msg);
    }

    public void initViewMapObj(double lat, double log) {
        setShopLatLng(lat, log);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.gMap);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), DEFAULT_ZOOM_LEVEL));
    }

    //enable gps function to check shop location
    public void checkShopLocByGPS() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        } catch (SecurityException e) {
            e.printStackTrace();
            new ErrorDialog().ShowErrorDialog(INSERT_ACTIVITY_CONTEXT, LOG_ACTIVITY_TAG, " GPS發生錯誤, 請重新啟動程式");
        }
    }

    public void refreshMapMarker() {
        Marker marker_shop, marker_your;

        mMap.clear();
        // Add a marker in shop and move the camera
        LatLng shop = new LatLng(shop_Latitude, shop_Longitude);
        marker_shop = mMap.addMarker(new MarkerOptions().position(shop).title(shoplocform.getGrpname()));
        marker_shop.showInfoWindow();

        //add your location
        LatLng your_loc = new LatLng(your_Latitude, your_Longitude);
        MarkerOptions markeroption = new MarkerOptions();
        markeroption.position(your_loc);
        markeroption.title("你的位置");
        markeroption.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        //show marker
        marker_your = mMap.addMarker(markeroption);
        marker_your.showInfoWindow();

        //Move camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(your_loc));
    }

    @Override
    public void onLocationChanged(Location location) {
        String msg = "目前緯度: " + location.getLatitude()
                + "目前經度: " + location.getLongitude();

        //for debug
        //Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();

        //set your latitude and longitude
        setYourLatLng(location.getLatitude(), location.getLongitude());
        Log.d(" NEW SHOP LOC", Double.toString(your_Latitude) + ", " + Double.toString(your_Longitude));
        Log.d(" OLD SHOP LOC", Double.toString(shop_Latitude) + ", " + Double.toString(shop_Longitude));

        // Add your location on google map
        // remove old location
        btn_shopset.setText("校準");
        btn_shopset.setEnabled(true);
        refreshMapMarker();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getBaseContext(), "啟用GPS", Toast.LENGTH_LONG).show();

        //get current location
        btn_shopset.setText("校準");
        btn_shopset.setEnabled(true);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getBaseContext(), "GPS已停用, 請啟用GPS", Toast.LENGTH_LONG).show();
        btn_shopset.setText("不可校準");
        btn_shopset.setEnabled(false);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Insert Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public void onShopSetClick(View view) {
        //saved shop info
        ShopLocForm new_shoplocform = this.shoplocform;
        Double new_shoploclat = this.your_Latitude;
        Double new_shoploclng = this.your_Longitude;
        new_shoplocform.setGrp_lat(Double.toString(new_shoploclat));
        new_shoplocform.setGrp_lng(Double.toString(new_shoploclng));

        //save info
        savedShopInfo(new_shoplocform);

        //if initialize, do load shop info
        switchToMain();
        //switch to MainActivity
    }

    //Switch to query activity
    public void switchToMain() {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);

        //transfer parameter to insert web

        //close this activity
        finish();
    }

    public void savedShopInfo(ShopLocForm shoplocform) {
        //saved shop info on the phone permanently
        try {
            FileOutputStream fileout = openFileOutput("shopinfo.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);

            //build jsonObject
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("shop", shoplocform.getGrpname());
            jsonObj.put("id", shoplocform.getGrpno());
            jsonObj.put("lat", shoplocform.getGrp_lat());
            jsonObj.put("lng", shoplocform.getGrp_lng());

            outputWriter.write(jsonObj.toString());

            //display file saved message
            Toast.makeText(getBaseContext(), "儲存新分店資訊成功", Toast.LENGTH_LONG).show();

            if (outputWriter != null) {
                outputWriter.close();
            }

            if (fileout != null) {
                fileout.close();
            }

        } catch (Exception ex) {
            Toast.makeText(getBaseContext(), "儲存新分店資訊失敗, 請聯絡開發人員", Toast.LENGTH_LONG).show();
            Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
        }
    }
}
