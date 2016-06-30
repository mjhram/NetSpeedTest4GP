package com.Mohammad.ac.test3g;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohammad.haider on 5/16/2016.
 */
public class databaseHandler extends SQLiteOpenHelper {
        // All Static variables
        // Database
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "netSpeedInfo.db";

        // Contacts table name
        private static final String TABLE_3gTests = "netTests";

        public databaseHandler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // Creating Tables
        @Override
        public void onCreate(SQLiteDatabase db) {
             {


                String CREATE_3gTests_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_3gTests + "("
                        + "No INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,  time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                        + " deviceId varchar(20)  DEFAULT NULL,"
                        + " Brand varchar(15)  DEFAULT NULL,"
                        + " Manufacturer varchar(15)  DEFAULT NULL,"
                        + " Model varchar(15)  DEFAULT NULL,"
                        + " Product varchar(15)  DEFAULT NULL,"
                        + " imsi varchar(20)  DEFAULT NULL,"
                        + " phoneNumber varchar(20)  DEFAULT NULL,"
                        + " imei varchar(20)  DEFAULT NULL,"
                        + " netOperator varchar(10)  DEFAULT NULL,"
                        + " netName varchar(10)  DEFAULT NULL,"
                        + " netType int(11) DEFAULT NULL,"
                        + " netClass varchar(2)  DEFAULT NULL,"
                        + " phoneType int(11) DEFAULT NULL,"
                        + " mobileState varchar(20)  DEFAULT NULL,"
                        + " cid int(11) DEFAULT NULL,"
                        + " cid_3g int(11) DEFAULT NULL,"
                        + " rnc int(11) DEFAULT NULL,"
                        + " lac int(11) DEFAULT NULL,"
                        + " rssi int(11) DEFAULT NULL,"
                        + " minRxRate int(11) DEFAULT NULL,"
                        + " maxRxRate int(11) DEFAULT NULL,"
                        + " avRxRate int(11) DEFAULT NULL,"
                        + " deviceId2 varchar(20)  DEFAULT NULL,"
                        + " imsi2 varchar(20)  DEFAULT NULL,"
                        + " phoneNum2 varchar(20)  DEFAULT NULL,"
                        + " netOperator2 varchar(10)  DEFAULT NULL,"
                        + " netName2 varchar(10)  DEFAULT NULL,"
                        + " netType2 int(11) DEFAULT NULL,"
                        + " netClass2 varchar(2)  DEFAULT NULL,"
                        + " SignalStrengths varchar(256)  NOT NULL,"
                        + " nei varchar(100)  NOT NULL,"
                        + " tmp varchar(100)  NOT NULL,"
                        + " lon double NOT NULL DEFAULT '0',"
                        + " lat double NOT NULL DEFAULT '0',"
                        + " minTxRate int(11) NOT NULL,"
                        + " maxTxRate int(11) NOT NULL,"
                        + " avTxRate int(11) NOT NULL, "
                        + " cdmaDbm int(11) NOT NULL, "
                        + " cdmaEcio int(11) NOT NULL, "
                        + " wifissid varchar(35)  DEFAULT NULL,"
                        + " netsrc varchar(15)  DEFAULT NULL"
                        + ")";
                 Log.d("Test", CREATE_3gTests_TABLE);
                db.execSQL(CREATE_3gTests_TABLE);
            }
        }

        // Upgrading database
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Drop older table if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_3gTests);

            // Create tables again
            onCreate(db);
        }

        /**
         * All CRUD(Create, Read, Update, Delete) Operations
         */

        // Adding new 3gTest
        void add3gTest(c_Info mobInfo) {
            SQLiteDatabase db = this.getWritableDatabase();
            mobInfo.add3gTest2db(db);
            db.close();
        }

        // Getting All 3gTests
        public List<c_Info> getAll3gTests() {
            List<c_Info> cInfoList = new ArrayList<c_Info>();
            // Select All Query
            String selectQuery = "SELECT  * FROM " + TABLE_3gTests + " ORDER BY No DESC";
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    c_Info tmpMobInfo = c_Info.getInfoFromRow(cursor);
                    // Adding cInfo to list
                    cInfoList.add(tmpMobInfo);
                } while (cursor.moveToNext());
            }
            // return 3gTests list
            return cInfoList;
        }

        // Getting contacts Count
        public int get3gTestsCount() {
            String countQuery = "SELECT  * FROM " + TABLE_3gTests;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(countQuery, null);
            int count = cursor.getCount();
            cursor.close();
            return count;
        }
}
