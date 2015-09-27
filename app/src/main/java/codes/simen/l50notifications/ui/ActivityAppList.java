/*
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package codes.simen.l50notifications.ui;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import codes.simen.l50notifications.R;
import codes.simen.l50notifications.util.Mlog;
import codes.simen.l50notifications.util.ObjectSerializer;

public class ActivityAppList extends ListActivity {
    private final String logTag = "AppList";
    private AppAdapter adapter = null;
    private SharedPreferences prefs;
    private Set<String> blacklisted;
    private String type = "blacklist";
    private boolean checkedWhenInList = false;
    private boolean invertedList = false;

    private PackageManager pm;
    private List<ApplicationInfo> pkgs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        type = getIntent().getAction().substring(48);
        Mlog.d(type, String.valueOf(prefs.contains(type)));
        if ("blacklist".equals(type)) {
            setTitle(R.string.title_activity_app_list);
        } else {
            setTitle(R.string.title_activity_noshow_list);
            checkedWhenInList = true;
            ((TextView) findViewById(R.id.titleView)).setText(R.string.blocklist_desc);
            if (Build.VERSION.SDK_INT >= 23)
                findViewById(R.id.marshmallow_applist_error).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            String serializedBlacklist = prefs.getString(type/* + "Serial"*/, "");
            //noinspection unchecked
            blacklisted = (Set<String>) ObjectSerializer.deserialize(serializedBlacklist);
            if (blacklisted == null) {
                Mlog.w(logTag, "null");
                blacklisted = new HashSet<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "IOe " + e.getMessage(), Toast.LENGTH_LONG).show();
            blacklisted = new HashSet<>();
            //finish();
        } catch (ClassCastException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "CCe " + e.getMessage(), Toast.LENGTH_LONG).show();
            blacklisted = new HashSet<>();
            //finish();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "CNFe " + e.getMessage(), Toast.LENGTH_LONG).show();
            blacklisted = new HashSet<>();
            //finish();
        }
        invertedList = prefs.getBoolean(type + "_inverted", false);

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkAllBox);
        checkBox.setEnabled(false);
        if (blacklisted.size() == 0 && !invertedList) {
            checkBox.setChecked(!checkedWhenInList);
        } else {
            checkBox.setChecked(checkedWhenInList);
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                blacklisted.clear();
                invertedList = isChecked == checkedWhenInList;

                //adapter.clear();
                adapter = null;
                loadAdapter(true);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                pm = getPackageManager();

                pkgs = pm.getInstalledApplications(0);
                if (pkgs == null) finish();

                Collections.sort(pkgs, new ApplicationInfo.DisplayNameComparator(pm));

                loadThreadHandler.sendEmptyMessage(0);
            }
        }).start();


    }

    private final Handler loadThreadHandler = new Handler() {
        @SuppressLint("NewApi")
        public void handleMessage (Message msg) {
            if (Build.VERSION.SDK_INT >= 19 && ((ActivityManager) getSystemService(ACTIVITY_SERVICE))
                    .isLowRamDevice())
                 loadAdapter(false);
            else loadAdapter(true);

            findViewById(R.id.progressBar).setVisibility(View.GONE);
            findViewById(android.R.id.list).setVisibility(View.VISIBLE);
            findViewById(R.id.checkAllBox).setEnabled(true);
        }
    };

    private void loadAdapter(boolean isIcons) {
        try {
            adapter = new AppAdapter(pm, pkgs, isIcons);
            setListAdapter(adapter);
        } catch (OutOfMemoryError outOfMemoryError) {
            adapter.clear();
            adapter = new AppAdapter(pm, pkgs, false);
            setListAdapter(adapter);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ApplicationInfo pkg = adapter.getItem(position);

        if (pkg == null) {
            Toast.makeText(getApplicationContext(), "ERROR (null) at AppList ln114", Toast.LENGTH_LONG).show();
            return;
        }
        Mlog.d(logTag, pkg.packageName);

        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            CheckBox checkBox = (CheckBox) vg.getChildAt(0);
            assert checkBox != null;
            boolean isChecked = checkBox.isChecked();
            if (checkedWhenInList) {
                isChecked = !isChecked;
            }
            if (isChecked) {
                if (!invertedList) blacklisted.add(pkg.packageName);
                else               blacklisted.remove(pkg.packageName);
                checkBox.setChecked(checkedWhenInList);
            } else {
                if (!invertedList) blacklisted.remove(pkg.packageName);
                else               blacklisted.add(pkg.packageName);
                checkBox.setChecked(!checkedWhenInList);
            }

            final boolean manyInList = pkgs.size() < blacklisted.size() * 2;
            Mlog.v(logTag + "Invert", String.valueOf(manyInList));
            if (manyInList && !invertedList) {
                invertedList = true;
                for (ApplicationInfo applicationInfo : pkgs) {
                    final String s = applicationInfo.packageName;
                    if (blacklisted.contains(s)) blacklisted.remove(s);
                    else                         blacklisted.add(s);
                    Mlog.v(s, String.valueOf(blacklisted.contains(s)));
                }
            } else if (manyInList && invertedList) {
                invertedList = false;
                for (ApplicationInfo applicationInfo : pkgs) {
                    final String s = applicationInfo.packageName;
                    if (blacklisted.contains(s)) blacklisted.add(s);
                    else                         blacklisted.remove(s);
                    Mlog.v(s, String.valueOf(blacklisted.contains(s)));
                }
            }
        }

    }

    class AppAdapter extends ArrayAdapter<ApplicationInfo> {
        private PackageManager pm = null;
        private boolean isIcons;

        AppAdapter(PackageManager pm, List<ApplicationInfo> apps, boolean isIcons) {
            super(ActivityAppList.this, R.layout.row_app_list, apps);
            this.isIcons = isIcons;
            this.pm = pm;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = newView(parent);
            }

            bindView(position, convertView);

            return (convertView);
        }

        private View newView(ViewGroup parent) {
            return (getLayoutInflater().inflate(R.layout.row_app_list, parent, false));
        }

        private void bindView(int position, View row) {
            ApplicationInfo applicationInfo = getItem(position);

            TextView subTitleView = (TextView) row.findViewById(R.id.subTitleView);
            subTitleView.setText(applicationInfo.packageName);

            final CharSequence appLabel = pm.getApplicationLabel(applicationInfo);
                TextView titleView = (TextView) row.findViewById(R.id.titleView);
            if (!appLabel.equals(applicationInfo.packageName)) {
                titleView.setVisibility(View.VISIBLE);
                titleView.setText(appLabel);
            } else {
                titleView.setVisibility(View.GONE);
            }

            CheckBox checkBox = (CheckBox) row.findViewById(R.id.checkbox);
            boolean isInList = blacklisted.contains(applicationInfo.packageName);
            if (invertedList) isInList = !isInList;

            if (isInList) {
                //Mlog.d(activityInfo.packageName, "false");
                checkBox.setChecked(checkedWhenInList);
            } else {
                //Mlog.d(activityInfo.packageName, "true");
                checkBox.setChecked(!checkedWhenInList);
            }

            ImageView icon = (ImageView) row.findViewById(R.id.icon);
            if (isIcons) {
                try {
                    icon.setImageDrawable(pm.getApplicationIcon(applicationInfo));
                } catch (OutOfMemoryError outOfMemoryError) {
                    Mlog.e(logTag, outOfMemoryError.toString());
                    isIcons = false;
                    adapter = null;
                    loadAdapter(false);
                }
            } else {
                icon.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onStop () {
        super.onStop();
        SharedPreferences.Editor editor = prefs.edit();
        /*if (Build.VERSION.SDK_INT >= 11)
            editor.putStringSet(type, blacklisted);
        else {*/
            try {
                editor.putString(type/* + "Serial"*/, ObjectSerializer.serialize( (Serializable) blacklisted));
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "IOe " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        //}
        editor.putBoolean(type + "_inverted", invertedList);
        editor.apply();
        //Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
        Mlog.v(logTag, "Saved " + type + " " + blacklisted.toString() + " - " + String.valueOf(invertedList));
    }
}
