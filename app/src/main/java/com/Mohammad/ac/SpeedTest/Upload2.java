package com.Mohammad.ac.SpeedTest;

import static com.Mohammad.ac.SpeedTest.MainActivity.socketTimeOut;

import android.net.TrafficStats;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URI;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by mohammad.haider on 021 2/21/2017.
 */

public class Upload2 extends AsyncTask<String, Double, String> {
    //String str_unit;
    //speedUnit unit;
    long BeforeTime, initialTime, TotalTxBeforeTest, initialTotalTx;
    //double rate, minTxRate, maxTxRate;
    MainActivity theActivity;
    boolean disabled;

    public Upload2(MainActivity activity, boolean disableUpload) {
        theActivity = activity;
        disabled = disableUpload;
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
        // Cloudflare's own speed test client never posts more than 50,000,000 bytes in
        // a single request - it does many bounded requests back to back instead of one
        // giant streaming connection. Declaring a huge Content-Length (we tried 2GB)
        // gets treated as abuse and the edge resets the connection ("Broken pipe").
        // Match their pattern: one chunk per connection, repeated until testDuration.
        final long chunkSize = 25_000_000L; // 25MB, well within Cloudflare's own usage
        try {
            if (!disabled) {
                URI uri = new URI(f_url[0]);
                String host = uri.getHost();
                String path = uri.getPath();
                if (path == null || path.isEmpty()) {
                    path = "/";
                }

                byte[] dummy = new byte[maxBufferSize];
                uploadRate2(true);
                long testStartTime = System.currentTimeMillis();
                boolean bDone = false;

                for (int k = 0; k < 100 && !bDone; k++) {
                    // Cloudflare's upload-test endpoint (and virtually every modern
                    // public host) requires TLS - plain port 80 will just get
                    // refused/redirected.
                    SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                    SSLSocket mSocket = (SSLSocket) sslSocketFactory.createSocket(host, 443);
                    mSocket.setSoTimeout(socketTimeOut);
                    mSocket.setReuseAddress(true);
                    mSocket.setKeepAlive(true);
                    if (mSocket.isClosed()) {
                        return null;
                    }

                    final String head = "POST " + path + " HTTP/1.1\r\n" + "Host: " + host + "\r\n" +
                            "User-Agent: NetSpeedTest4GP\r\n" +
                            "Content-Type: application/octet-stream\r\n" +
                            "Accept: */*\r\nContent-Length: " + chunkSize + "\r\n\r\n";
                    OutputStream outStream = mSocket.getOutputStream();
                    if (outStream == null) {
                        return null;
                    }
                    outStream.write(head.getBytes());
                    outStream.flush();

                    long chunkWritten = 0;
                    while (chunkWritten < chunkSize) {
                        int toWrite = (int) Math.min(dummy.length, chunkSize - chunkWritten);
                        outStream.write(dummy, 0, toWrite);
                        outStream.flush();
                        chunkWritten += toWrite;
                        if (uploadRate2(false)) {
                            bDone = true;
                            break;
                        }
                    }
                    outStream.close();
                    mSocket.close();

                    if (System.currentTimeMillis() - testStartTime > MainActivity.testDuration) {
                        bDone = true;
                    }
                }
            }
            if(theActivity.mobInfo.minTxRate == Double.MAX_VALUE) {
                theActivity.mobInfo.minTxRate = 0;
            }
            theActivity.dbHandler.add3gTest(theActivity.mobInfo);
            //theActivity.mobInfo.upload(theActivity);
        } catch (SocketTimeoutException e) {
            Log.e("Upload Exception", "Server did not respond in time (" + socketTimeOut + "ms) - possible bad host/path or blocked connection");
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
