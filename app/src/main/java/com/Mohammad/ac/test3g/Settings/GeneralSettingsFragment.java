/*******************************************************************************
 * This file is part of GPSLogger for Android.
 *
 * GPSLogger for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * GPSLogger for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.Mohammad.ac.test3g.Settings;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.Mohammad.ac.test3g.R;

public class GeneralSettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener
{

    //int aboutClickCounter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);

        Preference prefListeners = findPreference("speedmeterMax");
        prefListeners.setOnPreferenceClickListener(this);
        prefListeners.setOnPreferenceChangeListener(this);

        /*Preference profileListeners = findPreference("user_profile");
        profileListeners.setOnPreferenceClickListener(this);*/
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ListPreference list = (ListPreference) preference;
        int index = list.findIndexOfValue(newValue.toString());
        if (index != -1)
        {
            Log.v("speed_pref", Integer.toString(index));
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference.getKey().equalsIgnoreCase("speedmeterMax")){

            return true;
        } /*else if(preference.getKey().equalsIgnoreCase("user_profile")){
            startActivity(new Intent(GeneralSettingsFragment.this.getActivity(), ProfileActivity.class));
        }*/
        return false;
    }
}
