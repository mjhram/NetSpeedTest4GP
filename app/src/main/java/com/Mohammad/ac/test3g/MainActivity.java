package com.Mohammad.ac.test3g;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;
import com.crashlytics.android.Crashlytics;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import io.fabric.sdk.android.Fabric;

enum speedUnit {bps, Kbps, Mbps, Gbps};

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, LocationListener {
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
    int serverResponseCode = 0;
    String serverUri = "http://www.ttaxi1.com/3gtests";
    String upLoadServerUri = serverUri + "/en/upload.php";
    static final String MOB_INFO = "mobInfo";
    MainActivity thisActivity;
    databaseHandler dbHandler;

    // button to show progress dialog
    Button btnStartTest;
    Button btnHistory;

    public TextView txt_minmaxtx;
    //public GaugeView mGaugeView2;

    // File url to download
    private static String file_url = "http://download.thinkbroadband.com/20MB.zip";

    private ListView listView;
    private TextView txtRxRateText;
    private TextView txtTxRateText;
    public TextView txt_cdmaDbm;
    public TextView txt_cdmaEcio;
    public TextView txt_neighboring;
    private SpeedometerGauge speedometer;

    private gpsTracker locationTracker;
    private void initalGaugeView()
    {
        speedometer = ((SpeedometerGauge)findViewById(R.id.speedometer));
        speedometer.setLabelTextSize(22);
        speedometer.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });
        speedometer.setMaxSpeed(21.0D);
        speedometer.setMajorTickStep(5.0D);
        speedometer.setMinorTicks(1);
        speedometer.addColoredRange(0.0D, 1.0D, Color.RED);
        speedometer.addColoredRange(1.0D, 5.0D, Color.YELLOW);
        speedometer.addColoredRange(5.0D, 21.0D, Color.GREEN);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(MOB_INFO, mobInfo);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        thisActivity = this;
        dbHandler = new databaseHandler(this);
        locationTracker = new gpsTracker(this);


        setContentView(R.layout.activity_main);
        myUtility.OrientationUtils.lockOrientationPortrait(this);

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
        //mGaugeView2 = (GaugeView) findViewById(R.id.gauge_view2);

        this.txt_neighboring = ((TextView)findViewById(R.id.id_neighbors));
        this.txt_cdmaDbm = ((TextView)findViewById(R.id.id_cdmaDbm));
        this.txt_cdmaEcio = ((TextView)findViewById(R.id.id_cdmaEcio));
        this.listView = ((ListView)findViewById(R.id.drawerList));
        this.listView.setOnItemClickListener(this);
        initalGaugeView();

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
                collectInitInfo();
                mobInfo.showInfo(thisActivity);
                // starting new Async Task
                if (MainActivity.this.isNetworkAvailable()) {
                    btnStartTest.setVisibility(View.GONE);
                    btnHistory.setVisibility(View.GONE);
                    MainActivity.DownloadFileFromURL localDownloadFileFromURL = new MainActivity.DownloadFileFromURL(MainActivity.this);
                    localDownloadFileFromURL.execute(file_url);
                    uploadFileToURL localuploadFileToURL = new MainActivity.uploadFileToURL(MainActivity.this);
                    localuploadFileToURL.execute(upLoadServerUri);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
    /* Called when the application is minimized */
    @Override
    protected void onPause()
    {
        super.onPause();
        //mTelephonyMgr.listen(MyListener, PhoneStateListener.LISTEN_NONE);
        locationTracker.unregProviders();
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

    class DownloadFileFromURL extends AsyncTask<String, Double, String> {
        //String str_unit;
        double rate/*, minRxRate, maxRxRate*/;
        //MainActivity theActivity;
        Context cntx;

        public DownloadFileFromURL(Context c) {
            cntx = c;
        }
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                //OutputStream output = new FileOutputStream("/sdcard/downloadedfile.jpg");

                byte data[] = new byte[1024];
                speedUnit unit = speedUnit.bps;
                long BeforeTime = System.currentTimeMillis();
                long initialTime = System.currentTimeMillis();
                long TotalTxBeforeTest = TrafficStats.getTotalTxBytes();
                long TotalRxBeforeTest = TrafficStats.getTotalRxBytes();

                long initialTotalRx = TrafficStats.getTotalRxBytes();
                mobInfo.minRxRate = Double.MAX_VALUE;
                mobInfo.maxRxRate = 0;
                mobInfo.avRxRate=0;
                do {
                    if (((count = input.read(data)) == -1)) {
                        break;
                    }
                    long AfterTime = System.currentTimeMillis();
                    if (AfterTime - BeforeTime > 500) {
                        rate = 0.0;
                        long TotalTxAfterTest = TrafficStats.getTotalTxBytes();
                        long TotalRxAfterTest = TrafficStats.getTotalRxBytes();
                        double TimeDifference = AfterTime - BeforeTime;
                        double rxDiff = TotalRxAfterTest - TotalRxBeforeTest;
                        double txDiff = TotalTxAfterTest - TotalTxBeforeTest;
                        //if((rxDiff != 0) && (txDiff != 0))
                        if (rxDiff != 0) {
                            double rxBPS = (rxDiff / (TimeDifference / 1000.0)); // total rx bytes per second.
                            double txBPS = (txDiff / (TimeDifference / 1000.0)); // total tx bytes per second.
                            rate = rxBPS * 8;
                        } else {
                            rate = 0.0;
                        }

                        double overallTimeDifference = AfterTime - initialTime;
                        double overallRxDiff = TotalRxAfterTest - initialTotalRx;
                        if (overallRxDiff != 0) {
                            double rxBPS = (overallRxDiff / (overallTimeDifference / 1000.0)); // total rx bytes per second.
                            mobInfo.avRxRate = rxBPS * 8;
                        } else {
                            mobInfo.avRxRate = 0.0;
                        }
                        if (AfterTime - initialTime > 25000) {
                            break;
                        }
                        if (rate < mobInfo.minRxRate) {
                            mobInfo.minRxRate = rate;
                        }
                        if (rate > mobInfo.maxRxRate) {
                            mobInfo.maxRxRate = rate;
                        }
                        publishProgress(rate, mobInfo.avRxRate/*, mobInfo.minRxRate, mobInfo.maxRxRate*/);
                        BeforeTime = System.currentTimeMillis();
                        TotalTxBeforeTest = TrafficStats.getTotalTxBytes();
                        TotalRxBeforeTest = TrafficStats.getTotalRxBytes();
                    }
                } while (true);
                if (mobInfo.minRxRate == Double.MAX_VALUE) {
                    mobInfo.minRxRate = 0;
                }
                // flushing output
                //output.flush();

                // closing streams
                //output.close();
                input.close();
                //mobInfo.upload();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }
        /**
         * Updating progress bar
         * */
        @Override
        protected void onProgressUpdate(Double... progress) {
            String str = MainActivity.getRateWithUnit(progress[0]);
            MainActivity.this.txtRxRateText.setText(str);
            MainActivity.this.txt_minmaxrx.setText("-"+", "+"-"+", "+MainActivity.getRateWithUnit(mobInfo.avRxRate));
            MainActivity.this.speedometer.setSpeed(progress[0].doubleValue() / 1024.0D / 1024.0D,1000,0);
        }

        @Override
        protected void onPostExecute(String file_url) {
            MainActivity.this.speedometer.setSpeed(0.0D, true);
            MainActivity.this.mobInfo.showInfo(MainActivity.this.thisActivity);
        }

    }

    class uploadFileToURL extends AsyncTask<String, Double, String> {
        //String str_unit;
        speedUnit unit;
        long BeforeTime, initialTime, TotalTxBeforeTest, initialTotalTx;
        //double rate, minTxRate, maxTxRate;
        Context cntx;

        public uploadFileToURL(Context c) {
            cntx = c;
        }

        @Override
        protected void onPreExecute() {
            //isDownloadUpload = true;
        }

        boolean uploadRate2(boolean init)//return true to stop
        {
            if(init == true) {
                unit = speedUnit.bps;
                BeforeTime = System.currentTimeMillis();
                initialTime = System.currentTimeMillis();
                TotalTxBeforeTest = TrafficStats.getTotalTxBytes();
                initialTotalTx = TrafficStats.getTotalTxBytes();
                mobInfo.minTxRate = Double.MAX_VALUE;
                mobInfo.maxTxRate = 0;
                mobInfo.avTxRate = 0;
                return false;
            }

            long AfterTime = System.currentTimeMillis();
            if(AfterTime - BeforeTime > 500) {
                double rate=0.0;
                long TotalTxAfterTest = TrafficStats.getTotalTxBytes();
                double TimeDifference = AfterTime - BeforeTime;
                double txDiff = TotalTxAfterTest - TotalTxBeforeTest;
                if(txDiff != 0) {
                    double txBPS = (txDiff / (TimeDifference/1000.0)); // total tx bytes per second.
                    rate = txBPS*8;
                }
                else {
                    rate=0.0;
                }
                if(rate < mobInfo.minTxRate ) {
                    mobInfo.minTxRate = rate;
                }
                if(rate > mobInfo.maxTxRate ){
                    mobInfo.maxTxRate = rate;
                }

                double overallTimeDifference = AfterTime -initialTime;
                double overallTxDiff = TotalTxAfterTest - initialTotalTx;
                if(overallTxDiff != 0) {
                    double txBPS = (overallTxDiff / (overallTimeDifference/1000.0)); // total tx bytes per second.
                    mobInfo.avTxRate = txBPS*8;
                }
                else {
                    mobInfo.avTxRate=0.0;
                }

                if(overallTimeDifference > 25000) {
                    return true;
                }
                publishProgress(rate, mobInfo.avTxRate/*, minTxRate, maxTxRate*/);
                BeforeTime = System.currentTimeMillis();
                TotalTxBeforeTest = TrafficStats.getTotalTxBytes();
            }
            return false;
        }

        @Override
        protected String doInBackground(String... f_url) {
            //int count;
            String fileName = "tmp.bin";
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            byte[] buffer;
            int maxBufferSize = 20 * 1024;
            int contentLen = 100*maxBufferSize;
            try {
                String strHeader = "Content-Disposition: form-data; name='ufile';filename='"
                        + fileName + "'" + lineEnd;
                URL url = new URL(f_url[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("ufile", fileName);
                int len = contentLen+strHeader.length()+11;//+Header
                len += 13;//Footer
                conn.setFixedLengthStreamingMode(len);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes(strHeader);
                dos.writeBytes(lineEnd);

                buffer = new byte[maxBufferSize];
                int totalBytes2 =0;
                uploadRate2(true);
                do {
                    dos.write(buffer, 0, maxBufferSize);
                    totalBytes2 += maxBufferSize;
                    if(uploadRate2(false)) {
                        //break;
                    }
                    dos.flush();
                } while (totalBytes2 < contentLen);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                int serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();
                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);
                dos.flush();
                dos.close();
                if(mobInfo.minTxRate == Double.MAX_VALUE) {
                    mobInfo.minTxRate = 0;
                }
                dbHandler.add3gTest(mobInfo);
                mobInfo.upload(MainActivity.this);
            } catch (MalformedURLException ex) {
                //dialog.dismiss();
                Toast.makeText(MainActivity.this, "MalformedURLException", Toast.LENGTH_LONG).show();
                ex.printStackTrace();
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Got Exception : see logcat ", Toast.LENGTH_LONG).show();
                //dialog.dismiss();
                e.printStackTrace();
                Log.e("Upload Exception", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Double... progress) {
            String str = MainActivity.getRateWithUnit(progress[0]);
            MainActivity.this.txtTxRateText.setText(str);
            MainActivity.this.txt_minmaxtx.setText("-"+", "+"-"+", "+MainActivity.getRateWithUnit(mobInfo.avTxRate));
            MainActivity.this.speedometer.setSpeed(progress[0].doubleValue() / 1024.0D / 1024.0D,1000,0);
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            speedometer.setSpeed(0.0D, true);
            mobInfo.showInfo(MainActivity.this.thisActivity);
            btnStartTest.setVisibility(View.VISIBLE);
            btnHistory.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long is)
    {
        String[] menuArray = getResources().getStringArray(R.array.main_selection);
        if (menuArray[position].equalsIgnoreCase("About"))
        {
            Intent localIntent = new Intent(this, AboutActivity.class);
            startActivity(localIntent);
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

    public void onTestClick(View v) {
        Intent myIntent = new Intent(MainActivity.this, InfoListActivity.class);
        MainActivity.this.startActivity(myIntent);
    }
}
