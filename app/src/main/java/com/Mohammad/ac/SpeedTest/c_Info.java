package com.Mohammad.ac.SpeedTest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.Locale;

/**
 * Created by mohammad.haider on 2/16/2015.
 */
public class c_Info implements Parcelable{
    //boolean serverUp = false;
    //private MainActivity theActivity;
    public String time="";//used for sqliteDB timestamp
    private String serverUri="";
    public String deviceId="";
    public String deviceId2="";
    public String manuf="";
    public String brand="";
    public String model="";
    public String product="";
    public String imsi="";
    public String imsi2="";
    public String phoneNumber="";
    public String phoneNumber2="";
    //public String imei;
    public String netOperator="";
    public String netOperator2="";
    public String netName="";
    public String netName2="";
    public int netType;
    public int netType2;
    public String netClass="";
    public String netClass2="";
    public int phoneType;
    public String mobileState="";
    public int cid;
    public int cid_3g;
    public int rnc;
    public int lac;
    public int rssi;//signal Strength
    public String SignalStrengths="";
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
    public String neighboringCells="";
    //wifi info
    public boolean wifiIsConnected;
    public String wifiSsid="";
    public String netSource=""; //mobile data, WiFi or NA

    public String tmp="";
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
        Object wifiObj = in.readValue(getClass().getClassLoader());
        wifiIsConnected = ((wifiObj != null) && ((Boolean) wifiObj));
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

    /*void upload(Context cntx){
        if(serverUp) {
            //new uploadInfo(cntx).execute();
            add3GTest(cntx);
        }
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
                Map<String, String> params = new HashMap<>();
                params.put("tag", tag_string);

                params.put("deviceId", deviceId != null ? deviceId : "");
                params.put("imsi", imsi != null ? imsi : "");
                params.put("phoneNumber", phoneNumber != null ? phoneNumber : "");
                params.put("imei", "");//imei);
                params.put("netOperator", netOperator != null ? netOperator : "");
                params.put("netName", netName != null ? netName : "");

                params.put("netType", String.valueOf(netType));
                params.put("netClass", netClass != null ? netClass : "");
                params.put("phoneType", String.valueOf(phoneType));
                params.put("mobileState", mobileState != null ? mobileState : "");
                params.put("cid", String.valueOf(cid));
                params.put("cid_3g", String.valueOf(cid_3g));
                params.put("rnc", String.valueOf(rnc));
                params.put("lac", String.valueOf(lac));
                params.put("rssi", String.valueOf(rssi));
                params.put("SignalStrengths",SignalStrengths);
                String rxMinStr = String.format(Locale.getDefault(), "%d", Math.round(minRxRate));
                params.put("minRxRate", rxMinStr);
                params.put("maxRxRate", String.format(Locale.getDefault(), "%d", Math.round(maxRxRate)));
                params.put("avRxRate", String.format(Locale.getDefault(), "%d", Math.round(avRxRate)));
                params.put("minTxRate", String.format(Locale.getDefault(), "%d", Math.round(minTxRate)));
                params.put("maxTxRate", String.format(Locale.getDefault(), "%d", Math.round(maxTxRate)));
                params.put("avTxRate", String.format(Locale.getDefault(), "%d", Math.round(avTxRate)));
                params.put("lon", Double.toString(lon));
                params.put("lat", Double.toString(lat));
                params.put("brand", brand != null ? brand : "");
                params.put("manuf", manuf != null ? manuf : "");
                params.put("product", product != null ? product : "");
                params.put("model", model != null ? model : "");
                params.put("deviceId2", deviceId2 != null ? deviceId2 : "");
                params.put("imsi2", imsi2 != null ? imsi2 : "");
                params.put("phoneNum2", phoneNumber2 != null ? phoneNumber2 : "");
                params.put("netOperator2", netOperator2 != null ? netOperator2 : "");
                params.put("netName2", netName2 != null ? netName2 : "");
                params.put("netType2", String.valueOf(netType2));
                params.put("netClass2", netClass2 != null ? netClass2 : "");
                params.put("nei", neighboringCells != null ? neighboringCells : "");
                params.put("cdmaDbm", String.valueOf(cdmaDbm));
                params.put("cdmaEcio", String.valueOf(cdmaEcio));
                params.put("wifissid", wifiSsid);
                params.put("netsrc", netSource);
                params.put("tmp", "");
                return params;
            }
        };
        // Adding request to request queue
        strReq.setTag(tag_string);
        getRequestQueue(cntx).add(strReq);
    }*/

