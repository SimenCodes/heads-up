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

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

import codes.simen.l50notifications.R;
import codes.simen.l50notifications.util.Mlog;

public class SetLocaleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_locale);
        Configuration configuration = getResources().getConfiguration();
        Mlog.v("LOCALE", configuration.locale);
        ((EditText) findViewById(R.id.editText)).setText(configuration.locale.toString());
    }

    public void setLocale(View view) {
        if (Build.VERSION.SDK_INT >= 17) {
            Resources resources = getResources();
            Configuration resourcesConfiguration = resources.getConfiguration();
            resourcesConfiguration.setLocale(new Locale(
                    ((EditText) findViewById(R.id.editText)).getText().toString()
            ));
            resources.updateConfiguration(resourcesConfiguration, resources.getDisplayMetrics());
        } else {
            Toast.makeText(getApplicationContext(), "Sorry, not supported on your device (Android <= 4.2)", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}