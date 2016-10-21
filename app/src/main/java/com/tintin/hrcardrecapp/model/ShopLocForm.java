package com.tintin.hrcardrecapp.model;

import java.io.Serializable;

/**
 * Created by maxhsieh on 2016/10/13.
 */

public class ShopLocForm implements Serializable {

    private String grpno;
    private String grpname;
    private String grp_lat;  //latitude
    private String grp_lng;  //longitude

    public ShopLocForm() {

    }

    public ShopLocForm(String grpno, String grpname, String grp_lat, String grp_lng) {
        this.grpno = grpno;
        this.grpname = grpname;
        this.grp_lat = grp_lat;
        this.grp_lng = grp_lng;
    }

    public void setGrpno(String grpno) {
        this.grpno = grpno;
    }

    public String getGrpno() {
        return grpno;
    }

    public void setGrpname(String grpname) {
        this.grpname = grpname;
    }

    public String getGrpname() {
        return grpname;
    }

    public void setGrp_lat(String grp_lat) {
        this.grp_lat = grp_lat;
    }

    public String getGrp_lat() {
        return grp_lat;
    }

    public void setGrp_lng(String grp_loc) {
        this.grp_lng = grp_loc;
    }

    public String getGrp_lng() {
        return grp_lng;
    }

    @Override
    public String toString()
    {
        return "GRPNO: "+grpno+", GRPNAME: "+grpname+", GRPLATITUDE: "+grp_lat+", GRPLONGITUDE: "+grp_lng;
    }

}
