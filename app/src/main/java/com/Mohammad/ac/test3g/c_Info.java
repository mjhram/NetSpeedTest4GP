package com.Mohammad.ac.test3g;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mohammad.haider on 2/16/2015.
 */
public class c_Info implements Parcelable{
    //private MainActivity theActivity;
    public String time;//used for sqliteDB timestamp
    private String serverUri;
    public String deviceId;
    public String deviceId2;
    public String manuf;
    public String brand;
    public String model;
    public String product;
    public String imsi;
    public String imsi2;
    public String phoneNumber;
    public String phoneNumber2;
    //public String imei;
    public String netOperator;
    public String netOperator2;
    public String netName;
    public String netName2;
    public int netType;
    public int netType2;
    public String netClass;
    public String netClass2;
    public int phoneType;
    public String mobileState;
    public int cid;
    public int cid_3g;
    public int rnc;
    public int lac;
    public int rssi;//signal Strength
    public String SignalStrengths;
    public double rxRate, txRate;
    public double minRxRate;
    public double maxRxRate;
    public double avRxRate;
    public double minTxRate;
    public double maxTxRate;
    public double avTxRate;
    public double pingTime;
    public double lat, lon;//Location info
    public int cdmaDbm;
    public int cdmaEcio;
    public String neighboringCells;
    //wifi info
    public boolean wifiIsConnected;
    public String wifiSsid;
    public String netSource; //mobile data, WiFi or NA

    public String tmp;
    final private String TABLE_3gTests = "netTests";


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //dest.writeString(time);
        dest.writeString(deviceId);
        dest.writeString(deviceId2);
        dest.writeString(manuf);
        dest.writeString(brand);
        dest.writeString(model);
        dest.writeString(product);
        dest.writeString(imsi);
        dest.writeString(imsi2);
        dest.writeString(phoneNumber);
        dest.writeString(phoneNumber2);
        //dest.writeString(imei);
        dest.writeString(netOperator);
        dest.writeString(netOperator2);
        dest.writeString(netName);
        dest.writeString(netName2);
        dest.writeInt(netType);
        dest.writeInt(netType2);
        dest.writeString(netClass);
        dest.writeString(netClass2);
        dest.writeInt(phoneType);
        dest.writeString(mobileState);
        dest.writeInt(cid);
        dest.writeInt(cid_3g);
        dest.writeInt(rnc);
        dest.writeInt(lac);
        dest.writeInt(rssi);
        dest.writeString(SignalStrengths);
        dest.writeDouble(rxRate);
        dest.writeDouble(txRate);
        dest.writeDouble(minRxRate);
        dest.writeDouble(maxRxRate);
        dest.writeDouble(avRxRate);
        dest.writeDouble(minTxRate);
        dest.writeDouble(maxTxRate);
        dest.writeDouble(avTxRate);
        dest.writeDouble(pingTime);
        dest.writeDouble(lat);
        dest.writeDouble(lon);
        dest.writeString(this.neighboringCells);
        dest.writeInt(cdmaDbm);
        dest.writeInt(cdmaEcio);
        dest.writeString(wifiSsid);
        dest.writeValue(wifiIsConnected);
        dest.writeString(netSource);
        dest.writeString(tmp);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {
        //time = in.readString();
        deviceId=in.readString();
        deviceId2=in.readString();
        manuf=in.readString();
        brand=in.readString();
        model=in.readString();
        product=in.readString();
        imsi=in.readString();
        imsi2=in.readString();
        phoneNumber=in.readString();
        phoneNumber2=in.readString();
        //imei=in.readString();
        netOperator=in.readString();
        netOperator2=in.readString();
        netName=in.readString();
        netName2=in.readString();
        netType=in.readInt();
        netType2=in.readInt();
        netClass=in.readString();
        netClass2=in.readString();
        phoneType=in.readInt();
        mobileState=in.readString();
        cid=in.readInt();
        cid_3g=in.readInt();
        rnc=in.readInt();
        lac=in.readInt();
        rssi=in.readInt();
        SignalStrengths=in.readString();
        rxRate = in.readDouble();
        txRate = in.readDouble();
        minRxRate=in.readDouble();
        maxRxRate=in.readDouble();
        avRxRate=in.readDouble();
        minTxRate=in.readDouble();
        maxTxRate=in.readDouble();
        avTxRate=in.readDouble();
        pingTime=in.readDouble();
        lat=in.readDouble();
        lon=in.readDouble();
        neighboringCells = in.readString();
        cdmaDbm=in.readInt();
        cdmaEcio = in.readInt();
        wifiSsid = in.readString();
        wifiIsConnected = (Boolean) in.readValue(null);
        netSource = in.readString();
        tmp = in.readString();
    }

