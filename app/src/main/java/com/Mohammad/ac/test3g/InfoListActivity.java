package com.Mohammad.ac.test3g;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/*public class InfoListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_list);
    }
}*/

public class InfoListActivity extends ListActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHandler dbHandler = new databaseHandler(this);
        List<c_Info> values = dbHandler.getAll3gTests();
        MyInfoArrayAdapter adapter = new MyInfoArrayAdapter(this, values);
        setListAdapter(adapter);
    }

    /*@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //String item = (String) getListAdapter().getItem(position);
        Toast.makeText(this, position + " selected", Toast.LENGTH_LONG).show();
    }*/

    public void onLocationClick(View paramView)
    {
        String tmp = ((TextView) paramView).getText().toString();
        String loc[] = tmp.split(",");
        double lat = Double.parseDouble(loc[0]);
        double lon = Double.parseDouble(loc[1]);

        if ((lat == 0.0D) && (lon == 0.0D)) {
            return;
        }
        Uri localUri = Uri.parse("geo:0,0?q=" + lat + "," + lon);
        Intent localIntent = new Intent("android.intent.action.VIEW", localUri);
        startActivity(localIntent);
    }
}

class MyInfoArrayAdapter extends ArrayAdapter<c_Info> {
    private final Context context;
    private final List<c_Info> mobInfoArray;

    public MyInfoArrayAdapter(Context context, List<c_Info> values) {
        super(context, R.layout.info_row_layout, values);
        this.context = context;
        this.mobInfoArray = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView txt_tmp;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.info_row_layout, parent, false);

        showInfo(mobInfoArray.get(position), rowView);

        return rowView;
    }

    private void showInfo(c_Info info, View rowView) {
        TextView txt_tmp;
        txt_tmp = (TextView) rowView.findViewById(R.id.textViewTime);
        txt_tmp.setText(getDate(info.time));

        txt_tmp = (TextView) rowView.findViewById(R.id.textViewModel);
        txt_tmp.setText(info.manuf.toUpperCase() + "/" + info.model);
        txt_tmp = (TextView) rowView.findViewById(R.id.textViewNetSrce);
        txt_tmp.setText(info.netSource);
        txt_tmp = (TextView) rowView.findViewById(R.id.textViewWifiState);
        if(info.wifiIsConnected) {
            txt_tmp.setText("Connected");
        } else {
            txt_tmp.setText("Disconnected");
        }
        txt_tmp = (TextView) rowView.findViewById(R.id.textViewWifiSSID);
        txt_tmp.setText(info.wifiSsid);

        txt_tmp = (TextView) rowView.findViewById(R.id.id_netclass);
        txt_tmp.setText(info.netClass+" - "+info.netClass2);
        txt_tmp = (TextView) rowView.findViewById(R.id.id_netname);
        txt_tmp.setText(info.netName+" - "+info.netName2);
        if(info.netClass.equals("2G")) {
            txt_tmp = (TextView) rowView.findViewById(R.id.id_cellid);
            txt_tmp.setText(""+info.cid);
            txt_tmp = (TextView) rowView.findViewById(R.id.id_rnc);
            txt_tmp.setText("");
        }else {
            txt_tmp = (TextView) rowView.findViewById(R.id.id_cellid);
            txt_tmp.setText(String.format("%04d",info.cid_3g));
            txt_tmp = (TextView) rowView.findViewById(R.id.id_rnc);
            txt_tmp.setText(""+info.rnc);
        }
        txt_tmp = (TextView) rowView.findViewById(R.id.id_lac);
        txt_tmp.setText(""+info.lac);
        txt_tmp = (TextView) rowView.findViewById(R.id.id_rssi);
        txt_tmp.setText(""+info.rssi);
        txt_tmp = (TextView) rowView.findViewById(R.id.id_minmaxrate);
        txt_tmp.setText(MainActivity.getRateWithUnit(info.minRxRate)+", "+MainActivity.getRateWithUnit(info.maxRxRate)+", "+MainActivity.getRateWithUnit(info.avRxRate));
        txt_tmp = (TextView) rowView.findViewById(R.id.id_minmaxTxrate);
        txt_tmp.setText(MainActivity.getRateWithUnit(info.minTxRate)+", "+MainActivity.getRateWithUnit(info.maxTxRate)+", "+MainActivity.getRateWithUnit(info.avTxRate));
        txt_tmp = (TextView) rowView.findViewById(R.id.id_lat);
        txt_tmp.setText(""+info.lat + ", " + +info.lon);

    }

    private String getDate(String time) {
        Timestamp timestamp = Timestamp.valueOf(time);
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();

        calendar.setTimeInMillis(timestamp.getTime());
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));

        SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
        Date currenTimeZone = (Date)calendar.getTime();
        String date = sdf.format(currenTimeZone);

        return date;
    }

}


