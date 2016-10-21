package com.tintin.hrcardrecapp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.tintin.hrcardrecapp.R;
import com.tintin.hrcardrecapp.model.HRCardRecForm;

import java.util.ArrayList;

public class QueryActivity extends AppCompatActivity {

    private ArrayList<HRCardRecForm> qHRCardRecs;
    private HRCardRecForm hrcardrecform;
    private static final String LOG_ACTIVITY_TAG = "QueryActivity";

    private Button btn_toFile;
    private TextView txt_qInfo;
    private TableLayout table_query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        //show the form value
        Intent intent = getIntent();
        qHRCardRecs = (ArrayList<HRCardRecForm>) intent.getExtras().getSerializable("QHRCardRecList");
        hrcardrecform = (HRCardRecForm) intent.getExtras().getSerializable("HRCardRecform");

        Log.d(LOG_ACTIVITY_TAG, "HRCardRecForm: "+hrcardrecform.toString());

        initObj();
        genQueryRow();

    }

    public void initObj()
    {
        btn_toFile = (Button) findViewById(R.id.btn_toFile);
        txt_qInfo = (TextView) findViewById(R.id.txt_qInfo);
        String msg = "員編:"+hrcardrecform.getEmpno()+" 查詢自"+hrcardrecform.getReaddt()+"前的打卡記錄";

        txt_qInfo.setText(msg);
        table_query = (TableLayout) findViewById(R.id.table_query);
    }

    public void genQueryRow()
    {
        TextView row_id;
        TextView row_cardtype;
        TextView row_readdt;

        TableRow tbrow0 = new TableRow(this);
        TextView tv0 = new TextView(this);
        tv0.setText(" Sl.No ");
        tv0.setTextColor(Color.BLACK);
        tbrow0.addView(tv0);
        TextView tv1 = new TextView(this);
        tv1.setText(" Product ");
        tv1.setTextColor(Color.BLACK);
        tbrow0.addView(tv1);
        TextView tv2 = new TextView(this);
        tv2.setText(" Unit Price ");
        tv2.setTextColor(Color.BLACK);
        tbrow0.addView(tv2);
        TextView tv3 = new TextView(this);
        tv3.setText(" Stock Remaining ");
        tv3.setTextColor(Color.BLACK);
        table_query.addView(tbrow0);

        /*
        for (int i = 0; i < qHRCardRecs.size(); i++) {
            TableRow row = new TableRow(this);
            TableRow.LayoutParams lParams = new TableRow.LayoutParams(0, 0, 0.5f);
            row.setLayoutParams(lParams);
            row_id = new TextView(this);
            row_id.setText("10");
            row_id.setWidth(20);
            row.addView(row_id);
            table_query.addView(row,i);

            row.addView(checkBox);
            row.addView(minusBtn);
            row.addView(qty);
            row.addView(addBtn);
            ll.addView(row,i);

        }
        */
    }
}
