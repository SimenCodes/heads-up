package codes.simen.l50notifications.ui;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import codes.simen.l50notifications.R;
import codes.simen.l50notifications.admin.AdminReceiver;
import codes.simen.l50notifications.util.Mlog;

public class DeviceAdminEnabler extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (((DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE)).isAdminActive(AdminReceiver.getComponentName(this))) {
            Toast.makeText(this, "Already a device manager.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Device provisioning is not enabled. Stopping.",
                    Toast.LENGTH_SHORT).show();
            Mlog.w("DeviceAdminEnabler", "Not possible");
        }
    }

}
