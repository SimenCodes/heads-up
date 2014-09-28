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
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import codes.simen.l50notifications.R;
import codes.simen.l50notifications.admin.AdminReceiver;
import codes.simen.l50notifications.util.Mlog;

public class DeviceAdminEnabler extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (((DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE)).isAdminActive(AdminReceiver.getComponentName(this))) {
            Toast.makeText(this, getString(R.string.device_admin_alredy_on), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
            finish();
            return;
        }

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                AdminReceiver.getComponentName(getApplicationContext()));
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getString(R.string.device_admin_enable_explanation));

        if (intent.resolveActivity(getPackageManager()) != null) {
            Mlog.d("DeviceAdminEnabler", "Is resolved");
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.device_admin_not_available),
                    Toast.LENGTH_SHORT).show();
            Mlog.w("DeviceAdminEnabler", "Not possible");
        }
    }

}
