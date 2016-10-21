package com.tintin.hrcardrecapp.activity;

import com.tintin.hrcardrecapp.model.HRCardRecForm;
import com.tintin.hrcardrecapp.model.ShopLocForm;
import com.tintin.hrcardrecapp.service.HRCardRecService;
import com.tintin.hrcardrecapp.service.ShopLocService;
import com.tintin.hrcardrecapp.util.*;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.tintin.hrcardrecapp.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String LOG_ACTIVITY_TAG = "MainActivity";
    private static final int DATE_DIALOG_ID = 999;
    private final Context MAIN_ACTIVITY_CONTEXT = this;
    private long lastClickTime = 0;
    private final int MIN_CLICK_MSEC = 1000;
    private final int MIN_CLICK_TIMES = 10;

    private final String PASSWORD_CHANGE_SHOP = "norbel38";

    private final Context context = this;

    private Button btn_Qdate;
    private Spinner sp_Qtype;
    private Spinner sp_Qlimit;

    private TextView txt_Qdate;
    private TextClock text_Clock;

    private EditText et_idno;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup button listeners
        initViewObj();
        initShopInfo();
        addSetDateListenerOnButton();
    }

    public void initViewObj() {
        btn_Qdate = (Button) findViewById(R.id.btn_Qdate);
        txt_Qdate = (TextView) findViewById(R.id.txt_Qdate);
        text_Clock = (TextClock) findViewById(R.id.t_Clock);
        et_idno = (EditText) findViewById(R.id.et_idno);
        et_empno = (EditText) findViewById(R.id.et_empno);
        sp_Qtype = (Spinner) findViewById(R.id.sp_Qtype);
        sp_Qlimit = (Spinner) findViewById(R.id.sp_Qlimit);

        txt_shop = (TextView) findViewById(R.id.txt_shop); //Add btn listener to change shop location

        //set ItemSelect listener
        sp_Qtype.setOnItemSelectedListener(this);
        sp_Qlimit.setOnItemSelectedListener(this);

        /*
        Log.v(LOG_ACTIVITY_TAG, "This is Verbose.");
        Log.d(LOG_ACTIVITY_TAG, "This is Debug.");
        Log.i(LOG_ACTIVITY_TAG, "This is Information");
        Log.w(LOG_ACTIVITY_TAG, "This is Warnning.");
        Log.e(LOG_ACTIVITY_TAG, "This is Error.");
        */
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
            shoplocservice.setAllShopAndLoc();
            String[] shops = shoplocservice.getShopTitleListsArr();
            if (shops.length == 0) {
                new ErrorDialog().ShowErrorDialog(MAIN_ACTIVITY_CONTEXT, " 取得分店列表失敗, 請檢查網路");
                return;
            } else {
                //update shop info
                selectShopDialog(shops, true);
            }
        } else {
            loadShopInfoInit();  //load shop info
        }
    }

    public void onQueryHRCardRecClick(final View v) {
        String cardtype = "";
        String empno = "";
        String limit = "";
        String qdate = "";

        //set query cardtype and limit
        cardtype = QHRCardType;
        limit = QHRCardLimit;

        //ERROR checking
        //ERROR checking
        if (!fieldChecking(false)) {
            return;
        }

        //setting value
        empno = "0000" + et_empno.getText().toString();
        qdate = txt_Qdate.getText().toString().trim().replace("-", "");

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
                        Toast.makeText(MAIN_ACTIVITY_CONTEXT,"取得資料失敗 請檢查是否有啟用網路連線", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        switchToQuery(hrcardrecform_thread, qHRCardRecs);
                    }
                    progressDialog.dismiss();
                    Looper.loop();
                }
                catch (Exception ex) {
                    Log.e(LOG_ACTIVITY_TAG,ex.getMessage());
                }finally{
                    progressDialog.dismiss();
                }

            }
        }).start();

        //async to run query data
        /*
        try {
            qHRCardRecs = new ProgressTask().execute(hrcardrecform).get();
        } catch (Exception ex) {
            Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
        }
        */

        //qHRCardRecs = (ArrayList<HRCardRecForm>) hrCardRecService.getHRCardRecForms();
        //switch to query layout

    }

    //set shop location , use long click
    public void onShopLabelClick(View view) {
        if (SystemClock.elapsedRealtime() - lastClickTime < (MIN_CLICK_MSEC * 2)) {
            setShopClick_times++;
            //Toast.makeText(view.getContext(),"Times: "+Integer.toString(setShopClick_times), Toast.LENGTH_SHORT).show();
            if (setShopClick_times >= MIN_CLICK_TIMES) {
                setShopClick_times = 0;
                checkPasswordDialog();
                lastClickTime = SystemClock.elapsedRealtime();
            }
        } else {
            setShopClick_times = 0;
            lastClickTime = SystemClock.elapsedRealtime();
            return;
        }
    }

    public void checkPasswordDialog() {
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
                    shoplocservice.setAllShopAndLoc();

                    //get shop info
                    //shoplocservice.getShopListsArr();
                    //show list of location
                    String[] shops = shoplocservice.getShopTitleListsArr();
                    if (shops.length == 0) {
                        new ErrorDialog().ShowErrorDialog(MAIN_ACTIVITY_CONTEXT, " 取得分店列表失敗");
                        return;
                    } else {
                        //update shop info
                        selectShopDialog(shops, false);
                    }
                } else {
                    String message = "輸入錯誤, 請重新輸入";
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("認證失敗");
                    builder.setMessage(message);
                    builder.setNegativeButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            checkPasswordDialog();
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

    public void selectShopDialog(String[] shop_items, boolean isInitial) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        final String items[] = shop_items;
        adb.setItems(shop_items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface d, int n) {
                txt_shop.setText(items[n]);
                if (shoplocservice.getShopByName(items[n]) == null) {
                    new ErrorDialog().ShowErrorDialog(MAIN_ACTIVITY_CONTEXT, " 設定新分店失敗");
                    return;
                } else {
                    //set new shop
                    shoplocform = shoplocservice.getShopByName(items[n]);
                    Log.d(LOG_ACTIVITY_TAG, " XXX FORM " + shoplocform.toString());

                    //saved shop info
                    savedShopInfo();

                    //if initialize, do load shop info
                    loadShopInfoInit();
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
        idno = et_idno.getText().toString();
        empno = "0000" + et_empno.getText().toString(); //add zero to fill 8 digit
        readdt = text_Clock.getText().toString();

        //set ip
        ip = Utility.getIPAddress(true);

        //insert HRCardRec to service
        HRCardRecForm hrcardrecform = new HRCardRecForm(idno, empno, readdt, cardtype, ip);
        hrcardrecform.setLimit("");
        switchToInsert(hrcardrecform);
    }

    private boolean fieldChecking(boolean isInsert) {
        if (isInsert) {
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
        }

        if (et_empno.getText().toString().trim().length() == 0) {
            //show dialog
            new ErrorDialog().ShowErrorDialog(MAIN_ACTIVITY_CONTEXT, " 員工編號不可為空");
            et_empno.requestFocus();
            return false;
        }
        if (et_empno.getText().toString().trim().length() != 4) {
            //show dialog
            new ErrorDialog().ShowErrorDialog(MAIN_ACTIVITY_CONTEXT, LOG_ACTIVITY_TAG, " 請輸入完整的員工編號4碼");
            et_empno.requestFocus();
            return false;
        }
        if (!Utility.EmpNoChecker(et_empno.getText().toString().trim())) {
            new ErrorDialog().ShowErrorDialog(MAIN_ACTIVITY_CONTEXT, LOG_ACTIVITY_TAG, " 請輸入員工編號數字4碼");
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

    //Switch to insert activity
    public void switchToInsert(HRCardRecForm hrcardrecform) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, InsertActivity.class);

        //transfer parameter to insert web
        intent.putExtra("HRCardRecform", hrcardrecform);
        intent.putExtra("ShopLocform", this.shoplocform);

        //switch activity to insert
        startActivity(intent);

        //MainActivity.this.finish();  //disable, due to MainActivity cannot be finish working
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

    public void savedShopInfo() {
        //saved shop info on the phone permanently
        try {
            FileOutputStream fileout = openFileOutput("shopinfo.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);

            //build jsonObject
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("shop", this.shoplocform.getGrpname());
            jsonObj.put("id", this.shoplocform.getGrpno());
            jsonObj.put("lat", this.shoplocform.getGrp_lat());
            jsonObj.put("lng", this.shoplocform.getGrp_lng());

            outputWriter.write(jsonObj.toString());

            //display file saved message
            new ErrorDialog().ShowSuccessDialog(MAIN_ACTIVITY_CONTEXT, " 儲存新分店資訊成功");

            if (outputWriter != null) {
                outputWriter.close();
            }

            if (fileout != null) {
                fileout.close();
            }

        } catch (Exception ex) {
            new ErrorDialog().ShowErrorDialog(MAIN_ACTIVITY_CONTEXT, " 儲存新分店資訊失敗");
            Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
        }
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
            shoplocform = new ShopLocForm(jsonObj.getString("id"), jsonObj.getString("shop"),
                    jsonObj.getString("lat"), jsonObj.getString("lng"));

            //set shop title
            txt_shop.setText(jsonObj.getString("shop"));
            return true;
        } catch (Exception ex) {
            Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
            return false;
        }
    }
}

