package com.tintin.hrcardrecapp.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by maxhsieh on 2016/10/12.
 */

public class ErrorDialog  extends AppCompatActivity {

    public void ShowErrorDialog(Context context, String name_class , String msg) {
        AlertDialog.Builder _AlertDialog = new AlertDialog.Builder(context);
        _AlertDialog.setTitle(name_class+" class 錯誤");
        _AlertDialog.setMessage("原因: "+msg);
        _AlertDialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                //Toast.makeText(getBaseContext(), "按左邊", Toast.LENGTH_SHORT).show();
            }
        });
        _AlertDialog.show();
    }

    public void ShowErrorDialog(Context context, String msg) {
        AlertDialog.Builder _AlertDialog = new AlertDialog.Builder(context);
        _AlertDialog.setTitle("錯誤");
        _AlertDialog.setMessage("原因: "+msg);
        _AlertDialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                //Toast.makeText(getBaseContext(), "按左邊", Toast.LENGTH_SHORT).show();
            }
        });
        _AlertDialog.show();
    }

    public void ShowSuccessDialog(Context context , String msg) {
        AlertDialog.Builder _AlertDialog = new AlertDialog.Builder(context);
        _AlertDialog.setTitle("成功");
        _AlertDialog.setMessage(msg);
        _AlertDialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                //Toast.makeText(getBaseContext(), "按左邊", Toast.LENGTH_SHORT).show();
            }
        });
        _AlertDialog.show();
    }
}
