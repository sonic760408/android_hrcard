package com.tintin.hrcardrecapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.tintin.hrcardrecapp.R;

import java.util.List;

public class BindwifiActivity extends AppCompatActivity {

    private static final String LOG_ACTIVITY_TAG = "TinTin_BindWifi";
    private static final int MSG_REFRESH_LIST = 0;
    private final Context BINDWIFI_ACTIVITY_CONTEXT = this;
    private List<ScanResult> wifi_result_list;
    private WifiManager wifi_mng;
    private WifiReceiver wifi_rec;
    //use handler to handle UI changes for UI thread
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == MSG_REFRESH_LIST) {
                refreshList();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bindwifi);

        initWifiScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wifiscanbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //do behavior according ItemID
        switch (id) {
            case R.id.action_refresh:
                //refresh list
                if (wifi_rec != null)
                    unregisterReceiver(wifi_rec);

                initWifiScan();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshList() {
        int level = 4;
        //if ScanResult is not null, show list
        for (int i = 0; i < wifi_result_list.size(); i++) {
            Log.w(LOG_ACTIVITY_TAG, i + ". SSID: " + wifi_result_list.get(i).SSID
                    + " STRENGTH: " + WifiManager.calculateSignalLevel(wifi_result_list.get(i).level, level));
        }
    }

    //use thread to scan wifi
    public void initWifiScan() {
        Toast.makeText(BINDWIFI_ACTIVITY_CONTEXT, "掃描Wifi中", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    Log.w(LOG_ACTIVITY_TAG, " IN THREAD ");
                    bindWifi();

                    if (wifi_result_list != null) {
                        //send msg to refreshList();
                        //refreshList();
                    }
                    Looper.loop();
                } catch (Exception ex) {
                    Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
                } finally {

                }
            }
        }).start();

    }

    public void bindWifi() {
        wifi_mng = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifi_rec = new WifiReceiver();
        registerReceiver(wifi_rec, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifi_mng.startScan();
    }

    public List<ScanResult> getWifiResultList() {
        return wifi_result_list;
    }

    public void setWifiResultList(List<ScanResult> list) {
        wifi_result_list = list;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_ACTIVITY_TAG, " PAUSE ");
        unregisterReceiver(wifi_rec);
    }

    class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            setWifiResultList(wifi_mng.getScanResults());
            Log.d(LOG_ACTIVITY_TAG, "SET WIFI");

            //push message to refresh wifi lists
            Message msg = new Message();
            msg.what = MSG_REFRESH_LIST;
            handler.sendMessage(msg);
        }
    }
}
