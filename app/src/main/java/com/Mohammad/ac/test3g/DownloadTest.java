package com.Mohammad.ac.test3g;

import android.os.AsyncTask;
import android.util.Log;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.IRepeatListener;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

/**
 * Created by mohammad.haider on 017 2/17/2017.
 */

public class DownloadTest extends AsyncTask<Integer, Double, String> {
    MainActivity theActivity;
    boolean bDnTestComplete = false;
    boolean bFinish = false;

    DownloadTest(MainActivity activity) {
        theActivity=activity;
        theActivity.mobInfo.minRxRate = Double.MAX_VALUE;
        theActivity.mobInfo.maxRxRate = 0;
        theActivity.mobInfo.avRxRate = 0;
    }

    @Override
    protected void onProgressUpdate(Double... progress) {
        String str = MainActivity.getRateWithUnit(progress[0]);
        theActivity.txtRxRateText.setText(str);
        theActivity.txt_minmaxrx.setText("-"+", "+"-"+", "+MainActivity.getRateWithUnit(theActivity.mobInfo.avRxRate));
        theActivity.speedometer.setSpeed(progress[0].doubleValue() / 1024.0D / 1024.0D,1000,0);

        //theActivity.tvTmp.setText(Double.toString(progress[0]));
    }

    @Override
    protected void onPostExecute(String file_url) {
        //Log.d("speedtest","Finish");
        theActivity.speedometer.setSpeed(0.0D, true);
        theActivity.mobInfo.showInfo(theActivity);

        if (theActivity.mobInfo.minRxRate == Double.MAX_VALUE) {
            theActivity.mobInfo.minRxRate = 0;
        }
    }

    @Override
    protected String doInBackground(Integer... params) {

        SpeedTestSocket speedTestSocket = new SpeedTestSocket();

        // add a listener to wait for speedtest completion and progress
        speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

            @Override
            public void onDownloadFinished(SpeedTestReport report) {
                // called when download is finished
                Log.v("speedtest", "[DL FINISHED] rate in octet/s : " + report.getTransferRateOctet());
                Log.v("speedtest", "[DL FINISHED] rate in bit/s   : " + report.getTransferRateBit());
                bDnTestComplete = true;
            }

            @Override
            public void onDownloadError(SpeedTestError speedTestError, String errorMessage) {
                // called when a download error occur
                Log.v("speedtest", "[DL Error] : " + errorMessage);
                bDnTestComplete = true;
            }

            @Override
            public void onUploadFinished(SpeedTestReport report) {
                // called when an upload is finished
                Log.v("speedtest", "[UL FINISHED] rate in octet/s : " + report.getTransferRateOctet());
                Log.v("speedtest", "[UL FINISHED] rate in bit/s   : " + report.getTransferRateBit());
                bDnTestComplete = true;
            }

            @Override
            public void onUploadError(SpeedTestError speedTestError, String errorMessage) {
                // called when an upload error occur
                Log.v("speedtest", "[UL Error] : " + errorMessage);
                bDnTestComplete = true;
            }
            long initTime =System.currentTimeMillis();

            @Override
            public void onDownloadProgress(float percent, SpeedTestReport report) {
                // called to notify download progress
                //Log.v("speedtest", "[DL PROGRESS] progress : " + percent + "%");
                //Log.v("speedtest", "[DL PROGRESS] rate in octet/s : " + report.getTransferRateOctet());
                //Log.v("speedtest", "[DL PROGRESS] rate in bit/s   : " + report.getTransferRateBit());
                double rate = report.getTransferRateBit().doubleValue();
                if (rate < theActivity.mobInfo.minRxRate) {
                    theActivity.mobInfo.minRxRate = rate;
                }
                if (rate > theActivity.mobInfo.maxRxRate) {
                    theActivity.mobInfo.maxRxRate = rate;
                }
                if (theActivity.mobInfo.avRxRate == 0) {
                    theActivity.mobInfo.avRxRate = rate;
                } else {
                    theActivity.mobInfo.avRxRate = (theActivity.mobInfo.avRxRate + rate)/2.0;
                }
                long curTime =System.currentTimeMillis();
                if(curTime-initTime >500) {
                    publishProgress(rate);
                    initTime=curTime;
                }
            }

            @Override
            public void onUploadProgress(float percent, SpeedTestReport report) {
                // called to notify upload progress
                //Log.v("speedtest", "[UL PROGRESS] progress : " + percent + "%");
                //Log.v("speedtest", "[UL PROGRESS] rate in octet/s : " + report.getTransferRateOctet());
                //Log.v("speedtest", "[UL PROGRESS] rate in bit/s   : " + report.getTransferRateBit());
            }

            @Override
            public void onInterruption() {
                // triggered when forceStopTask is called
                Log.v("speedtest", "[Forced Stop]");
                bDnTestComplete = true;
            }
        });

        //speedTestSocket.startDownload("2.testdebit.info", "/fichiers/100Mo.dat");
        //speedTestSocket.startDownload("download.thinkbroadband.com", "/20MB.zip");
        //speedTestSocket.startFixedDownload("ajerlitaxi.com", 80, "/3gtests/files_db/8MB.bin", params[0]);
        speedTestSocket.startDownloadRepeat("ajerlitaxi.com", "/3gtests/files_db/8MB.bin",
                params[0], 2000, new
                        IRepeatListener() {
                            @Override
                            public void onFinish(final SpeedTestReport report) {
                                // called when repeat task is finished
                                bFinish = true;
                            }

                            @Override
                            public void onReport(final SpeedTestReport report) {
                                // called when a download report is dispatched
                            }
                        });
        //keep the task running
        while(bFinish == false){

        }
        return null;
    }
}