    static public c_Info getInfoFromRow(Cursor in) {
        c_Info tmpMobInfo = new c_Info("");

        int index;
        if ((index = in.getColumnIndex("time")) != -1) tmpMobInfo.time = in.getString(index);
        if ((index = in.getColumnIndex("deviceId")) != -1) tmpMobInfo.deviceId = in.getString(index);
        if ((index = in.getColumnIndex("deviceId2")) != -1) tmpMobInfo.deviceId2 = in.getString(index);
        if ((index = in.getColumnIndex("Manufacturer")) != -1) tmpMobInfo.manuf = in.getString(index);
        if ((index = in.getColumnIndex("Brand")) != -1) tmpMobInfo.brand = in.getString(index);
        if ((index = in.getColumnIndex("Model")) != -1) tmpMobInfo.model = in.getString(index);
        if ((index = in.getColumnIndex("Product")) != -1) tmpMobInfo.product = in.getString(index);
        if ((index = in.getColumnIndex("imsi")) != -1) tmpMobInfo.imsi = in.getString(index);
        if ((index = in.getColumnIndex("imsi2")) != -1) tmpMobInfo.imsi2 = in.getString(index);
        if ((index = in.getColumnIndex("phoneNumber")) != -1) tmpMobInfo.phoneNumber = in.getString(index);
        if ((index = in.getColumnIndex("phoneNum2")) != -1) tmpMobInfo.phoneNumber2 = in.getString(index);
        if ((index = in.getColumnIndex("netOperator")) != -1) tmpMobInfo.netOperator = in.getString(index);
        if ((index = in.getColumnIndex("netOperator2")) != -1) tmpMobInfo.netOperator2 = in.getString(index);
        if ((index = in.getColumnIndex("netName")) != -1) tmpMobInfo.netName = in.getString(index);
        if ((index = in.getColumnIndex("netName2")) != -1) tmpMobInfo.netName2 = in.getString(index);
        if ((index = in.getColumnIndex("netType")) != -1) tmpMobInfo.netType = in.getInt(index);
        if ((index = in.getColumnIndex("netType2")) != -1) tmpMobInfo.netType2 = in.getInt(index);
        if ((index = in.getColumnIndex("netClass")) != -1) tmpMobInfo.netClass = in.getString(index);
        if ((index = in.getColumnIndex("netClass2")) != -1) tmpMobInfo.netClass2 = in.getString(index);
        if ((index = in.getColumnIndex("phoneType")) != -1) tmpMobInfo.phoneType = in.getInt(index);
        if ((index = in.getColumnIndex("mobileState")) != -1) tmpMobInfo.mobileState = in.getString(index);
        if ((index = in.getColumnIndex("cid")) != -1) tmpMobInfo.cid = in.getInt(index);
        if ((index = in.getColumnIndex("cid_3g")) != -1) tmpMobInfo.cid_3g = in.getInt(index);
        if ((index = in.getColumnIndex("rnc")) != -1) tmpMobInfo.rnc = in.getInt(index);
        if ((index = in.getColumnIndex("lac")) != -1) tmpMobInfo.lac = in.getInt(index);
        if ((index = in.getColumnIndex("rssi")) != -1) tmpMobInfo.rssi = in.getInt(index);
        if ((index = in.getColumnIndex("SignalStrengths")) != -1) tmpMobInfo.SignalStrengths = in.getString(index);
        if ((index = in.getColumnIndex("minRxRate")) != -1) tmpMobInfo.minRxRate = in.getDouble(index);
        if ((index = in.getColumnIndex("maxRxRate")) != -1) tmpMobInfo.maxRxRate = in.getDouble(index);
        if ((index = in.getColumnIndex("avRxRate")) != -1) tmpMobInfo.avRxRate = in.getDouble(index);
        if ((index = in.getColumnIndex("minTxRate")) != -1) tmpMobInfo.minTxRate = in.getDouble(index);
        if ((index = in.getColumnIndex("maxTxRate")) != -1) tmpMobInfo.maxTxRate = in.getDouble(index);
        if ((index = in.getColumnIndex("avTxRate")) != -1) tmpMobInfo.avTxRate = in.getDouble(index);
        if ((index = in.getColumnIndex("lat")) != -1) tmpMobInfo.lat = in.getDouble(index);
        if ((index = in.getColumnIndex("lon")) != -1) tmpMobInfo.lon = in.getDouble(index);
        if ((index = in.getColumnIndex("nei")) != -1) tmpMobInfo.neighboringCells = in.getString(index);
        if ((index = in.getColumnIndex("cdmaDbm")) != -1) tmpMobInfo.cdmaDbm = in.getInt(index);
        if ((index = in.getColumnIndex("cdmaEcio")) != -1) tmpMobInfo.cdmaEcio = in.getInt(index);
        if ((index = in.getColumnIndex("wifissid")) != -1) tmpMobInfo.wifiSsid = in.getString(index);
        if ((index = in.getColumnIndex("netsrc")) != -1) tmpMobInfo.netSource = in.getString(index);
        if ((index = in.getColumnIndex("tmp")) != -1) tmpMobInfo.tmp = in.getString(index);

        return tmpMobInfo;
    }

