package com.tintin.hrcardrecapp.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;

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

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.tintin.hrcardrecapp.R;
import com.tintin.hrcardrecapp.model.HRCardRecForm;
import com.tintin.hrcardrecapp.model.ShopLocForm;
import com.tintin.hrcardrecapp.service.HRCardRecService;
import com.tintin.hrcardrecapp.util.ErrorDialog;

import java.util.List;

public class InsertActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private static final String LOG_ACTIVITY_TAG = "InsertActivity";
    private final Context INSERT_ACTIVITY_CONTEXT = this;
    private final float DEFAULT_ZOOM_LEVEL = 18.5f;
    private final double MAX_ACCEPTABLE_RANGE = 0.000100;
    private final int SHOW_GPS_TOAST_PERIOD = 300;

    private GoogleMap mMap;
    private double your_Latitude = 0; //latitude (-90 ~ 90)
    private double your_Longitude = 0; //longitude (-180 ~ 180)
    private double shop_Latitude = 0; //shop latitude
    private double shop_Longitude = 0; //shop longitude

    private boolean isSubmit = false; //check can submit the HRCardRec to cloud

    private String date = "";

    private Button btn_submit;
    private TextView txt_cardinfo;

    //GPS location
    private LocationManager locationManager;

    private HRCardRecForm hrcardrecform;
    private ShopLocForm shoplocform;

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
        setContentView(R.layout.activity_insert);

        //show the form value
        Intent intent = getIntent();
        hrcardrecform = (HRCardRecForm) intent.getExtras().getSerializable("HRCardRecform");
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
        btn_submit = (Button) findViewById(R.id.btn_submit);
        txt_cardinfo = (TextView) findViewById(R.id.txt_cardinfo);
        String cardtype = "";
        switch (hrcardrecform.getCardtype()) {
            case "1":
                cardtype = "上班";
                break;
            case "2":
                cardtype = "公出";
                break;
            case "3":
                cardtype = "公入";
                break;
            case "4":
                cardtype = "下班";
                break;
        }

        //set title
        txt_cardinfo.setText(shoplocform.getGrpno() + " " + shoplocform.getGrpname() + " 員編:" + hrcardrecform.getEmpno()
                + " " + hrcardrecform.getReaddt() + " " + cardtype + "打卡");
    }

    public void initViewMapObj(double lat, double log) {
        setShopLatLng(lat, log);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.gMap);
        mapFragment.getMapAsync(this);

    }

    public void onSubmitClick(View view) {
        Toast.makeText(view.getContext(), "送出中...", Toast.LENGTH_SHORT).show();
        btn_submit.setText("正在送出");
        btn_submit.setEnabled(false);
        final HRCardRecService hrcardrecservice = new HRCardRecService();

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        final ProgressDialog ringProgressDialog =
                ProgressDialog.show(INSERT_ACTIVITY_CONTEXT, "上傳打卡紀錄","上傳中,請稍後 ...", true);
        ringProgressDialog.setCancelable(false);  //enable ring progress

        //use new thread to trigger the progress dialog
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String msg = "";
                    String cardtype = "";
                    List<HRCardRecForm> form;
                    DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //close ringprogress
                            ringProgressDialog.dismiss();
                        }
                    };

                    Looper.prepare();  //use loop to handle UIdialog, sub thread default has no UI control
                    hrcardrecservice.insertHRRec(hrcardrecform);
                    form = hrcardrecservice.getHRCardRecForms();
                    if (hrcardrecservice.getIsError() == true) {
                        Log.d("XXXX"," FAILED ");
                        dialog.setTitle("新增失敗");
                        msg = form.get(0).getEmpno();
                        dialog.setMessage(msg);
                    } else if (form.isEmpty()) {
                        dialog.setTitle("新增失敗");
                        Log.d("XXXX"," EMPTY ");
                        msg = "回傳結果爲空";
                        dialog.setMessage(msg);
                    } else {
                        dialog.setTitle("新增成功");
                        Log.d("XXXX"," SUCCESS ");
                        for (int i = 0; i < form.size(); i++) {
                            switch (form.get(i).getCardtype()) {
                                case "1":
                                    cardtype = "上班";
                                    break;
                                case "2":
                                    cardtype = "公出";
                                    break;
                                case "3":
                                    cardtype = "公入";
                                    break;
                                case "4":
                                    cardtype = "下班";
                                    break;
                            }
                            msg = "員編:" + form.get(i).getEmpno() + "\n新增" +
                                    form.get(i).getReaddt() + cardtype + "\n補打卡紀錄成功";
                        }
                    }
                    dialog.setMessage(msg);
                    dialog.setNegativeButton("確定", OkClick);
                    dialog.show();
                    Looper.loop();
                } catch (Exception ex) {
                    Log.e(LOG_ACTIVITY_TAG,ex.getMessage());
                }finally{
                    ringProgressDialog.dismiss();
                }
            }
        }).start();

        btn_submit.setText("打卡");
        btn_submit.setEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0,0),DEFAULT_ZOOM_LEVEL));
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
        String msg = "新經度: " + location.getLatitude()
                + "新緯度: " + location.getLongitude();

        //for debug
        //Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();

        //set your latitude and longitude
        setYourLatLng(location.getLatitude(), location.getLongitude());
        //Log.d("YOUR LOC", Double.toString(your_Latitude) + ", " + Double.toString(your_Longitude));
        //Log.d("SHOP LOC", Double.toString(shop_Latitude) + ", " + Double.toString(shop_Longitude));

        // Add your location on google map
        // remove old location
        refreshMapMarker();

        if ((Math.abs(shop_Latitude - your_Latitude) < MAX_ACCEPTABLE_RANGE)
                && (Math.abs(shop_Longitude - your_Longitude) < MAX_ACCEPTABLE_RANGE)) {
            if (find_times % SHOW_GPS_TOAST_PERIOD == 0) {
                Toast.makeText(getBaseContext(), "手機確定位於分店, 可打卡", Toast.LENGTH_LONG).show();
                find_times = 0;
            }
            find_times++;
            btn_submit.setText("打卡");
            btn_submit.setEnabled(true);
        } else {
            if (not_find_times % SHOW_GPS_TOAST_PERIOD == 0) {
                Toast.makeText(getBaseContext(), "請確認GPS已啟動以及手機確實在分店", Toast.LENGTH_LONG).show();
                not_find_times = 0;
            }
            not_find_times++;
            btn_submit.setText("不可打卡");
            btn_submit.setEnabled(false);
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getBaseContext(), "啟用GPS", Toast.LENGTH_LONG).show();

        //get current location

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getBaseContext(), "GPS已停用, 請啟用GPS", Toast.LENGTH_LONG).show();
        btn_submit.setText("不可打卡");
        btn_submit.setEnabled(false);
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
}
