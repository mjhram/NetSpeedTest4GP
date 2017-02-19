package com.Mohammad.ac.test3g;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.IRepeatListener;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

/**
 * Created by mohammad.haider on 017 2/17/2017.
 */
// This is not working because the value of upload speed initially very large!!

public class UploadTest extends AsyncTask<Integer, Double, String> {
    MainActivity theActivity;
    boolean bDnTestComplete = false;
    boolean bUpTestComplete = false;
    boolean bFinish = false;
    boolean bInitial = true;

    UploadTest(MainActivity activity) {
        theActivity=activity;
        theActivity.mobInfo.minTxRate = Double.MAX_VALUE;
        theActivity.mobInfo.maxTxRate = 0;
        theActivity.mobInfo.avTxRate = 0;
    }

    @Override
    protected void onProgressUpdate(Double... progress) {
        String str = MainActivity.getRateWithUnit(progress[0]);
        theActivity.txtTxRateText.setText(str);
        theActivity.txt_minmaxtx.setText("-"+", "+"-"+", "+MainActivity.getRateWithUnit(theActivity.mobInfo.avTxRate));
        theActivity.speedometer.setSpeed(progress[0].doubleValue() / 1024.0D / 1024.0D,1000,0);
        //theActivity.tvTmp.setText(Double.toString(progress[0]));
        Log.v("speedtest", "[ProgressUpdate:]"+Double.toString(progress[0]));
    }

    @Override
    protected void onPostExecute(String file_url) {
        //Log.d("speedtest","Finish");
        theActivity.speedometer.setSpeed(0.0D, true);
        theActivity.mobInfo.showInfo(theActivity);
        theActivity.btnStartTest.setVisibility(View.VISIBLE);
        theActivity.btnHistory.setVisibility(View.VISIBLE);

        if (theActivity.mobInfo.minTxRate == Double.MAX_VALUE) {
            theActivity.mobInfo.minTxRate = 0;
        }

        if(theActivity.mobInfo.minTxRate == Double.MAX_VALUE) {
            theActivity.mobInfo.minTxRate = 0;
        }
        //upload info to server
        theActivity.mobInfo.upload(theActivity);
        Log.v("speedtest", "[PostExec]");
    }

    @Override
    protected String doInBackground(Integer... params) {

        SpeedTestSocket speedTestSocket = new SpeedTestSocket();

        // add a listener to wait for speedtest completion and progress
        speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

            @Override
            public void onDownloadFinished(SpeedTestReport report) {
                // called when download is finished
                //Log.v("speedtest", "[DL FINISHED] rate in octet/s : " + report.getTransferRateOctet());
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
                //Log.v("speedtest", "[UL FINISHED] rate in octet/s : " + report.getTransferRateOctet());
                Log.v("speedtest", "[UL FINISHED] rate in bit/s   : " + report.getTransferRateBit());
                bUpTestComplete = true;
                bInitial = true;
            }

            @Override
            public void onUploadError(SpeedTestError speedTestError, String errorMessage) {
                // called when an upload error occur
                Log.v("speedtest", "[UL Error] : " + errorMessage);
                bUpTestComplete = true;
                bInitial = true;
            }
            long initTime = System.currentTimeMillis();

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
                //Log.v("speedtest", "[DnldProgress]");
            }

            @Override
            public void onUploadProgress(float percent, SpeedTestReport report) {
                if(bInitial) {
                    bInitial=false;
                    Log.v("speedtest", "[Skip]");
                    return;
                }
                // called to notify upload progress
                //Log.v("speedtest", "[UL PROGRESS] progress : " + percent + "%");
                //Log.v("speedtest", "[UL PROGRESS] rate in octet/s : " + report.getTransferRateOctet());
                //Log.v("speedtest", "[UL PROGRESS] rate in bit/s   : " + report.getTransferRateBit());
                double rate = report.getTransferRateBit().doubleValue();
                if (rate < theActivity.mobInfo.minTxRate) {
                    theActivity.mobInfo.minTxRate = rate;
                }
                if (rate > theActivity.mobInfo.maxTxRate) {
                    theActivity.mobInfo.maxTxRate = rate;
                }
                if (theActivity.mobInfo.avTxRate == 0) {
                    theActivity.mobInfo.avTxRate = rate;
                } else {
                    theActivity.mobInfo.avTxRate = (theActivity.mobInfo.avTxRate + rate)/2.0;
                }
                long curTime =System.currentTimeMillis();
                if(curTime-initTime >500) {
                    publishProgress(rate);
                    initTime=curTime;
                }
                //Log.v("speedtest", "[UpldProgress]");
            }

            @Override
            public void onInterruption() {
                // triggered when forceStopTask is called
                Log.v("speedtest", "[Forced Stop]");
                bDnTestComplete = true;
                bUpTestComplete = true;

            }
        });

        //speedTestSocket.startDownload("2.testdebit.info", "/fichiers/100Mo.dat");
        //speedTestSocket.startDownload("download.thinkbroadband.com", "/20MB.zip");
        //speedTestSocket.startFixedDownload("ajerlitaxi.com", 80, "/3gtests/files_db/8MB.bin", params[0]);
        speedTestSocket.startUploadRepeat("ajerlitaxi.com", "/",
                params[0], 2000, 1000000, new
                        IRepeatListener() {
                            @Override
                            public void onFinish(final SpeedTestReport report) {
                                // called when repeat task is finished
                                bFinish = true;
                                Log.v("speedtest", "[Rpt Finish]");
                            }

                            @Override
                            public void onReport(final SpeedTestReport report) {
                                // called when a download report is dispatched
                                Log.v("speedtest", "[Rpt]");
                            }
                        });
        //keep the task running
        while(bFinish == false){

        }
        Log.v("speedtest", "[AsyncTask ended]");
        return null;
    }
}
