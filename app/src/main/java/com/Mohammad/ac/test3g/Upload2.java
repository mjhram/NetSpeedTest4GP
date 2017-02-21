package com.Mohammad.ac.test3g;

import android.net.TrafficStats;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by mohammad.haider on 021 2/21/2017.
 */

public class Upload2 extends AsyncTask<String, Double, String> {
    //String str_unit;
    //speedUnit unit;
    long BeforeTime, initialTime, TotalTxBeforeTest, initialTotalTx;
    //double rate, minTxRate, maxTxRate;
    MainActivity theActivity;

    public Upload2(MainActivity activity) {
        theActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        //isDownloadUpload = true;
    }

    boolean uploadRate2(boolean init)//return true to stop
    {
        if(init == true) {
            //unit = speedUnit.bps;
            BeforeTime = System.currentTimeMillis();
            initialTime = System.currentTimeMillis();
            TotalTxBeforeTest = TrafficStats.getTotalTxBytes();
            initialTotalTx = TrafficStats.getTotalTxBytes();
            theActivity.mobInfo.minTxRate = Double.MAX_VALUE;
            theActivity.mobInfo.maxTxRate = 0;
            theActivity.mobInfo.avTxRate = 0;
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
            if(rate < theActivity.mobInfo.minTxRate ) {
                theActivity.mobInfo.minTxRate = rate;
            }
            if(rate > theActivity.mobInfo.maxTxRate ){
                theActivity.mobInfo.maxTxRate = rate;
            }

            double overallTimeDifference = AfterTime -initialTime;
            double overallTxDiff = TotalTxAfterTest - initialTotalTx;
            if(overallTxDiff != 0) {
                double txBPS = (overallTxDiff / (overallTimeDifference/1000.0)); // total tx bytes per second.
                theActivity.mobInfo.avTxRate = txBPS*8;
            }
            else {
                theActivity.mobInfo.avTxRate=0.0;
            }

            if(overallTimeDifference > MainActivity.testDuration) {
                return true;
            }
            publishProgress(rate, theActivity.mobInfo.avTxRate/*, minTxRate, maxTxRate*/);
            BeforeTime = System.currentTimeMillis();
            TotalTxBeforeTest = TrafficStats.getTotalTxBytes();
        }
        return false;
    }

    @Override
    protected String doInBackground(String... f_url) {
        //int count;
        int maxBufferSize = 20 * 1024;
        try {
            Socket mSocket = new Socket();
            mSocket.setSoTimeout(10000);
            mSocket.setReuseAddress(true);
            mSocket.setKeepAlive(true);
            mSocket.connect(new InetSocketAddress("ajerlitaxi.com", 80));
            if (mSocket == null || mSocket.isClosed()) {
                return null;
            }
            final String head = "POST " + "/" + " HTTP/1.1\r\n" + "Host: " + "http://ajerlitaxi.com" + "\r\nAccept: " +
                    "*/*\r\nContent-Length: " + 10000000 + "\r\n\r\n";
            OutputStream outStream = mSocket.getOutputStream();
            if (outStream == null) {
                return null;
            }
            outStream.write(head.getBytes());
            outStream.flush();

            byte dummy[] =new byte[maxBufferSize];
            uploadRate2(true);
            while(true){
                outStream.write(dummy);
                outStream.flush();
                if(uploadRate2(false)) {
                    break;
                }
            }
            mSocket.close();
            if(theActivity.mobInfo.minTxRate == Double.MAX_VALUE) {
                theActivity.mobInfo.minTxRate = 0;
            }
            theActivity.dbHandler.add3gTest(theActivity.mobInfo);
            theActivity.mobInfo.upload(theActivity);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Upload Exception", e.getMessage(), e);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Double... progress) {
        String str = MainActivity.getRateWithUnit(progress[0]);
        theActivity.txtTxRateText.setText(str);
        theActivity.txt_minmaxtx.setText("-"+", "+"-"+", "+MainActivity.getRateWithUnit(theActivity.mobInfo.avTxRate));
        theActivity.speedometer.setSpeed(progress[0].doubleValue() / 1024.0D / 1024.0D,1000,0);
    }

    /**
     * After completing background task
     * Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(String file_url) {
        theActivity.speedometer.setSpeed(0.0D, true);
        theActivity.mobInfo.showInfo(theActivity);
        theActivity.btnStartTest.setVisibility(View.VISIBLE);
        theActivity.btnHistory.setVisibility(View.VISIBLE);
    }
}

