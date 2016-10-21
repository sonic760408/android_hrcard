package com.tintin.hrcardrecapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by maxhsieh on 2016/10/12.
 */

public class HRCardRecForm implements Serializable{

    private String idno;
    private String empno;
    private String cardtype;
    private String ip;
    private String readdt; //For insert or query
    private String limit; //For query limit

    public HRCardRecForm()
    {}

    public HRCardRecForm(String idno, String empno, String readdt, String cardtype, String ip)
    {
        this.idno = idno;
        this.empno = empno;
        this.readdt = readdt;
        this.cardtype = cardtype;
        this.ip = ip;
    }

    // get/set method

    public void setIdno(String idno)
    {
        this.idno = idno;
    }

    public String getIdno()
    {
        return this.idno;
    }

    public void setEmpno(String empno)
    {
        this.empno = empno;
    }

    public String getEmpno()
    {
        return this.empno;
    }

    public void setReaddt(String readdt)
    {
        this.readdt = readdt;
    }

    public String getReaddt()
    {
        return this.readdt;
    }

    public void setCardtype(String cardtype)
    {
        this.cardtype = cardtype;
    }

    public String getCardtype()
    {
        return this.cardtype;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public String getIp()
    {
        return this.ip;
    }

    public void setLimit(String limit)
    {
        this.limit = limit;
    }

    public String getLimit()
    {
        return this.limit;
    }
    
    @Override
    public String toString()
    {
        return "HRCardRecForm - IDNO: "+idno+", EMPNO: "+empno+", READDT: "+readdt
                + ", CARDTYPE: "+cardtype+", IP: "+ip+", LIMIT: "+limit;
    }

}
