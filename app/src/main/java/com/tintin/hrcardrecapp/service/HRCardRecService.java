package com.tintin.hrcardrecapp.service;

import android.util.Log;

import com.tintin.hrcardrecapp.model.HRCardRecForm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxhsieh on 2016/10/12.
 */

public class HRCardRecService {

    private static final String LOG_ACTIVITY_TAG = "TinTin_HRCardService";
    private static final String HRCARDREC_URL = "https://www.norbelbaby.com.tw/HRCardRecWeb_3-1.0/DoJSON";
    private static boolean isError = false;
    private List<HRCardRecForm> hrcardrecforms;

    public boolean getIsError() {
        return isError;
    }

    public void setIsError(boolean isError) {
        HRCardRecService.isError = isError;
    }

    //insert date from app to web ap
    public void insertHRRec(HRCardRecForm hrcardrecform) {
        //connect to website
        final String url = "";
        List<HRCardRecForm> forms;
        HTTPSService httpsService = new HTTPSService();

        //connect to servlet
        String result = httpsService.doConnect(HRCARDREC_URL, "insert", hrcardrecform);

        //parse the result
        String output = "";

        //parse the json is error
        output = errJSONResponse(result);
        if (output.length() != 0) {
            //return error message to dialog
            setIsError(true);
            Log.e(LOG_ACTIVITY_TAG, "發生錯誤!!");
            //return error reason
            output=errJSONResponse(result);
            forms = new ArrayList<HRCardRecForm>();
            forms.add(new HRCardRecForm(null,output,null,null,null)); //set caused on empno items

        } else {
            //show success
            setIsError(false);
            //return hrcardrec info
            forms = JSONgetHRRec(result);
        }
        setHRCardRecForms(forms);
    }

    public List<HRCardRecForm> getHRCardRecForms()
    {
        return hrcardrecforms;
    }

    public void setHRCardRecForms(List<HRCardRecForm> hrcardrecforms)
    {
        this.hrcardrecforms = hrcardrecforms;
    }

    public String errJSONResponse(String json_str) {
        String err_str = "";
        try {
            JSONObject obj = new JSONObject(json_str);

            if (obj.has("error")) {
                err_str = obj.getString("error");
            } else {
                err_str = "";
            }
        } catch (JSONException ex) {
            Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
            err_str = "無法成功讀取資料, 請重新作業"; // AS error, caused: JSONException
        }
        return err_str;
    }

    public List<HRCardRecForm> JSONgetHRRec(String json_str) {
        try {
            JSONObject obj = new JSONObject(json_str);
            JSONObject sub_obj;
            Log.w(LOG_ACTIVITY_TAG, " JSON: "+ json_str);
            JSONArray jsonarray;
            List<HRCardRecForm> hrforms = new ArrayList<>();

            //get hrcardrec info, to package the List object
            jsonarray = obj.getJSONArray("hrcardrec");
            if (jsonarray == null || jsonarray.length() == 0) {
                Log.e(LOG_ACTIVITY_TAG, "XXX NULL XXX");
            }

            for (int i = 0; i < jsonarray.length(); i++) {
                //add to lists
                sub_obj = new JSONObject(jsonarray.getString(i));
                hrforms.add(new HRCardRecForm(null, sub_obj.getString("empno")
                        , sub_obj.getString("datetime"), sub_obj.getString("cardtype"), null));
            }
            return hrforms;

        } catch (JSONException ex) {
            Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
            return null;
        }

    }

    public void queryHRRec(HRCardRecForm hrcardrecform) {
        //query data
        //connect to website
        final String url = "";
        List<HRCardRecForm> forms;
        HTTPSService httpsService = new HTTPSService();

        //connect to servlet
        String result = httpsService.doConnect(HRCARDREC_URL, "query", hrcardrecform);

        //parse the result
        String output = "";

        //parse the json is error
        output = errJSONResponse(result);
        Log.w(LOG_ACTIVITY_TAG, " OUTPUT: "+output);
        if (output.length() != 0) {
            //return error message to dialog
            setIsError(true);
            Log.e(LOG_ACTIVITY_TAG, "發生錯誤!!");
            //return error reason
            output=errJSONResponse(result);
            forms = new ArrayList<HRCardRecForm>();
            forms.add(new HRCardRecForm(null,output,null,null,null)); //set caused on empno items

        } else {
            //show success
            setIsError(false);
            //return hrcardrec info
            forms = JSONgetHRRec(result);
        }
        setHRCardRecForms(forms);
    }

    public void queryHRRecToFile(HRCardRecForm hrcardrecform) {

    }
}
