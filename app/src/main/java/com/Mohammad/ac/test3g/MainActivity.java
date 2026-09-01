package com.Mohammad.ac.test3g;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.Mohammad.ac.test3g.Settings.MainPreferenceActivity;
import com.cardiomood.android.controls.gauge.SpeedometerGauge;

import java.util.List;
import java.util.Locale;

enum speedUnit {bps, Kbps, Mbps, Gbps}

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, LocationListener {
    static int testDuration = 15000;//test duration in msec
    final static int socketTimeOut = 5000;

    private Context mAppContext;
    static TelephonyManager        mTelephonyMgr;
    MyPhoneStateListener    MyListener;
    public c_Info mobInfo;
    public TextView txt_netclass;
    public TextView txt_netname;
    public TextView txt_model;
    public TextView txt_cellid;
    public TextView txt_rnc;
    public TextView txt_lac;
    public TextView txt_rssi;
    public TextView txt_minmaxrx;
    public TextView txt_latitude;
    public TextView txt_wifiState;
    public TextView txt_wifiSsid;
    public TextView txt_netSrc;

    String serverUri, upLoadServerUri, downloadServerUri;
    static final String MOB_INFO = "mobInfo";
    MainActivity thisActivity;
    databaseHandler dbHandler;
    static int speedMeterMaxIdx;
    double speedMeterMax[]=         {3.0, 21.0, 50.0, 100.0, 1000.0};
    double speedMeterMajor[]=       {1.0, 5.0, 10.0, 25.0, 250.0};
    double speedMeterRange4Red[]=   {0.2, 2.0, 4, 10.0, 100.0};
    double speedMeterRange4Yellow[]={1.0, 5.0, 10.0, 25.0, 250.0};

    Button btnStartTest;
    Button btnHistory;

    public TextView txt_minmaxtx;

    private ListView listView;
    public TextView txtRxRateText;
    public TextView txtTxRateText;
    public TextView txt_cdmaDbm;
    public TextView txt_cdmaEcio;
    public TextView txt_neighboring;
    public SpeedometerGauge speedometer;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private gpsTracker locationTracker;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private void initalGaugeView(int speedIdx)
    {
        speedometer = ((SpeedometerGauge)findViewById(R.id.speedometer));
        speedometer.setLabelTextSize(22);
        speedometer.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });
        setSpeedMeterMax(speedIdx);
        speedometer.setMinorTicks(4);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(MOB_INFO, mobInfo);
        super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mobInfo = savedInstanceState.getParcelable(MOB_INFO);
        if (mobInfo != null) mobInfo.showInfo(thisActivity);
    }

    private String checkVer(){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String tmpStr = SP.getString("ver","1.0");
        String ver="1.0";
        if(!tmpStr.equals(ver)) {
            SP.edit().remove("downloadhost").apply();
            SP.edit().putString("ver",ver).apply();
            tmpStr = ver;
        }

        return tmpStr;
    }
    private String getDownloadUrl(){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return SP.getString("downloadhost",downloadServerUri);
    }

    private int getSpeedMeterMaxIdx(){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String speedMeterMaxIdxStr = SP.getString("speedmeterMax","2");//default is 1
        int tmpInt = Integer.parseInt(speedMeterMaxIdxStr);
        return tmpInt-1;
    }

    private int getSpeedTestLen(){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String speedLenStr = SP.getString("speedtestlen","15000");//default is 8sec
        return Integer.parseInt(speedLenStr);
    }

    private void setSpeedMeterMax(int speedIdx) {
        speedometer.setMaxSpeed(speedMeterMax[speedIdx]);//21.0D);
        speedometer.setMajorTickStep(speedMeterMajor[speedIdx]);//5.0D);
        speedometer.addColoredRange(0.0D, speedMeterRange4Red[speedIdx], Color.RED);
        speedometer.addColoredRange(speedMeterRange4Red[speedIdx], speedMeterRange4Yellow[speedIdx], Color.YELLOW);
        speedometer.addColoredRange(speedMeterRange4Yellow[speedIdx], speedMeterMax[speedIdx], Color.GREEN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisActivity = this;
        serverUri = getResources().getString(R.string.serverUrl);
        upLoadServerUri = serverUri + "/en/upload.php";
        downloadServerUri = "http://ipv4.download.thinkbroadband.com/10MB.zip";

        dbHandler = new databaseHandler(this);

        checkVer();

        speedMeterMaxIdx = getSpeedMeterMaxIdx();
        testDuration = getSpeedTestLen();

        setContentView(R.layout.activity_main);
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        btnStartTest = (Button) findViewById(R.id.btnStartTest);
        btnHistory = (Button) findViewById(R.id.id_BtnHistory);
        txt_model = (TextView) findViewById(R.id.textViewModel);
        txt_netclass = (TextView) findViewById(R.id.id_netclass);
        txt_netname = (TextView) findViewById(R.id.id_netname);
        txt_cellid = (TextView) findViewById(R.id.id_cellid);
        txt_rnc = (TextView) findViewById(R.id.id_rnc);
        txt_lac = (TextView) findViewById(R.id.id_lac);
        txt_rssi = (TextView) findViewById(R.id.id_rssi);
        txt_minmaxrx = (TextView) findViewById(R.id.id_minmaxrate);
        txtRxRateText = (TextView) findViewById(R.id.rateText_id);
        txt_latitude = (TextView) findViewById(R.id.id_lat);
        txtTxRateText = (TextView) findViewById(R.id.txRateText_id);
        txt_minmaxtx = (TextView) findViewById(R.id.id_minmaxTxrate);

        txt_wifiState = (TextView) findViewById(R.id.textViewWifiState);
        txt_wifiSsid = (TextView) findViewById(R.id.textViewWifiSSID);
        txt_netSrc = (TextView) findViewById(R.id.textViewNetSrce);

        this.txt_neighboring = ((TextView)findViewById(R.id.id_neighbors));
        this.txt_cdmaDbm = ((TextView)findViewById(R.id.id_cdmaDbm));
        this.txt_cdmaEcio = ((TextView)findViewById(R.id.id_cdmaEcio));
        this.listView = ((ListView)findViewById(R.id.drawerList));
        this.listView.setOnItemClickListener(this);
        initalGaugeView(speedMeterMaxIdx);

        SetUpToolbar();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, GetToolbar(),
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        btnHistory.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  if(dbHandler.get3gTestsCount() !=0) {
                      Intent myIntent = new Intent(MainActivity.this, InfoListActivity.class);
                      MainActivity.this.startActivity(myIntent);
                  } else {
                      new AlertDialog.Builder(MainActivity.this)
                              .setTitle("History")
                              .setMessage("There are no local History records")
                              .setNegativeButton(android.R.string.no, null)
                              .setIcon(android.R.drawable.ic_dialog_alert)
                              .show();
                  }
              }
        });

        btnStartTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mobInfo.netSource.equalsIgnoreCase("NA")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("No active Network available")
                            .setTitle("No Network")
                            .setCancelable(true)
                            .setPositiveButton("OK", null);
                    AlertDialog alert = builder.create();
                    alert.show();
                    return;
                }
                collectInitInfo();
                mobInfo.showInfo(thisActivity);
                if (MainActivity.this.isNetworkAvailable()) {
                    btnStartTest.setVisibility(View.GONE);
                    btnHistory.setVisibility(View.GONE);

                    new Download2(MainActivity.this).execute(getDownloadUrl());
                    new Upload2(MainActivity.this, true).execute(upLoadServerUri);
                } else {
                    Toast.makeText(MainActivity.this.thisActivity, "No Network Available", Toast.LENGTH_LONG).show();
                }
            }
        });

        if (savedInstanceState != null) {
            mobInfo = savedInstanceState.getParcelable(MOB_INFO);
            if (mobInfo != null) mobInfo.showInfo(thisActivity);
        } else if(mobInfo == null) {
            mobInfo = new c_Info(serverUri);
        }

        mAppContext = getApplicationContext();
        mTelephonyMgr = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);

        if (checkPermissions()) {
            startAppLogic();
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (checkPermissions()) {
                startAppLogic();
            } else {
                Toast.makeText(this, "Permissions are required for this app to function", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startAppLogic() {
        locationTracker = new gpsTracker(this);
        MyListener = new MyPhoneStateListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            mTelephonyMgr.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_LOCATION);
        }
        collectInitInfo();
        mobInfo.showInfo(thisActivity);
    }

    private void resumeAppLogic() {
        progressReceiver receiver = new progressReceiver();
        IntentFilter filter= new IntentFilter("com.Mohammad.ac.test3g.PROGRESS");
        LocalBroadcastManager.getInstance(this).registerReceiver (receiver, filter);

        filter= new IntentFilter("com.Mohammad.ac.test3g.U_PROGRESS");
        LocalBroadcastManager.getInstance(this).registerReceiver (receiver, filter);

        filter= new IntentFilter("com.Mohammad.ac.test3g.DONE");
        LocalBroadcastManager.getInstance(this).registerReceiver (receiver, filter);
        
        if (locationTracker != null) {
            locationTracker.regProviders();
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(wifiBroadcastReceiver, intentFilter);

        int tmpInt = getSpeedMeterMaxIdx();
        if(speedMeterMaxIdx != tmpInt) {
            speedMeterMaxIdx = tmpInt;
            setSpeedMeterMax(speedMeterMaxIdx);
        }
        testDuration = getSpeedTestLen();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (checkPermissions()) {
            resumeAppLogic();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (locationTracker != null) {
            locationTracker.unregProviders();
        }
        try {
            unregisterReceiver(wifiBroadcastReceiver);
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public Toolbar GetToolbar(){
        return (Toolbar)findViewById(R.id.toolbar);
    }
    public void SetUpToolbar(){
        try{
            Toolbar toolbar = GetToolbar();
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }
        catch(Exception ex){
            Log.e("netspeed", ex.toString());
        }
    }

    String isMobileEnabled(){
        ConnectivityManager cm = (ConnectivityManager) mAppContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(info == null) {
            return "";
        } else {
            return info.getState().toString();
        }
    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager cm = (ConnectivityManager) mAppContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null);
    }

    public String getNetworkClass(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "Unknown";
        }
    }

    private void collectInitInfo() {
        mobInfo.neighboringCells = "";
        txt_neighboring.setText(mobInfo.neighboringCells);
        {
            mobInfo.deviceId = ""; 
            mobInfo.imsi = ""; 
            mobInfo.phoneNumber = ""; 
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                mobInfo.netType = mTelephonyMgr.getNetworkType();
            }
            mobInfo.netClass = getNetworkClass(mobInfo.netType);
            mobInfo.netOperator = mTelephonyMgr.getSimOperator();
            mobInfo.netName = mTelephonyMgr.getNetworkOperatorName();
        }
        mobInfo.mobileState = isMobileEnabled();
        mobInfo.phoneType = mTelephonyMgr.getPhoneType();
        mobInfo.brand = Build.BRAND;
        mobInfo.manuf = Build.MANUFACTURER;
        mobInfo.product = Build.PRODUCT;
        mobInfo.model = Build.MODEL;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            GsmCellLocation cellLocation = (GsmCellLocation) mTelephonyMgr.getCellLocation();
            if(cellLocation != null) {
                mobInfo.cid = cellLocation.getCid();
                mobInfo.cid_3g = mobInfo.cid & 0xffff;
                mobInfo.rnc = (mobInfo.cid & 0xffff0000) >> 16;
                mobInfo.lac = cellLocation.getLac();
            }
        }
        Location loc = getLocation();
        if(loc != null) {
            mobInfo.lon = loc.getLongitude();
            mobInfo.lat = loc.getLatitude();
        }
    }

    private BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() != null && intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if(activeNetwork != null) {
                    boolean isConnected = activeNetwork.isConnectedOrConnecting();
                    boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
                    if(isWiFi) {
                        mobInfo.netSource = "Wifi";
                    } else {
                        mobInfo.netSource = "Mobile Data";
                    }
                } else {
                    mobInfo.netSource = "NA";
                }
                txt_netSrc.setText(mobInfo.netSource);
            }
            if(intent.getAction() != null && intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(networkInfo!=null && networkInfo.isConnected()) {
                    WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    mobInfo.wifiSsid = wifiInfo.getSSID();
                    mobInfo.wifiIsConnected = true;
                    txt_wifiSsid.setText(mobInfo.wifiSsid);
                    txt_wifiState.setText("Connected");
                } else {
                    mobInfo.wifiSsid = "";
                    mobInfo.wifiIsConnected = false;
                    txt_wifiSsid.setText("---");
                    txt_wifiState.setText("Disconnected");
                }
            }
        }
    };

    private String getLTEsignalStrengthString(SignalStrength signalStrength)
    {
        String tmpStr = "";
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            List<CellInfo> cellInfos = mTelephonyMgr.getAllCellInfo();
            if(cellInfos != null && !cellInfos.isEmpty()) {
                CellInfo cInfo = cellInfos.get(0);
                if (cInfo instanceof CellInfoLte) {
                    CellSignalStrengthLte ssLte = ((CellInfoLte) cInfo).getCellSignalStrength();
                    tmpStr += ssLte.getAsuLevel();
                    tmpStr += ssLte.getDbm();
                    tmpStr += ssLte.toString();
                }
            }
        }
        return tmpStr;
    }

    private class MyPhoneStateListener extends PhoneStateListener
    {
        @Override
        public void onCellLocationChanged(CellLocation location)
        {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                mobInfo.netType = mTelephonyMgr.getNetworkType();
            }
            mobInfo.netClass = getNetworkClass(mobInfo.netType);
            MainActivity.this.txt_netclass.setText(mobInfo.netClass + " - " + mobInfo.netClass2);

            if(location instanceof GsmCellLocation) {
                GsmCellLocation cellLocation = (GsmCellLocation) location;
                mobInfo.cid = cellLocation.getCid();
                mobInfo.cid_3g = mobInfo.cid & 0xffff;
                mobInfo.rnc = (mobInfo.cid & 0xffff0000) >> 16;
                mobInfo.lac = cellLocation.getLac();

                if (mobInfo.netClass.equals("2G")) {
                    MainActivity.this.txt_cellid.setText("" + mobInfo.cid);
                    MainActivity.this.txt_rnc.setText("");
                } else {
                    MainActivity.this.txt_cellid.setText(String.format(Locale.getDefault(), "%04d", mobInfo.cid_3g));
                    MainActivity.this.txt_rnc.setText("" + mobInfo.rnc);
                }
                MainActivity.this.txt_lac.setText(""+mobInfo.lac);
            } else if(mobInfo.netClass.equals("4G")) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    List<CellInfo> cellInfos = mTelephonyMgr.getAllCellInfo();
                    if(cellInfos != null && !cellInfos.isEmpty()) {
                        CellInfo cInfo = cellInfos.get(0);
                        if (cInfo instanceof CellInfoLte) {
                            CellIdentityLte cellId = ((CellInfoLte) cInfo).getCellIdentity();
                            mobInfo.lac = cellId.getTac();
                            mobInfo.cid = cellId.getCi();
                            MainActivity.this.txt_cellid.setText("" + mobInfo.cid);
                            MainActivity.this.txt_rnc.setText("");
                            MainActivity.this.txt_lac.setText(""+mobInfo.lac);
                        }
                    }
                }
            }
            super.onCellLocationChanged(location);
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            super.onSignalStrengthsChanged(signalStrength);
            mobInfo.SignalStrengths = signalStrength.toString();
            mobInfo.SignalStrengths += ">>" + signalStrength.getCdmaDbm() + "," + signalStrength.getCdmaEcio() + "," + signalStrength.getEvdoDbm();
            mobInfo.SignalStrengths +=  "," + signalStrength.getEvdoEcio() + "," + signalStrength.getEvdoSnr() + "," + signalStrength.getGsmBitErrorRate();

            int oldRssi = mobInfo.rssi;
            if (signalStrength.isGsm()) {
                if (signalStrength.getGsmSignalStrength() != 99 && signalStrength.getGsmSignalStrength() != 0)
                    mobInfo.rssi = signalStrength.getGsmSignalStrength() * 2 - 113;
                else
                    mobInfo.rssi = signalStrength.getGsmSignalStrength();
            } else {
                mobInfo.rssi = signalStrength.getCdmaDbm();
            }
            if(mobInfo.rssi != oldRssi) {
                if(mobInfo.rssi != 99 && mobInfo.rssi !=0) {
                    txt_rssi.setText("" + mobInfo.rssi);
                }else {
                    txt_rssi.setText("---");
                }
            }
            String ss = getLTEsignalStrengthString(signalStrength);
            mobInfo.SignalStrengths += ">>LTE:" + ss;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static String getRateWithUnit(Double value) {
        String str_unit="";
        double displayValue = value;
        if(value < 1024) {
            // bps
        } else if (value >= 1024 && value <1024*1024) {
            displayValue = value / 1024;
            str_unit="K";
        }else if (value >= 1024*1024 && value <1024*1024*1024) {
            displayValue = value / 1024 / 1024;
            str_unit="M";
        }else if (value >= 1024*1024*1024) {
            displayValue = value / 1024 / 1024 / 1024;
            str_unit="G";
        }
        return(String.format(Locale.getDefault(), "%.2f%s", displayValue, str_unit));
    }

    public Location getLocation() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocationGPS != null) {
                return lastKnownLocationGPS;
            } else {
                return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            }
        } else {
            return null;
        }
    }

    private Location getLastKnownLocation() {
        LocationManager mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public void onMainClick(View v) {
        collectInitInfo();
        mobInfo.showInfo(thisActivity);
    }

    private class progressReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) return;
            switch(intent.getAction()) {
                case "com.Mohammad.ac.test3g.PROGRESS":
                    mobInfo.rxRate = intent.getDoubleExtra("RxRATE",0);
                    mobInfo.minRxRate = intent.getDoubleExtra("MinRxRATE",0);
                    mobInfo.maxRxRate = intent.getDoubleExtra("MaxRxRATE",0);
                    if(intent.getBooleanExtra("SHOW_INFO",false)) {
                        mobInfo.showInfo(thisActivity);
                    }
                    break;
                case "com.Mohammad.ac.test3g.U_PROGRESS":
                    mobInfo.txRate = intent.getDoubleExtra("TxRATE",0);
                    mobInfo.minTxRate = intent.getDoubleExtra("MinTxRATE",0);
                    mobInfo.maxTxRate = intent.getDoubleExtra("MaxTxRATE",0);
                    if(intent.getBooleanExtra("SHOW_INFO",false)) {
                        mobInfo.showInfo(thisActivity);
                    }
                    if(intent.getBooleanExtra("UL_DONE",false)) {
                        dbHandler.add3gTest(mobInfo);
                        mobInfo.upload(thisActivity);
                    }
                    break;
                case "com.Mohammad.ac.test3g.DONE":
                    if(intent.getBooleanExtra("DONE",false)) {
                        btnStartTest.setVisibility(View.VISIBLE);
                        btnHistory.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long is)
    {
        String[] menuArray = getResources().getStringArray(R.array.main_selection);
        if (menuArray[position].equalsIgnoreCase("About")) {
            Intent localIntent = new Intent(this, AboutActivity.class);
            startActivity(localIntent);
        } else if (menuArray[position].equalsIgnoreCase("Options")) {
            LaunchPreferenceScreen(MainPreferenceActivity.PreferenceConstants.GENERAL);
        }
    }

    public void onLocationClick(View paramView)
    {
        if ((mobInfo.lat == 0.0D) && (mobInfo.lon == 0.0D)) {
            return;
        }
        Uri localUri = Uri.parse("geo:0,0?q=" + mobInfo.lat + "," + mobInfo.lon);
        Intent localIntent = new Intent("android.intent.action.VIEW", localUri);
        startActivity(localIntent);
    }


    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    private void LaunchPreferenceScreen(final String whichFragment) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent targetActivity = new Intent(getApplicationContext(), MainPreferenceActivity.class);
                targetActivity.putExtra("preference_fragment", whichFragment);
                startActivity(targetActivity);
            }
        }, 250);
    }
}
