package com.Mohammad.ac.test3g;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
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

import com.Mohammad.ac.test3g.Settings.MainPreferenceActivity;
import com.cardiomood.android.controls.gauge.SpeedometerGauge;
import com.crashlytics.android.Crashlytics;

import java.util.List;

import io.fabric.sdk.android.Fabric;

enum speedUnit {bps, Kbps, Mbps, Gbps};

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

    String serverUri = "http://www.ajerlitaxi.com/3gtests";
    String upLoadServerUri = serverUri + "/en/upload.php";
    String downloadServerUri = serverUri + "/files_db/8MB.bin";
    static final String MOB_INFO = "mobInfo";
    MainActivity thisActivity;
    databaseHandler dbHandler;
    static int speedMeterMaxIdx;
    double speedMeterMax[]=         {3.0, 21.0, 50.0, 100.0, 1000.0};
    double speedMeterMajor[]=       {1.0, 5.0, 10.0, 25.0, 250.0};
    double speedMeterRange4Red[]=   {0.2, 2.0, 4, 10.0, 100.0};
    double speedMeterRange4Yellow[]={1.0, 5.0, 10.0, 25.0, 250.0};

    //int speedMeterMinor[]={1, 1, 2, 5, 50};

    // button to show progress dialog
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
        //speedometer.setMaxSpeed(speedMeterMax[speedIdx]);//21.0D);
        //speedometer.setMajorTickStep(speedMeterMajor[speedIdx]);//5.0D);
        speedometer.setMinorTicks(4);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(MOB_INFO, mobInfo);
        super.onSaveInstanceState(savedInstanceState);
    }

    private String getDownloadUrl(){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String tmpStr = SP.getString("downloadhost",downloadServerUri);
        return tmpStr;
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
        int tmpInt = Integer.parseInt(speedLenStr);
        return tmpInt;
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
        Fabric.with(this, new Crashlytics());
        thisActivity = this;
        dbHandler = new databaseHandler(this);
        locationTracker = new gpsTracker(this);
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        speedMeterMaxIdx = getSpeedMeterMaxIdx();
        testDuration = getSpeedTestLen();

        setContentView(R.layout.activity_main);
        //myUtility.OrientationUtils.lockOrientationPortrait(this);
        //force orientation for phones only
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        btnStartTest = (Button) findViewById(R.id.btnStartTest);//
        btnHistory = (Button) findViewById(R.id.id_BtnHistory);//
        //btnUpdate = (Button) findViewById(R.id.btnUpdate);
        //btnUpload = (Button) findViewById(R.id.btnUpload);
        txt_model = (TextView) findViewById(R.id.textViewModel);
        txt_netclass = (TextView) findViewById(R.id.id_netclass);
        txt_netname = (TextView) findViewById(R.id.id_netname);
        txt_cellid = (TextView) findViewById(R.id.id_cellid);
        txt_rnc = (TextView) findViewById(R.id.id_rnc);
        txt_lac = (TextView) findViewById(R.id.id_lac);
        txt_rssi = (TextView) findViewById(R.id.id_rssi);
        txt_minmaxrx = (TextView) findViewById(R.id.id_minmaxrate);
        txtRxRateText = (TextView) findViewById(R.id.rateText_id);//
        txt_latitude = (TextView) findViewById(R.id.id_lat);
        txtTxRateText = (TextView) findViewById(R.id.txRateText_id);
        txt_minmaxtx = (TextView) findViewById(R.id.id_minmaxTxrate);

        txt_wifiState = (TextView) findViewById(R.id.textViewWifiState);
        txt_wifiSsid = (TextView) findViewById(R.id.textViewWifiSSID);
        txt_netSrc = (TextView) findViewById(R.id.textViewNetSrce);
        //mGaugeView2 = (GaugeView) findViewById(R.id.gauge_view2);

        this.txt_neighboring = ((TextView)findViewById(R.id.id_neighbors));
        this.txt_cdmaDbm = ((TextView)findViewById(R.id.id_cdmaDbm));
        this.txt_cdmaEcio = ((TextView)findViewById(R.id.id_cdmaEcio));
        this.listView = ((ListView)findViewById(R.id.drawerList));
        this.listView.setOnItemClickListener(this);
        initalGaugeView(speedMeterMaxIdx);

        SetUpToolbar();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, GetToolbar(),
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle(mTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle(mDrawerTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        btnHistory.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  if(dbHandler.get3gTestsCount() !=0) {
                      //local history instead of site history
                      Intent myIntent = new Intent(MainActivity.this, InfoListActivity.class);
                      MainActivity.this.startActivity(myIntent);
                  } else {
                      new AlertDialog.Builder(MainActivity.this)
                              .setTitle("History")
                              .setMessage("There are no local History records. open web history?")
                              .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                  public void onClick(DialogInterface dialog, int which) {
                                      // continue with delete
                                      if (mobInfo.deviceId == null) {
                                          collectInitInfo();
                                          mobInfo.showInfo(thisActivity);
                                      }
                                      String url = serverUri + "/en/index.php?dev=" + mobInfo.deviceId + "&ver=Feb12";
                                      Intent i = new Intent(Intent.ACTION_VIEW);
                                      i.setData(Uri.parse(url));
                                      startActivity(i);
                                  }
                              })
                              .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                  public void onClick(DialogInterface dialog, int which) {
                                      // do nothing
                                  }
                              })
                              .setIcon(android.R.drawable.ic_dialog_alert)
                              .show();
                  }
                  /**/
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
                // starting new Async Task
                if (MainActivity.this.isNetworkAvailable()) {
                    btnStartTest.setVisibility(View.GONE);
                    btnHistory.setVisibility(View.GONE);

                    new Download2(MainActivity.this).execute(getDownloadUrl());
                    new Upload2(MainActivity.this).execute(upLoadServerUri);
                } else {
                    Toast.makeText(MainActivity.this.thisActivity, "No Network Available", Toast.LENGTH_LONG).show();
                }
            }
        });

        if (savedInstanceState != null) {
            mobInfo = savedInstanceState.getParcelable(MOB_INFO);
            mobInfo.showInfo(thisActivity);
        } else if(mobInfo == null) {
            mobInfo = new c_Info(serverUri);
        }

        mAppContext = getApplicationContext();
        mTelephonyMgr = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);

        /* Update the listener, and start it */
        MyListener   = new MyPhoneStateListener();
        mTelephonyMgr.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_LOCATION);
        collectInitInfo();
        mobInfo.showInfo(thisActivity);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public Toolbar GetToolbar(){
        return (Toolbar)findViewById(R.id.toolbar);
    }
    public void SetUpToolbar(){
        try{
            Toolbar toolbar = GetToolbar();
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            //Deprecated in Lollipop but required if targeting 4.x
            //SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.gps_main_views, R.layout.spinner_dropdown_item);
            //getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            //getSupportActionBar().setListNavigationCallbacks(spinnerAdapter, this);
            //getSupportActionBar().setSelectedNavigationItem(GetUserSelectedNavigationItem());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }
        catch(Exception ex){
            //http://stackoverflow.com/questions/26657348/appcompat-v7-v21-0-0-causing-crash-on-samsung-devices-with-android-v4-2-2
            Log.e("netspeed", ex.toString());
        }

    }

    String isMobileEnabled(){
        ConnectivityManager cm = (ConnectivityManager) mAppContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(info == null) {
            return "";
        } else {
            //mob_avail = info.isAvailable();
            NetworkInfo.State state;
            //if(mob_avail)
            {
                state = info.getState();
            }
            return state.toString();
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
        List<NeighboringCellInfo> neighborCells = mTelephonyMgr.getNeighboringCellInfo();
        if(neighborCells!=null && neighborCells.size() > 0) {
            mobInfo.neighboringCells = "Neighboring List- Lac : Cid : PSC : RSSI\n";
            for(NeighboringCellInfo aCell : neighborCells) {
                int k = aCell.getRssi();
                String str2;
                if (k == 99) {
                    str2 = "Unknown RSSI";
                } else if (getNetworkClass(aCell.getNetworkType()) == "2G") {
                    str2 = String.valueOf(-113 + 2 * k) + " dBm";
                } else {
                    str2 = String.valueOf(k) + " dBm";
                }
                mobInfo.neighboringCells += String.valueOf(aCell.getLac()) + " : " + String.valueOf(aCell.getCid()) + " : " + String.valueOf((aCell.getPsc()) + " : " + str2 + "\n");
            }
        }
        txt_neighboring.setText(mobInfo.neighboringCells);
        //for Dual SIM
        /*myDualSim myDualSimInfo = myDualSim.getInstance(this);
        if(myDualSimInfo.isDualSIM()) {
            mobInfo.deviceId=myDualSimInfo.getImeiSIM1();
            mobInfo.imsi=myDualSimInfo.imsi1;
            mobInfo.phoneNumber = myDualSimInfo.phoneNumber1;
            mobInfo.netType = myDualSimInfo.netType1;
            mobInfo.netClass = getNetworkClass(mobInfo.netType);
            mobInfo.netOperator = myDualSimInfo.netOperator1;
            mobInfo.netName = myDualSimInfo.netName1;

            mobInfo.deviceId2=myDualSimInfo.getImeiSIM2();
            mobInfo.imsi2=myDualSimInfo.imsi2;
            mobInfo.phoneNumber2 = myDualSimInfo.phoneNumber2;
            mobInfo.netType2 = myDualSimInfo.netType2;
            mobInfo.netClass2 = getNetworkClass(mobInfo.netType2);
            mobInfo.netOperator2 = myDualSimInfo.netOperator2;
            mobInfo.netName2 = myDualSimInfo.netName2;
        } else */
        {
            mobInfo.deviceId=mTelephonyMgr.getDeviceId();
            mobInfo.imsi = mTelephonyMgr.getSubscriberId();
            mobInfo.phoneNumber = mTelephonyMgr.getLine1Number();
            mobInfo.netType = mTelephonyMgr.getNetworkType();
            mobInfo.netClass = getNetworkClass(mobInfo.netType);
            mobInfo.netOperator = mTelephonyMgr.getSimOperator();
            mobInfo.netName = mTelephonyMgr.getNetworkOperatorName();
        }
        //TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);


        //mobInfo.imei = android.os.SystemProperties.get(android.telephony.TelephonyProperties.PROPERTY_IMSI);
        //TelephonyManager tMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        mobInfo.mobileState = isMobileEnabled();

        mobInfo.phoneType = mTelephonyMgr.getPhoneType();

        mobInfo.brand = Build.BRAND;
        mobInfo.manuf = Build.MANUFACTURER;
        mobInfo.product = Build.PRODUCT;
        mobInfo.model = Build.MODEL;

        GsmCellLocation cellLocation = (GsmCellLocation) mTelephonyMgr.getCellLocation();
        if(cellLocation != null) {
            mobInfo.cid = cellLocation.getCid();
            mobInfo.cid_3g = mobInfo.cid & 0xffff;
            mobInfo.rnc = (mobInfo.cid & 0xffff0000) >> 16;
            mobInfo.lac = cellLocation.getLac();
        }
        //mTelephonyMgr.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        //Geo Locatoion:
        Location loc = getLocation();//getLastKnownLocation();//
        if(loc != null) {
            mobInfo.lon = loc.getLongitude();
            mobInfo.lat = loc.getLatitude();
        }
    }

    private BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager cm =
                        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if(activeNetwork != null) {
                    boolean isConnected = activeNetwork.isConnectedOrConnecting();
                    boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
                    //btnStartTest.setEnabled(true);
                    if(isWiFi) {
                        mobInfo.netSource = "Wifi";
                    } else {
                        mobInfo.netSource = "Mobile Data";
                    }
                    Log.d("Con","isCon:" + isConnected + "- isWifi:" + isWiFi);
                } else {
                    //btnStartTest.setEnabled(false);
                    mobInfo.netSource = "NA";
                }
                txt_netSrc.setText(mobInfo.netSource);
            }
            if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo =
                        intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(networkInfo!=null && networkInfo.isConnected()) {
                    //do stuff
                    WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
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
                //Other actions implementation
            }
        }
    };
    /* Called when the application is minimized */
    @Override
    protected void onPause()
    {
        super.onPause();
        //mTelephonyMgr.listen(MyListener, PhoneStateListener.LISTEN_NONE);
        locationTracker.unregProviders();
        unregisterReceiver(wifiBroadcastReceiver);
    }

    /* Called when the application resumes */
    @Override
    protected void onResume()
    {
        super.onResume();
        progressReceiver receiver = new progressReceiver();
        IntentFilter filter= new IntentFilter("com.Mohammad.ac.test3g.PROGRESS");
        LocalBroadcastManager.getInstance(this).registerReceiver (receiver, filter);

        filter= new IntentFilter("com.Mohammad.ac.test3g.U_PROGRESS");
        LocalBroadcastManager.getInstance(this).registerReceiver (receiver, filter);

        filter= new IntentFilter("com.Mohammad.ac.test3g.DONE");
        LocalBroadcastManager.getInstance(this).registerReceiver (receiver, filter);
        //Log.d("dev", mobInfo.deviceId);
        System.out.print("dev"+mobInfo.deviceId);
        //mTelephonyMgr.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        locationTracker.regProviders();

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
    /* —————————– */
    /* Start the PhoneState listener */
    /* —————————– */
    private class MyPhoneStateListener extends PhoneStateListener
    {
        @Override
        public void onCellLocationChanged(CellLocation location)
        {
            GsmCellLocation cellLocation = (GsmCellLocation) location;
            if(cellLocation != null) {
                mobInfo.cid = cellLocation.getCid();
                mobInfo.cid_3g = mobInfo.cid & 0xffff;
                mobInfo.rnc = (mobInfo.cid & 0xffff0000) >> 16;
                mobInfo.lac = cellLocation.getLac();

                mobInfo.netType = mTelephonyMgr.getNetworkType();
                mobInfo.netClass = getNetworkClass(mobInfo.netType);
                //show info
                MainActivity.this.txt_netclass.setText(mobInfo.netClass+" - "+mobInfo.netClass2);
                if(mobInfo.netClass.equals("2G")) {
                    MainActivity.this.txt_cellid.setText(""+mobInfo.cid);
                    MainActivity.this.txt_rnc.setText("");
                }else {
                    MainActivity.this.txt_cellid.setText(String.format("%04d",mobInfo.cid_3g));
                    MainActivity.this.txt_rnc.setText(""+mobInfo.rnc);
                }
            }

            /*if(mobInfo.rssi != oldRssi) {
                if(mobInfo.rssi != 99 && mobInfo.rssi !=0) {
                    txt_rssi.setText("" + mobInfo.rssi);
                }else {
                    txt_rssi.setText("---");
                }
            }*/
            super.onCellLocationChanged(location);
        }
        /* Get the Signal strength from the provider, each tiome there is an update */
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            super.onSignalStrengthsChanged(signalStrength);

            /*{

                Class tClass = signalStrength.getClass();
                Method[] methods = tClass.getMethods();
                for (int i = 0; i < methods.length; i++) {
                    System.out.println("public method: " + methods[i]);
                }
            }*/
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
            //mTelephonyMgr.listen(MyListener, PhoneStateListener.LISTEN_NONE);
        }

    };/* End of private Class */


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static String getRateWithUnit(Double value) {
        String str_unit="";
        speedUnit theunit = speedUnit.bps;

        //double rate = 8.0*1e9*total/delta;
        if(value < 1024) {
            theunit = speedUnit.bps;
        } else if (value >= 1024 && value <1024*1024) {
            value /=1024;
            str_unit="K";
            theunit = speedUnit.Kbps;
        }else if (value >= 1024*1024 && value <1024*1024*1024) {
            value /=1024*1024;
            theunit = speedUnit.Mbps;
            str_unit="M";
        }else if (value >= 1024*1024*1024) {
            value /= 1024*1024*1024;
            theunit = speedUnit.Gbps;
            str_unit="G";
        }
        return(String.format("%.2f%s", value, str_unit));
    }

    public Location getLocation() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocationGPS != null) {
                return lastKnownLocationGPS;
            } else {
                Location loc =  locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                //System.out.println("1::"+loc);
                //System.out.println("2::"+loc.getLatitude());
                return loc;
            }
        } else {
            return null;
        }
    }

    private Location getLastKnownLocation() {
        LocationManager mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
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
            //mobInfo.minRxRate = intent.getDoubleExtra("MIN_RATE",0);
            //mobInfo.maxRxRate = intent.getDoubleExtra("MAX_RATE",0);
            switch(intent.getAction()) {
                case "com.Mohammad.ac.test3g.PROGRESS":
                    mobInfo.rxRate = intent.getDoubleExtra("RxRATE",0);
                    mobInfo.minRxRate = intent.getDoubleExtra("MinRxRATE",0);
                    mobInfo.maxRxRate = intent.getDoubleExtra("MaxRxRATE",0);
                    boolean showInf = intent.getBooleanExtra("SHOW_INFO",false);
                    if(showInf) {
                        mobInfo.showInfo(thisActivity);
                    }
                    break;
                case "com.Mohammad.ac.test3g.U_PROGRESS":
                    mobInfo.txRate = intent.getDoubleExtra("TxRATE",0);
                    mobInfo.minTxRate = intent.getDoubleExtra("MinTxRATE",0);
                    mobInfo.maxTxRate = intent.getDoubleExtra("MaxTxRATE",0);
                    showInf = intent.getBooleanExtra("SHOW_INFO",false);
                    if(showInf) {
                        mobInfo.showInfo(thisActivity);
                    }
                    boolean uploadDone = intent.getBooleanExtra("UL_DONE",false);
                    if(uploadDone) {
                        dbHandler.add3gTest(mobInfo);
                        mobInfo.upload(thisActivity);
                    }
                    break;
                case "com.Mohammad.ac.test3g.DONE":
                    uploadDone = intent.getBooleanExtra("DONE",false);
                    if(uploadDone) {
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
