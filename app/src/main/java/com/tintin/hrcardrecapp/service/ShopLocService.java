package com.tintin.hrcardrecapp.service;

import android.util.Log;

import com.tintin.hrcardrecapp.model.ShopLocForm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by maxhsieh on 2016/10/18.
 */

public class ShopLocService {

    private String curshop_name;
    private List<ShopLocForm> shops = new ArrayList<>();
    private List<String> shop_title = new ArrayList<>();


    private static final String LOG_ACTIVITY_TAG = "ShopLocService";
    private static final String SHOPLOC_URL = "https://www.norbelbaby.com.tw/tintinapp/w/store/json/grid";

    public void setCurShop_name(String name)
    {
        this.curshop_name = name;
    }

    public String getCurShop_name()
    {
        return this.curshop_name;
    }

    public List<ShopLocForm> getShops()
    {
        return shops;
    }

    public ShopLocForm getShopByName(String shopname)
    {
        ShopLocForm shop = new ShopLocForm();

        for(int i=0;i<shops.size();i++)
        {
            if(shops.get(i).getGrpname().equals(shopname)) {
                shop = shops.get(i);
                break;
            }
        }
        return shop;
    }

    public String[] getShopTitleListsArr()
    {
        String[] stockArr = new String[shop_title.size()];
        stockArr = shop_title.toArray(stockArr);
        return stockArr;
    }

    public String getAllShopInfoByWeb()
    {
        ShopLocForm shoplocform = null;
        HTTPSService httpsService = new HTTPSService();

        //connect to servlet to gather shop info
        String result = httpsService.doConnect(SHOPLOC_URL, "shoplocform", shoplocform);

        return result;

    }

    //setting up shop location and name
    public void setAllShopAndLoc() {
        String result = getAllShopInfoByWeb();

        //parse the shop info
        shops = JSONgetShops(result);

        //set shops title
        for(int i =0; i < shops.size(); i++)
        {
            shop_title.add(shops.get(i).getGrpname());
        }
        //get current shop title to show the info

    }

    public List<ShopLocForm> JSONgetShops(String json_str) {
        try {
            JSONObject obj = new JSONObject(json_str);
            JSONObject sub_obj;
            //Log.e(LOG_ACTIVITY_TAG, " AAAA: "+ json_str);
            JSONArray jsonarray;
            List<ShopLocForm> shopforms = new ArrayList<>();

            //get hrcardrec info, to package the List object
            jsonarray = obj.getJSONArray("rows");
            if (jsonarray == null || jsonarray.length() == 0) {
                Log.e(LOG_ACTIVITY_TAG, "XXX NULL XXX");
            }

            for (int i = 0; i < jsonarray.length(); i++) {
                //add to lists
                sub_obj = new JSONObject(jsonarray.getString(i));
                shopforms.add(new ShopLocForm(sub_obj.getString("id"), sub_obj.getString("name"),
                        sub_obj.getString("lat"),sub_obj.getString("lng")));
            }
            return shopforms;

        } catch (JSONException ex) {
            Log.e(LOG_ACTIVITY_TAG, ex.getMessage());
            return null;
        }

    }
}