    public void add3gTest2db(SQLiteDatabase db)
    {
        ContentValues params = new ContentValues();

        params.put("deviceId", deviceId != null ? deviceId : "");
        params.put("imsi", imsi != null ? imsi : "");
        params.put("phoneNumber", phoneNumber != null ? phoneNumber : "");
        params.put("imei", "");//imei);
        params.put("netOperator", netOperator != null ? netOperator : "");
        params.put("netName", netName != null ? netName : "");

        params.put("netType", netType);
        params.put("netClass", netClass != null ? netClass : "");
        params.put("phoneType", phoneType);
        params.put("mobileState", mobileState != null ? mobileState : "");
        params.put("cid", cid);
        params.put("cid_3g", cid_3g);
        params.put("rnc", rnc);
        params.put("lac", lac);
        params.put("rssi", rssi);
        params.put("SignalStrengths",SignalStrengths);
        params.put("minRxRate", minRxRate);
        params.put("maxRxRate", maxRxRate);
        params.put("avRxRate", avRxRate);
        params.put("minTxRate", minTxRate);
        params.put("maxTxRate", maxTxRate);
        params.put("avTxRate", avTxRate);
        params.put("lon", lon);
        params.put("lat", lat);
        params.put("Brand", brand != null ? brand : "");
        params.put("Manufacturer", manuf != null ? manuf : "");
        params.put("Product", product != null ? product : "");
        params.put("Model", model != null ? model : "");
        params.put("deviceId2", deviceId2 != null ? deviceId2 : "");
        params.put("imsi2", imsi2 != null ? imsi2 : "");
        params.put("phoneNum2", phoneNumber2 != null ? phoneNumber2 : "");
        params.put("netOperator2", netOperator2 != null ? netOperator2 : "");
        params.put("netName2", netName2 != null ? netName2 : "");
        params.put("netType2", netType2);
        params.put("netClass2", netClass2 != null ? netClass2 : "");
        params.put("nei", neighboringCells != null ? neighboringCells : "");
        params.put("cdmaDbm", cdmaDbm);
        params.put("cdmaEcio", cdmaEcio);
        params.put("wifissid", wifiSsid);
        params.put("netsrc", netSource);

        params.put("tmp", "");
        long insertedId =  db.insert(TABLE_3gTests, null, params);
        Log.d("Test",Long.toString(insertedId));
    }

    private RequestQueue mRequestQueue;
    public RequestQueue getRequestQueue(Context cntx) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(cntx);
        }
        return mRequestQueue;
    }

    public void showInfo(MainActivity theActivity) {
        theActivity.txt_model.setText(String.format("%s/%s", manuf.toUpperCase(), model));
        theActivity.txt_netclass.setText(String.format("%s - %s", netClass, netClass2));
        theActivity.txt_netname.setText(String.format("%s - %s", netName, netName2));
        if("2G".equals(netClass)) {
            theActivity.txt_cellid.setText(String.valueOf(cid));
            theActivity.txt_rnc.setText("");
        }else {
            theActivity.txt_cellid.setText(String.format(Locale.getDefault(), "%04d", cid_3g));
            if(rnc==0) {
                theActivity.txt_rnc.setText("");
            }else {
                theActivity.txt_rnc.setText(String.valueOf(rnc));
            }
        }
        theActivity.txt_lac.setText(String.valueOf(lac));
        theActivity.txt_rssi.setText(String.valueOf(rssi));
        theActivity.txt_minmaxrx.setText(String.format("%s, %s, %s", MainActivity.getRateWithUnit(minRxRate), MainActivity.getRateWithUnit(maxRxRate), MainActivity.getRateWithUnit(avRxRate)));
        theActivity.txt_minmaxtx.setText(String.format("%s, %s, %s", MainActivity.getRateWithUnit(minTxRate), MainActivity.getRateWithUnit(maxTxRate), MainActivity.getRateWithUnit(avTxRate)));
        theActivity.txt_latitude.setText(String.format(Locale.getDefault(), "%.6f, %.6f", lat, lon));

        theActivity.txt_neighboring.setText(this.neighboringCells);
        theActivity.txt_cdmaDbm.setText(String.valueOf(this.cdmaDbm));
        theActivity.txt_cdmaEcio.setText(String.valueOf(this.cdmaEcio));

    }
}