    //private Activity theActivity;
    public c_Info(String uri) {
        //theActivity = activity;
        serverUri = uri;
    }

    public c_Info(Parcel in){
        //theActivity = activity;
        readFromParcel(in);
    }

    public static final Creator<c_Info> CREATOR = new Creator<c_Info>() {

        @Override
        public c_Info createFromParcel(Parcel source) {
            return new c_Info(source);
        }

        @Override
        public c_Info[] newArray(int size) {
            return new c_Info[size];
        }
    };

    void upload(Context cntx){
        //new uploadInfo(cntx).execute();
        add3GTest(cntx);
    }

    public void add3GTest(final Context cntx)
    {
        // Tag used to cancel the request
        final String tag_string = "add3GTest";

        String url_dbsite = serverUri + "/addTest.php";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                url_dbsite, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d(tag_string, "AddTReq Response: " + response);
                Intent resultsIntent=new Intent("com.Mohammad.ac.test3g.DONE");
                resultsIntent.putExtra("DONE", true);
                LocalBroadcastManager localBroadcastManager =LocalBroadcastManager.getInstance(cntx);
                localBroadcastManager.sendBroadcast(resultsIntent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(tag_string, "addRequest Error: " + error.getMessage());
                //Toast.makeText(cx,
                //      error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", tag_string);
                String tmp;

                tmp = deviceId;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("deviceId", tmp);
                tmp = imsi;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("imsi", imsi);
                tmp = phoneNumber;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("phoneNumber", tmp);
                params.put("imei", "");//imei);
                tmp = netOperator;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("netOperator", tmp);
                tmp = netName;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("netName", tmp);

                params.put("netType", Integer.toString(netType));
                tmp = netClass;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("netClass", tmp);
                params.put("phoneType", Integer.toString(phoneType));
                tmp = mobileState;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("mobileState", tmp);
                params.put("cid", Integer.toString(cid));
                params.put("cid_3g", Integer.toString(cid_3g));
                params.put("rnc", Integer.toString(rnc));
                params.put("lac", Integer.toString(lac));
                params.put("rssi", Integer.toString(rssi));
                params.put("SignalStrengths",SignalStrengths);
                String tmpStr = String.format("%d", Math.round(minRxRate));
                params.put("minRxRate", tmpStr);
                params.put("maxRxRate", String.format("%d", Math.round(maxRxRate)));
                params.put("avRxRate", String.format("%d", Math.round(avRxRate)));
                params.put("minTxRate", String.format("%d", Math.round(minTxRate)));
                params.put("maxTxRate", String.format("%d", Math.round(maxTxRate)));
                params.put("avTxRate", String.format("%d", Math.round(avTxRate)));
                params.put("lon", Double.toString(lon));
                params.put("lat", Double.toString(lat));
                tmp = brand;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("brand", tmp);
                tmp = manuf;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("manuf", tmp);
                tmp = product;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("product", tmp);
                tmp = model;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("model", tmp);
                tmp = deviceId2;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("deviceId2", tmp);
                tmp = imsi2;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("imsi2", tmp);
                tmp = phoneNumber2;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("phoneNum2", tmp);
                tmp = netOperator2;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("netOperator2", tmp);
                tmp = netName2;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("netName2", tmp);
                params.put("netType2", Integer.toString(netType2));
                tmp = netClass2;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("netClass2", tmp);
                tmp = neighboringCells;
                if(tmp == null)  {
                    tmp ="";
                }
                params.put("nei", tmp);
                params.put("cdmaDbm", Integer.toString(cdmaDbm));
                params.put("cdmaEcio", Integer.toString(cdmaEcio));
                params.put("wifissid", wifiSsid);
                params.put("netsrc", netSource);
                params.put("tmp", "");
                return params;
            }
        };
        // Adding request to request queue
        strReq.setTag(tag_string);
        getRequestQueue(cntx).add(strReq);
    }

    static public c_Info getInfoFromRow(Cursor in) {
        c_Info tmpMobInfo = new c_Info("");

        tmpMobInfo.time=in.getString(in.getColumnIndex("time"));
        tmpMobInfo.deviceId=in.getString(in.getColumnIndex("deviceId"));
        tmpMobInfo.deviceId2=in.getString(in.getColumnIndex("deviceId2"));
        tmpMobInfo.manuf=in.getString(in.getColumnIndex("Manufacturer"));
        tmpMobInfo.brand=in.getString(in.getColumnIndex("Brand"));
        tmpMobInfo.model=in.getString(in.getColumnIndex("Model"));
        tmpMobInfo.product=in.getString(in.getColumnIndex("Product"));
        tmpMobInfo.imsi=in.getString(in.getColumnIndex("imsi"));
        tmpMobInfo.imsi2=in.getString(in.getColumnIndex("imsi2"));
        tmpMobInfo.phoneNumber=in.getString(in.getColumnIndex("phoneNumber"));
        tmpMobInfo.phoneNumber2=in.getString(in.getColumnIndex("phoneNum2"));
        String imei=in.getString(in.getColumnIndex("imei"));
        tmpMobInfo.netOperator=in.getString(in.getColumnIndex("netOperator"));
        tmpMobInfo.netOperator2=in.getString(in.getColumnIndex("netOperator2"));
        tmpMobInfo.netName=in.getString(in.getColumnIndex("netName"));
        tmpMobInfo.netName2=in.getString(in.getColumnIndex("netName2"));
        tmpMobInfo.netType=in.getInt(in.getColumnIndex("netType"));
        tmpMobInfo.netType2=in.getInt(in.getColumnIndex("netType2"));
        tmpMobInfo.netClass=in.getString(in.getColumnIndex("netClass"));
        tmpMobInfo.netClass2=in.getString(in.getColumnIndex("netClass2"));
        tmpMobInfo.phoneType=in.getInt(in.getColumnIndex("phoneType"));
        tmpMobInfo.mobileState=in.getString(in.getColumnIndex("mobileState"));
        tmpMobInfo.cid=in.getInt(in.getColumnIndex("cid"));
        tmpMobInfo.cid_3g=in.getInt(in.getColumnIndex("cid_3g"));
        tmpMobInfo.rnc=in.getInt(in.getColumnIndex("rnc"));
        tmpMobInfo.lac=in.getInt(in.getColumnIndex("lac"));
        tmpMobInfo.rssi=in.getInt(in.getColumnIndex("rssi"));
        tmpMobInfo.SignalStrengths=in.getString(in.getColumnIndex("SignalStrengths"));
        //tmpMobInfo.rxRate = in.getDouble(in.getColumnIndex("EmployeeName"));
        //tmpMobInfo.txRate = in.getDouble(in.getColumnIndex("EmployeeName"));
        tmpMobInfo.minRxRate=in.getDouble(in.getColumnIndex("minRxRate"));
        tmpMobInfo.maxRxRate=in.getDouble(in.getColumnIndex("maxRxRate"));
        tmpMobInfo.avRxRate=in.getDouble(in.getColumnIndex("avRxRate"));
        tmpMobInfo.minTxRate=in.getDouble(in.getColumnIndex("minTxRate"));
        tmpMobInfo.maxTxRate=in.getDouble(in.getColumnIndex("maxTxRate"));
        tmpMobInfo.avTxRate=in.getDouble(in.getColumnIndex("avTxRate"));
        //tmpMobInfo.pingTime=in.getDouble(in.getColumnIndex("EmployeeName"));
        tmpMobInfo.lat=in.getDouble(in.getColumnIndex("lat"));
        tmpMobInfo.lon=in.getDouble(in.getColumnIndex("lon"));
        tmpMobInfo.neighboringCells = in.getString(in.getColumnIndex("nei"));
        tmpMobInfo.cdmaDbm=in.getInt(in.getColumnIndex("cdmaDbm"));
        tmpMobInfo.cdmaEcio = in.getInt(in.getColumnIndex("cdmaEcio"));
        tmpMobInfo.wifiSsid = in.getString(in.getColumnIndex("wifissid"));
        tmpMobInfo.netSource = in.getString(in.getColumnIndex("netsrc"));

        tmpMobInfo.tmp = in.getString(in.getColumnIndex("tmp"));
        return tmpMobInfo;
    }

    public void add3gTest2db(SQLiteDatabase db)
    {
        // Tag used to cancel the request
        final String tag_string = "add3GTest";
        ContentValues params = new ContentValues();
        tmp = deviceId;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("deviceId", tmp);
        tmp = imsi;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("imsi", imsi);
        tmp = phoneNumber;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("phoneNumber", tmp);
        params.put("imei", "");//imei);
        tmp = netOperator;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("netOperator", tmp);
        tmp = netName;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("netName", tmp);

        params.put("netType", Integer.toString(netType));
        tmp = netClass;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("netClass", tmp);
        params.put("phoneType", Integer.toString(phoneType));
        tmp = mobileState;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("mobileState", tmp);
        params.put("cid", Integer.toString(cid));
        params.put("cid_3g", Integer.toString(cid_3g));
        params.put("rnc", Integer.toString(rnc));
        params.put("lac", Integer.toString(lac));
        params.put("rssi", Integer.toString(rssi));
        params.put("SignalStrengths",SignalStrengths);
        String tmpStr = String.format("%d", Math.round(minRxRate));
        params.put("minRxRate", tmpStr);
        params.put("maxRxRate", String.format("%d", Math.round(maxRxRate)));
        params.put("avRxRate", String.format("%d", Math.round(avRxRate)));
        params.put("minTxRate", String.format("%d", Math.round(minTxRate)));
        params.put("maxTxRate", String.format("%d", Math.round(maxTxRate)));
        params.put("avTxRate", String.format("%d", Math.round(avTxRate)));
        params.put("lon", Double.toString(lon));
        params.put("lat", Double.toString(lat));
        tmp = brand;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("Brand", tmp);
        tmp = manuf;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("Manufacturer", tmp);
        tmp = product;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("Product", tmp);
        tmp = model;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("Model", tmp);
        tmp = deviceId2;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("deviceId2", tmp);
        tmp = imsi2;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("imsi2", tmp);
        tmp = phoneNumber2;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("phoneNum2", tmp);
        tmp = netOperator2;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("netOperator2", tmp);
        tmp = netName2;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("netName2", tmp);
        params.put("netType2", Integer.toString(netType2));
        tmp = netClass2;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("netClass2", tmp);
        tmp = neighboringCells;
        if(tmp == null)  {
            tmp ="";
        }
        params.put("nei", tmp);
        params.put("cdmaDbm", Integer.toString(cdmaDbm));
        params.put("cdmaEcio", Integer.toString(cdmaEcio));
        params.put("wifissid", wifiSsid);
        params.put("netsrc", netSource);

        params.put("tmp", "");
        long tmp =  db.insert(TABLE_3gTests, null, params);
        Log.d("Test",Long.toString(tmp));
    }

    private RequestQueue mRequestQueue;
    public RequestQueue getRequestQueue(Context cntx) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(cntx);
        }
        return mRequestQueue;
    }

    public void showInfo(MainActivity theActivity) {
        theActivity.txt_model.setText(manuf.toUpperCase() + "/" + model);
        theActivity.txt_netclass.setText(netClass+" - "+netClass2);
        theActivity.txt_netname.setText(netName+" - "+netName2);
        if(netClass.equals("2G")) {
            theActivity.txt_cellid.setText(""+cid);
            theActivity.txt_rnc.setText("");
        }else {
            theActivity.txt_cellid.setText(String.format("%04d",cid_3g));
            theActivity.txt_rnc.setText(""+rnc);
        }
        theActivity.txt_lac.setText(""+lac);
        theActivity.txt_rssi.setText(""+rssi);
        theActivity.txt_minmaxrx.setText(MainActivity.getRateWithUnit(minRxRate)+", "+MainActivity.getRateWithUnit(maxRxRate)+", "+MainActivity.getRateWithUnit(avRxRate));
        theActivity.txt_minmaxtx.setText(MainActivity.getRateWithUnit(minTxRate)+", "+MainActivity.getRateWithUnit(maxTxRate)+", "+MainActivity.getRateWithUnit(avTxRate));
        theActivity.txt_latitude.setText(""+lat + ", " + +lon);

        theActivity.txt_neighboring.setText(this.neighboringCells);
        theActivity.txt_cdmaDbm.setText("" + this.cdmaDbm);
        theActivity.txt_cdmaEcio.setText("" + this.cdmaEcio);

    }
}
