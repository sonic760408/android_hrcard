package com.tintin.hrcardrecapp.service;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.tintin.hrcardrecapp.model.HRCardRecForm;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.apache.http.util.EntityUtils;

/**
 * Created by maxhsieh on 2016/10/14.
 */

public class HTTPSService {

    private static final String LOG_ACTIVITY_TAG = "HTTPSService";

    public String genHRCardFormJSON(HRCardRecForm hrcardrecform, String type) {
        String json_str = "";
        JSONObject jsonHRCardRecObj = new JSONObject();
        try {
            jsonHRCardRecObj.put("empno", hrcardrecform.getEmpno());
            jsonHRCardRecObj.put("datetime", hrcardrecform.getReaddt());
            jsonHRCardRecObj.put("cardtype", hrcardrecform.getCardtype());
            if(type.equalsIgnoreCase("insert")) {
                jsonHRCardRecObj.put("ip", hrcardrecform.getIp());
                jsonHRCardRecObj.put("idno", hrcardrecform.getIdno());
                jsonHRCardRecObj.put("limit","");
            }
            else if(type.equalsIgnoreCase("query")){
                jsonHRCardRecObj.put("ip", "");
                jsonHRCardRecObj.put("idno", "");
                jsonHRCardRecObj.put("limit",hrcardrecform.getLimit());
            }

            json_str = jsonHRCardRecObj.toString();
        } catch (Exception ex) {
            Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
        }
        return json_str;
    }

    public String doConnect(String url, String req_type, Object form) {

        //Change hrcardrecform as jsonstring
        String json_form_str = "";
        json_form_str = genHRCardFormJSON((HRCardRecForm) form,req_type);


        //Log.e(LOG_ACTIVITY_TAG, "json_form_str: " + json_form_str.toString());

        String output = null;
        try {
            output = new ConnectToWebTask().execute(url, req_type, json_form_str).get();
        } catch (Exception ex) {
            Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
        }
        //Log.e(LOG_ACTIVITY_TAG, "json_form_str: " + output);
        return output;
    }

}

class ConnectToWebTask extends AsyncTask<String, String, String> {
    private static final String LOG_ACTIVITY_TAG = "ConnectToWebAsync";
    private static final int HTTP_CONNECT_TIME_OUT = 5000;
    private static final int HTTP_READ_TIME_OUT = 10000;

    HttpsURLConnection con;

    protected void onPreExecute() {
        //display progress dialog.
    }

    @Override
    protected String doInBackground(String... params) {

        String str = sendPostReq(params[0], params[1], params[2]);
        return str; //must return same type with onPostExecute() to trigger it
    }

    private String sendPostReq(String https_url, String req_type, String json_form_str) {
        BufferedReader bufferedReader = null;
        PrintWriter printWriter = null;
        String result = "";
        try {
            URL url = new URL(https_url);
            con = (HttpsURLConnection) url.openConnection();

            // set Timeout and method
            con.setReadTimeout(HTTP_CONNECT_TIME_OUT);
            con.setConnectTimeout(HTTP_READ_TIME_OUT);

            // set json request header
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            //con.setRequestProperty("Accept","application/json");
            con.setRequestProperty("accept", "*/*");
            con.setRequestProperty("connection", "Keep-Alive");
            //simulate android phono to request
            con.setRequestProperty("user-agent", "Mozilla/5.0 (Android 4.4; Mobile; rv:41.0) Gecko/41.0 Firefox/41.0");

            //set POST
            con.setRequestMethod("POST");
            con.setDoOutput(true); //send data
            con.setDoInput(true);  //recv data
            con.setUseCaches(false); //disable cache (JSON)

            // set json format
            String json = "";

            //build jsonObject
            JSONObject jsonObj = new JSONObject();
            if(req_type.equalsIgnoreCase("insert"))
                jsonObj.put("request", "insert");
            else if(req_type.equalsIgnoreCase("query"))
                jsonObj.put("request", "query");

            jsonObj.put("hrcardrec", json_form_str);

            //json to https body
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            byte[] outputInBytes = jsonObj.toString().getBytes("UTF-8");
            OutputStream os = con.getOutputStream();
            os.write(outputInBytes);
            os.close();

            //connect
            Log.w(LOG_ACTIVITY_TAG, "****** Connect  ********");
            con.connect();

            //dumpl all cert info
            //print_https_cert(con);

            //dump all the content
            result = get_content(con);

        } catch (MalformedURLException ex) {
            Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
        } catch (IOException ex) {
            Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
        } catch (JSONException ex) {
            Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
        } finally {
            if (null != bufferedReader) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    // TODO Auto-generated catch block
                    Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
                }
            }
            if (null != printWriter) {
                printWriter.close();
            }
            if (null != con)
                con.disconnect();
            return result;
        }
    }

    private void print_https_cert(HttpsURLConnection con) {

        if (con != null) {
            try {
                Log.e(LOG_ACTIVITY_TAG, "Response Code : " + con.getResponseCode());
                Log.e(LOG_ACTIVITY_TAG, "Cipher Suite : " + con.getCipherSuite());

                Certificate[] certs = con.getServerCertificates();
                for (Certificate cert : certs) {
                    Log.e(LOG_ACTIVITY_TAG, "Cert Type : " + cert.getType());
                    Log.e(LOG_ACTIVITY_TAG, "Cert Hash Code : " + cert.hashCode());
                    Log.e(LOG_ACTIVITY_TAG, "Cert Public Key Algorithm : "
                            + cert.getPublicKey().getAlgorithm());
                    Log.e(LOG_ACTIVITY_TAG, "Cert Public Key Format : "
                            + cert.getPublicKey().getFormat());
                }

            } catch (SSLPeerUnverifiedException ex) {
                Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
            } catch (IOException ex) {
                Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
            }
        }
    }

    private String get_content(HttpsURLConnection con) {
        if (con != null) {
            BufferedReader br;
            String line = "";
            try {
                br = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String input;

                //output return value
                while ((input = br.readLine()) != null) {
                    line = input;
                }
                //Do parser
                br.close();

            } catch (IOException ex) {
                Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
            }
            return line;
        }
        return null;
    }

    protected void onProgressUpdate(Void... progress) {

    }

    protected void onPostExecute(String result) {

        if (con != null)
            con.disconnect();
        Log.w(LOG_ACTIVITY_TAG, "****** Disconnect  ********");
        //return body
        super.onPostExecute(result);

    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException ex) {
            Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
