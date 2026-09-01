package com.Mohammad.ac.SpeedTest;

import static com.Mohammad.ac.SpeedTest.MainActivity.socketTimeOut;

import android.net.TrafficStats;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;

/**
 * Created by mohammad.haider on 023 2/23/2017.
 */

class Download2 extends AsyncTask<String, Double, String> {
    double rate;
    MainActivity theActivity;

    public Download2(MainActivity activity) {
        theActivity = activity;
    }

    @Override
    protected void onPreExecute() {
    }



    @Override
    protected String doInBackground(String... f_url) {
        long sum = 0;


        theActivity.mobInfo.minRxRate = Double.MAX_VALUE;
        theActivity.mobInfo.maxRxRate = 0;
        theActivity.mobInfo.avRxRate = 0;
        try {
            boolean bDone = false;
            URI uri = new URI(f_url[0]);
            String host = uri.getHost();
            String path = uri.getPath();
            long downloadTest_StrtTime = System.currentTimeMillis();
            for(int k=0; k<10 && bDone == false; k++)
            {
                Socket mSocket = new Socket();
                mSocket.setSoTimeout(socketTimeOut);
                mSocket.setReuseAddress(true);
                mSocket.setKeepAlive(true);
                mSocket.connect(new InetSocketAddress(host, 80));
                if (mSocket == null || mSocket.isClosed()) {
                    return null;
                }

                //the request
                final String downloadRequest = "GET " + path + " HTTP/1.1\r\n"
                        + "Host: " + host + "\r\n"
                        + "User-Agent: NetSpeedTest4GP\r\n"
                        + "Connection: close\r\n\r\n";

                OutputStream outStream = mSocket.getOutputStream();
                if (outStream == null) {
                    return null;
                }
                outStream.write(downloadRequest.getBytes());
                outStream.flush();

                final byte[] dummy = new byte[1024];
                int read;
                InputStream in = mSocket.getInputStream();

                // Read and validate the status line before treating anything as file data.
                StringBuilder statusLine = new StringBuilder();
                int c;
                while ((c = in.read()) != -1) {
                    statusLine.append((char) c);
                    if (statusLine.toString().endsWith("\r\n")) break;
                }
                Log.d("download_status", statusLine.toString().trim());
                if (!statusLine.toString().contains(" 200 ")) {
                    // Not a plain 200 OK (e.g. redirect/4xx) - this attempt can't measure
                    // real throughput, so bail out of this socket instead of blocking on
                    // read() waiting for a body that may never come.
                    in.close();
                    outStream.close();
                    mSocket.close();
                    continue;
                }
                // Skip the rest of the response headers (up to the blank line) so they
                // aren't counted as downloaded file data.
                int matched = 0; // tracks progress matching "\r\n\r\n"
                final byte[] terminator = {'\r', '\n', '\r', '\n'};
                while (matched < terminator.length && (c = in.read()) != -1) {
                    matched = (c == terminator[matched]) ? matched + 1 : (c == terminator[0] ? 1 : 0);
                }
                //boolean bFirst = true;

                long lastPublishProgressTime = System.currentTimeMillis();//can be intialized to 0;
                long initialTime = System.currentTimeMillis();//used for rate calc
                long TotalRxBeforeTest = TrafficStats.getTotalRxBytes();
                long initialTotalRx = TrafficStats.getTotalRxBytes();
                sum = 0;
                while ((read = in.read(dummy)) != -1) {
                    sum += read;
                    /*Log.v("download_read/total", Integer.toString(read)+"/"+Integer.toString(sum));
                    if(bFirst) {
                        String str = new String(dummy, 0, 200);
                        Log.v("download_dummy", str);
                        bFirst = false;
                    }*/
                    long AfterReadTime = System.currentTimeMillis();
                    if (AfterReadTime - lastPublishProgressTime > 500) {//publish progress every xxx sec
                        rate = 0.0;
                        long TotalRxAfterTest = TrafficStats.getTotalRxBytes();
                        double TimeDifference = AfterReadTime - lastPublishProgressTime;
                        double rxDiff = TotalRxAfterTest - TotalRxBeforeTest;
                        if (rxDiff != 0) {
                            double rxBPS = (rxDiff / (TimeDifference / 1000.0)); // total rx bytes per second.
                            rate = rxBPS * 8;
                        } else {
                            rate = 0.0;
                        }
                        double overallTimeDifference = AfterReadTime - initialTime;
                        double overallRxDiff = TotalRxAfterTest - initialTotalRx;
                        if (overallRxDiff != 0) {
                            double rxBPS = (overallRxDiff / (overallTimeDifference / 1000.0)); // total rx bytes per second.
                            theActivity.mobInfo.avRxRate = rxBPS * 8;
                        } else {
                            theActivity.mobInfo.avRxRate = 0.0;
                        }
                        if (AfterReadTime - downloadTest_StrtTime > MainActivity.testDuration) {
                            bDone = true;
                            break;
                        }
                        if (rate < theActivity.mobInfo.minRxRate) {
                            theActivity.mobInfo.minRxRate = rate;
                        }
                        if (rate > theActivity.mobInfo.maxRxRate) {
                            theActivity.mobInfo.maxRxRate = rate;
                        }
                        publishProgress(rate, theActivity.mobInfo.avRxRate);
                        lastPublishProgressTime = System.currentTimeMillis();
                        TotalRxBeforeTest = TrafficStats.getTotalRxBytes();
                    }
                }
                in.close();
                outStream.close();
                mSocket.close();
            }


            if (theActivity.mobInfo.minRxRate == Double.MAX_VALUE) {
                theActivity.mobInfo.minRxRate = 0;
            }
            Log.d("speedtest_total:", Long.toString(sum));
        } catch (SocketTimeoutException e) {
            // The socket read blocked for longer than socketTimeOut with no data -
            // typically means the server accepted the connection but never sent (or
            // stopped sending) the response body, e.g. after a redirect/error response.
            Log.e("Error-netSpeedTest: ", "Download server did not send data in time (" + socketTimeOut + "ms)");
            Log.d("err_speedtest_total:", Long.toString(sum));
        } catch (Exception e) {
            String eString = e.getMessage();
            if (eString != null) {
                Log.e("Error-netSpeedTest: ", eString);
            }
            Log.d("err_speedtest_total:", Long.toString(sum));
        }
        return null;
    }
    /**
     * Updating progress bar
     * */
    @Override
    protected void onProgressUpdate(Double... progress) {
        String str = MainActivity.getRateWithUnit(progress[0]);
        theActivity.txtRxRateText.setText(str);
        theActivity.txt_minmaxrx.setText("-"+", "+"-"+", "+MainActivity.getRateWithUnit(theActivity.mobInfo.avRxRate));
        theActivity.speedometer.setSpeed(progress[0].doubleValue() / 1024.0D / 1024.0D,1000,0);
    }

    @Override
    protected void onPostExecute(String file_url) {
        theActivity.speedometer.setSpeed(0.0D, true);
        theActivity.mobInfo.showInfo(theActivity);
    }

}