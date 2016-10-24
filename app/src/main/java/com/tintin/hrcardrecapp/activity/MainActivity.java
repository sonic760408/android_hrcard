package com.tintin.hrcardrecapp.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.tintin.hrcardrecapp.R;
import com.tintin.hrcardrecapp.model.HRCardRecForm;
import com.tintin.hrcardrecapp.model.ShopLocForm;
import com.tintin.hrcardrecapp.service.HRCardRecService;
import com.tintin.hrcardrecapp.service.ShopLocService;
import com.tintin.hrcardrecapp.util.ErrorDialog;
import com.tintin.hrcardrecapp.util.Utility;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String LOG_ACTIVITY_TAG = "TinTin_Main";
    private static final int DATE_DIALOG_ID = 999;
    //for message
    private static final int MSG_SETTEXT = 0;
    private static final long MIN_CLICK_INTERVAL = 1000;
    private final Context MAIN_ACTIVITY_CONTEXT = this;
    private final int MIN_CLICK_MSEC = 1000;
    private final int MIN_CLICK_TIMES = 10;
    private final String PASSWORD_CHANGE_SHOP = "norbel74";
    private final Context context = this;
    private long lastClickTime = 0;
    private long mLastClickTime;
    private Button btn_Qdate;
    private Spinner sp_Qtype;
    private Spinner sp_Qlimit;

    private TextView txt_Qdate;
    private TextClock text_Clock;

    //private EditText et_idno;
    private EditText et_empno;

    private TextView txt_shop;

    private int year;
    private int month;
    private int day;

    private int setShopClick_times = 0;

    private String QHRCardType;
    private String QHRCardLimit;

    private ShopLocForm shoplocform;

    private ShopLocService shoplocservice = new ShopLocService();
    private ProgressDialog progressDialog;

    private ArrayList<HRCardRecForm> qHRCardRecs;
    private String new_shop_name = "";

    private List<ScanResult> wifi_result_list;
    //use handler to handle UI changes for UI thread
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == MSG_SETTEXT) {
                txt_shop.setText(new_shop_name);
            }

        }
    };
    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;

            String str_date = "";

            //set date
            str_date = str_date.concat(Integer.toString(year)).concat("-");

            //set txt_Qdate as current date
            if (month + 1 < 10)
                str_date = str_date.concat("0");
            str_date = str_date.concat(Integer.toString(month + 1)).concat("-");

            if (day < 10)
                str_date = str_date.concat("0");
            str_date = str_date.concat(Integer.toString(day));

            txt_Qdate.setText(new StringBuilder(str_date));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup button listeners
        Log.w(" ONCREATE", "ONCREATE");
        initViewObj();
        initShopInfo();
        addSetDateListenerOnButton();
    }

    public void initViewObj() {
        btn_Qdate = (Button) findViewById(R.id.btn_Qdate);
        txt_Qdate = (TextView) findViewById(R.id.txt_Qdate);
        text_Clock = (TextClock) findViewById(R.id.t_Clock);
        //et_idno = (EditText) findViewById(R.id.et_idno);
        et_empno = (EditText) findViewById(R.id.et_empno);
        sp_Qtype = (Spinner) findViewById(R.id.sp_Qtype);
        sp_Qlimit = (Spinner) findViewById(R.id.sp_Qlimit);

        txt_shop = (TextView) findViewById(R.id.txt_shop); //Add btn listener to change shop location

        //set ItemSelect listener
        sp_Qtype.setOnItemSelectedListener(this);
        sp_Qlimit.setOnItemSelectedListener(this);
    }

    public void setShopLocForm(ShopLocForm shoplocform) {
        this.shoplocform = shoplocform;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        parent.getItemAtPosition(pos);

        if (parent.getId() == R.id.sp_Qtype) // for query type
        {
            switch (pos) {
                case 0:
                    QHRCardType = "%";
                    break;
                case 1:
                    QHRCardType = "1";
                    break;
                case 2:
                    QHRCardType = "4";
                    break;
                case 3:
                    QHRCardType = "2";
                    break;
                case 4:
                    QHRCardType = "3";
                    break;
            }
        } else if (parent.getId() == R.id.sp_Qlimit) //for query limit
        {
            switch (pos) {
                case 0:
                    QHRCardLimit = "10";
                    break;
                case 1:
                    QHRCardLimit = "20";
                    break;
                case 2:
                    QHRCardLimit = "30";
                    break;
                case 3:
                    QHRCardLimit = "50";
                    break;
                case 4:
                    QHRCardLimit = "100";
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    public void initShopInfo() {
        if (!loadShopInfoInit()) //first time, need to be set shop location
        {
            //set shops
            setShopsLocByWeb();
        } else {
            loadShopInfoInit();  //load shop info
        }
    }

    public void onQueryHRCardRecClick(final View v) {

        //prevent multiple click
        long currentClickTime = SystemClock.uptimeMillis();
        long elapsedTime = currentClickTime - mLastClickTime;

        mLastClickTime = currentClickTime;

        if (elapsedTime <= MIN_CLICK_INTERVAL) {
            return;
        }

        String cardtype = "";
        String empno = "";
        String limit = "";
        String qdate = "";

        //btn_Qdate.setText("送出中");
        //btn_Qdate.setEnabled(false);

        //set query cardtype and limit
        cardtype = QHRCardType;
        limit = QHRCardLimit;

        //ERROR checking
        if (!fieldChecking(false)) {
            btn_Qdate.setText("打卡紀錄查詢");
            btn_Qdate.setEnabled(true);
            return;
        }

        //setting value
        empno = et_empno.getText().toString();


        //set qdate's time as 23:59:59 (sql query date set as 00:00:00)
        qdate = txt_Qdate.getText().toString().trim().concat(" 23:59:59");

        //show ring progress dialog

        //query HRCardRec to service
        HRCardRecForm hrcardrecform = new HRCardRecForm("", empno, qdate, cardtype, "");
        hrcardrecform.setLimit(limit);
        final HRCardRecForm hrcardrecform_thread = hrcardrecform;
        final HRCardRecService hrCardRecService = new HRCardRecService();

        //ArrayList<HRCardRecForm> qHRCardRecs = new ArrayList<>();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("下載打卡紀錄");
        progressDialog.setMessage("下載中,請稍後 ...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
        // New thread

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    Log.w(LOG_ACTIVITY_TAG, " IN THREAD ");
                    hrCardRecService.queryHRRec(hrcardrecform_thread);
                    qHRCardRecs = (ArrayList<HRCardRecForm>) hrCardRecService.getHRCardRecForms();
                    if (hrCardRecService.getIsError()) {
                        Toast.makeText(MAIN_ACTIVITY_CONTEXT, "取得資料失敗 請重新操作", Toast.LENGTH_LONG).show();
                    } else {
                        switchToQuery(hrcardrecform_thread, qHRCardRecs);
                    }
                    progressDialog.dismiss();
                    Looper.loop();
                } catch (Exception ex) {
                    Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
                } finally {
                    progressDialog.dismiss();
                }

            }
        }).start();
        //btn_Qdate.setText("打卡紀錄查詢");
        //btn_Qdate.setEnabled(true);
    }

    //set shop location , use mulitclick
    public void onShopLabelClick(View view) {
        if (SystemClock.elapsedRealtime() - lastClickTime < (MIN_CLICK_MSEC * 2)) {
            setShopClick_times++;
            //Toast.makeText(view.getContext(),"Times: "+Integer.toString(setShopClick_times), Toast.LENGTH_SHORT).show();
            if (setShopClick_times >= MIN_CLICK_TIMES) {
                setShopClick_times = 0;
                checkPasswordDialog(view);
                lastClickTime = SystemClock.elapsedRealtime();
            }
        } else {
            setShopClick_times = 0;
            lastClickTime = SystemClock.elapsedRealtime();
            return;
        }
    }

    public void setShopsLocByWeb() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("取得分店列表");
        progressDialog.setMessage("下載中,請稍後 ...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    //go to change dialog
                    shoplocservice.setAllShopAndLoc();

                    //get shop info
                    //shoplocservice.getShopListsArr();
                    //show list of location
                    String[] shops = shoplocservice.getShopTitleListsArr();

                    if (shops.length == 0) {
                        new ErrorDialog().ShowErrorDialog(MAIN_ACTIVITY_CONTEXT, " 取得分店列表失敗");
                        progressDialog.dismiss();
                    } else {
                        //update shop info
                        progressDialog.dismiss();
                        selectShopDialog(shops);
                    }
                    Looper.loop();
                } catch (Exception ex) {
                    Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
                } finally {
                    progressDialog.dismiss();
                }
            }
        }).start();
    }

    public void checkPasswordDialog(final View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptView = layoutInflater.inflate(R.layout.prompts, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // set prompts.xml to be the layout file of the alertdialog builder
        alertDialogBuilder.setView(promptView);
        alertDialogBuilder.setTitle("請輸入分店更改權限密碼");
        final EditText input = (EditText) promptView.findViewById(R.id.txt_passwd);

        // setup a dialog window
        alertDialogBuilder.setCancelable(false).setPositiveButton("確定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // get user input and set it to result
                if (input.getText().toString().equals(PASSWORD_CHANGE_SHOP)) {
                    //go to change dialog
                    setShopsLocByWeb();

                } else {
                    String message = "輸入錯誤, 請重新輸入";
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("認證失敗");
                    builder.setMessage(message);
                    builder.setNegativeButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            checkPasswordDialog(view);
                        }
                    });
                    builder.create().show();
                }
            }
        })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alertD = alertDialogBuilder.create();
        alertD.show();
    }

    public void selectShopDialog(String[] shop_items) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        final String items[] = shop_items;
        adb.setItems(shop_items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface d, int n) {
                new_shop_name = items[n];
                //txt_shop.setText(items[n]);

                //push message to handle the labal(Sub Thread cannot be modify the view, only UI thread can)
                Message msg = new Message();
                msg.what = MSG_SETTEXT;
                handler.sendMessage(msg);

                if (shoplocservice.getShopByName(items[n]) == null) {
                    new ErrorDialog().ShowErrorDialog(MAIN_ACTIVITY_CONTEXT, " 設定新分店失敗");
                    return;
                } else {
                    //set new shop
                    shoplocform = shoplocservice.getShopByName(items[n]);

                    //switch to ShopActivity
                    switchToShopSet(shoplocform);

                }
            }

        });
        String title = "選擇新分店 目前分店:" + txt_shop.getText().toString();
        adb.setTitle(title);
        adb.show();
    }

    public void onInsertHRCardRecClick(View v) {

        //prevent double click
        if (SystemClock.elapsedRealtime() - lastClickTime < MIN_CLICK_MSEC) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();

        String cardtype = "";
        String idno = "";
        String empno = "";
        String ip = "";
        String readdt = "";

        switch (v.getId()) {

            case R.id.btn_onDuty:
                cardtype = "1";
                break;
            case R.id.btn_offDuty:
                cardtype = "4";
                break;
            case R.id.btn_onBuss:
                cardtype = "2";
                break;
            case R.id.btn_offBuss:
                cardtype = "3";
                break;
        }

        //ERROR checking
        if (!fieldChecking(true))
            return;

        //check empno is legal

        //setting value
        idno = "A000000000";
        empno = et_empno.getText().toString(); //add zero to fill 8 digit
        readdt = text_Clock.getText().toString();

        //set ip
        ip = Utility.getIPAddress(true);

        //insert HRCardRec to service
        HRCardRecForm hrcardrecform = new HRCardRecForm(idno, empno, readdt, cardtype, ip);
        hrcardrecform.setLimit("");
        switchToInsert(hrcardrecform);
    }

    public boolean fieldChecking(boolean isInsert) {
        if (isInsert) {
            /*
            if (et_idno.getText().toString().trim().length() == 0) {
                //show dialog
                new ErrorDialog().ShowErrorDialog(MAIN_ACTIVITY_CONTEXT, " ID不可為空");
                et_idno.requestFocus();
                return false;
            }
            if (et_idno.getText().length() != 10) {
                new ErrorDialog().ShowErrorDialog(MAIN_ACTIVITY_CONTEXT, LOG_ACTIVITY_TAG, "請檢查身分證字號是否爲10碼");
                et_idno.requestFocus();
                return false;
            }

            if (!Utility.TWPIDChecker(et_idno.getText().toString().trim())) {
                new ErrorDialog().ShowErrorDialog(MAIN_ACTIVITY_CONTEXT, LOG_ACTIVITY_TAG, " 請檢查身分證字號是否正確");
                et_idno.requestFocus();
                return false;
            }
            */
        }

        if (et_empno.getText().toString().trim().length() == 0) {
            //show dialog
            new ErrorDialog().ShowErrorDialog(MAIN_ACTIVITY_CONTEXT, " 員工編號不可為空");
            et_empno.requestFocus();
            return false;
        }
        if (et_empno.getText().toString().trim().length() != 8) {
            //fill zero
            String tmp = "";
            for (int i = 0; i < 8 - et_empno.getText().length(); i++) {
                tmp = tmp + "0";
            }
            et_empno.setText(tmp + et_empno.getText().toString());
        }
        if (!Utility.EmpNoChecker(et_empno.getText().toString().trim())) {
            new ErrorDialog().ShowErrorDialog(MAIN_ACTIVITY_CONTEXT, LOG_ACTIVITY_TAG, " 請輸入員工編號數字");
            et_empno.requestFocus();
            return false;
        }

        return true;
    }

    public void addSetDateListenerOnButton() {

        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        String str_date = "";

        //set date
        str_date = str_date.concat(Integer.toString(year)).concat("-");

        //set txt_Qdate as current date
        if (month + 1 < 10)
            str_date = str_date.concat("0");
        str_date = str_date.concat(Integer.toString(month + 1)).concat("-");

        if (day < 10)
            str_date = str_date.concat("0");
        str_date = str_date.concat(Integer.toString(day));

        txt_Qdate.setText(new StringBuilder(str_date));

        btn_Qdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                // set date picker as current date
                return new DatePickerDialog(this, datePickerListener, year, month,
                        day);
        }
        return null;
    }

    //Switch to insert activity
    public void switchToInsert(HRCardRecForm hrcardrecform) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, InsertActivity.class);

        //transfer parameter to insert web
        intent.putExtra("HRCardRecform", hrcardrecform);
        intent.putExtra("ShopLocform", this.shoplocform);

        //switch activity to insert
        startActivity(intent);
    }

    //Switch to bind activity
    public void switchToBind() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, BindwifiActivity.class);

        //switch activity to insert
        startActivity(intent);
    }

    //Switch to query activity
    public void switchToQuery(HRCardRecForm hrcardrecform, ArrayList<? extends Serializable> qHRCardRecs) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, QueryActivity.class);

        //transfer parameter to insert web
        intent.putExtra("HRCardRecform", hrcardrecform);
        intent.putExtra("QHRCardRecList", qHRCardRecs);

        //switch activity to query
        startActivity(intent);

    }

    //Switch to shopset activity
    public void switchToShopSet(ShopLocForm shoplocform) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, ShopActivity.class);

        //transfer parameter to insert web
        intent.putExtra("ShopLocform", shoplocform);

        //switch activity to query
        startActivity(intent);

    }

    public boolean loadShopInfoInit() {
        //load shop info from the phone
        String str;
        try {
            FileInputStream in = openFileInput("shopinfo.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }

            if (in != null) {
                in.close();
            }
            //Log.d(LOG_ACTIVITY_TAG, " XXX STRING: "+sb.toString());
            str = sb.toString();
            //set shop info to ShopLocForm
            JSONObject jsonObj = new JSONObject(str);
            setShopLocForm(new ShopLocForm(jsonObj.getString("id"), jsonObj.getString("shop"),
                    jsonObj.getString("lat"), jsonObj.getString("lng")));

            //set shop title
            txt_shop.setText(jsonObj.getString("shop"));
            return true;
        } catch (Exception ex) {
            Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
            return false;
        }
    }

    public void onWifiScanClick(View view) {
        switchToBind();
    }


}

