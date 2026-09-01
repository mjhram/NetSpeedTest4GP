/*
*    This file is part of GPSLogger for Android.
*
*    GPSLogger for Android is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 2 of the License, or
*    (at your option) any later version.
*
*    GPSLogger for Android is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.Mohammad.ac.SpeedTest.Settings;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.Mohammad.ac.SpeedTest.R;

public class MainPreferenceActivity extends AppCompatActivity{
    Fragment preferenceFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferences);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String whichFragment = PreferenceConstants.GENERAL;
        if (getIntent().getExtras() != null) {
            // This activity is exported with a public URI scheme (gpslogger://authorize),
            // so the incoming intent may come from any other app or a web link. Never
            // trust its contents outright - getString() can return null here if a
            // caller sends extras without this key, and switch(null) throws an NPE.
            String requested = getIntent().getExtras().getString("preference_fragment");
            if (requested != null) {
                whichFragment = requested;
            }
        }

        switch(whichFragment){
            case PreferenceConstants.GENERAL:
                setTitle(R.string.settings_screen_name);
                preferenceFragment = new GeneralSettingsFragment();
                break;
            /*case PreferenceConstants.PERFORMANCE:
                setTitle(R.string.pref_performance_title);
                preferenceFragment = new PerformanceSettingsFragment();
                break;*/
            default:
                // Unrecognized value (e.g. from the exported gpslogger:// deep link) -
                // fall back to the general settings screen instead of crashing.
                setTitle(R.string.settings_screen_name);
                preferenceFragment = new GeneralSettingsFragment();
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, preferenceFragment)
                .commit();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int id = item.getItemId();
        /*if (id == android.R.id.home) {
            //NavUtils.navigateUpFromSameTask(this);
            return false;
        }*/

        return super.onOptionsItemSelected(item);
    }

    public static class PreferenceConstants{
        public static final String GENERAL = "GeneralSettingsFragment";
        //public static final String PERFORMANCE = "PerformanceSettingsFragment";
    }

}
