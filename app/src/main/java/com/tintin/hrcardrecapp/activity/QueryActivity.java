package com.tintin.hrcardrecapp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.tintin.hrcardrecapp.R;
import com.tintin.hrcardrecapp.model.HRCardRecForm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class QueryActivity extends AppCompatActivity {

    private ArrayList<HRCardRecForm> qHRCardRecs;
    private HRCardRecForm hrcardrecform;
    private static final String LOG_ACTIVITY_TAG = "QueryActivity";

    private TextView txt_qInfo;
    private TableLayout table_query;

    private final float FONT_SIZE = 16.0f;

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
        txt_qInfo = (TextView) findViewById(R.id.txt_qInfo);
        String msg = "員編:"+hrcardrecform.getEmpno()
                +" 查詢含"+hrcardrecform.getReaddt().replace(" 23:59:59","")+"前的打卡記錄";

        txt_qInfo.setText(msg);
        table_query = (TableLayout) findViewById(R.id.table_query);
    }

    public void genQueryRow()
    {
        TableRow tbrow0 = new TableRow(this);
        TableRow.LayoutParams lp;
        TextView row_id_lb = new TextView(this);
        row_id_lb.setText("No.");
        row_id_lb.setTextColor(Color.BLACK);
        lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.1f);
        row_id_lb.setLayoutParams(lp);
        row_id_lb.setTextSize(FONT_SIZE);
        tbrow0.addView(row_id_lb);

        TextView row_cardtype_lb = new TextView(this);
        row_cardtype_lb.setText(" 打卡日期時間 ");
        row_cardtype_lb.setTextColor(Color.BLACK);
        lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.6f);
        row_cardtype_lb.setLayoutParams(lp);
        row_cardtype_lb.setTextSize(FONT_SIZE);
        tbrow0.addView(row_cardtype_lb);

        TextView row_readdt_lb = new TextView(this);
        row_readdt_lb.setText(" 類型 ");
        row_readdt_lb.setTextColor(Color.BLACK);
        row_readdt_lb.setTextSize(FONT_SIZE);
        lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.3f);
        row_readdt_lb.setLayoutParams(lp);
        tbrow0.addView(row_readdt_lb);
        table_query.addView(tbrow0);

        if(qHRCardRecs == null)
            return;
        else {
            for (int i = 0; i < qHRCardRecs.size(); i++) {
                tbrow0 = new TableRow(this);

                TextView row_id = new TextView(this);
                row_id.setText(Integer.toString(i + 1));
                row_id.setTextColor(Color.BLACK);
                lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.1f);
                row_id.setLayoutParams(lp);
                row_id.setTextSize(FONT_SIZE);
                tbrow0.addView(row_id);

                TextView row_cardtype = new TextView(this);
                row_cardtype.setText(qHRCardRecs.get(i).getReaddt());
                row_cardtype.setTextColor(Color.BLACK);
                lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.6f);
                row_cardtype.setLayoutParams(lp);
                row_cardtype.setTextSize(FONT_SIZE);
                tbrow0.addView(row_cardtype);

                String cardtype = "";

                if(qHRCardRecs.isEmpty())
                    return;

                switch (qHRCardRecs.get(i).getCardtype()) {
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

                TextView row_readdt = new TextView(this);
                row_readdt.setText(cardtype);
                row_readdt.setTextColor(Color.BLACK);
                lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.3f);
                row_readdt.setLayoutParams(lp);
                row_readdt.setTextSize(FONT_SIZE);
                tbrow0.addView(row_readdt);
                table_query.addView(tbrow0);
            }
        }
    }

    public void onDwBtnClick(View view)
    {
        //write to file
        String buffer = "";

        for(int i = 0; i < qHRCardRecs.size();i++)
        {
            buffer = buffer+"WB"+qHRCardRecs.get(i).getEmpno()+"   "
                    +qHRCardRecs.get(i).getReaddt().replace("-","").replace(":","").replace(" ","")
                    +qHRCardRecs.get(i).getCardtype()+"\n";

        }
        //Log.d(" XXXX ", " BUFFER: "+buffer);
        byte[] b = buffer.getBytes(StandardCharsets.UTF_8);
        String filename = hrcardrecform.getEmpno()+"-"+hrcardrecform.getReaddt()+"打卡紀錄.txt";

        saveData(b, filename);
    }

    private void saveData(byte[] data, String filename){

        //saved file to download folder
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, filename);
        try {
            FileOutputStream stream = new FileOutputStream(file, true);
            stream.write(data);
            stream.close();
            //Log.i("saveData", "Data Saved");
            Toast.makeText(this,"儲存檔案成功, 位於"+Environment.DIRECTORY_DOWNLOADS+"/"
                    + filename+"下"
                    , Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("SAVE DATA", "Could not write file " + e.getMessage());
            Toast.makeText(this,"儲存檔案失敗", Toast.LENGTH_LONG).show();
        }
    }
}
