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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import codes.simen.l50notifications.R;
import codes.simen.l50notifications.util.Mlog;

public class RequestBroadcastDialog extends Activity {

    public static final String ACTION_REQUEST = "codes.simen.l50notifications.REQUEST_BROADCAST";
    public static final String ACTION_CEHCK = "codes.simen.l50notifications.CHECK_BROADCAST";
    public static final String EXTRA_APP_NAME = "app_name";
    private String appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getAction().equals(ACTION_CEHCK)) {
            if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("broadcast_notifications", false)
                    && WelcomeActivity.isNotificationListenerEnabled(getApplicationContext())) {
                sendResult(RESULT_OK);
                return;
            }
        }

        appName = getIntent().getStringExtra(EXTRA_APP_NAME);

        if (appName == null) {
            Mlog.w("RequestBroadcastDialog", "Invalid or no app name");
            sendResult(RESULT_CANCELED);
            return;
        }

        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("broadcast_notifications", false)) {
            sendResult(RESULT_OK);
            return;
        }

        if (!WelcomeActivity.isNotificationListenerEnabled(getApplicationContext())) {
            showErrorDialog();
            return;
        }

        showAccessDialog(appName);
    }

    private void showAccessDialog(String appName) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_broadcast_request))
                .setMessage(String.format(getString(R.string.content_broadcast_request), appName))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                .edit()
                                .putBoolean("broadcast_notifications", true)
                                .commit();
                        sendResult(RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(RESULT_CANCELED);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        sendResult(RESULT_CANCELED);
                    }
                })
                .show();
    }

    private void showErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_broadcast_request))
                .setMessage("Please give Heads-up access to your notifications before you continue")
                .setPositiveButton("Open Heads-up", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(getApplicationContext(), WelcomeActivity.class), 18);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(RESULT_CANCELED);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        sendResult(RESULT_CANCELED);
                    }
                })
                .show();
    }

    private void sendResult(int resultCode) {
        Intent result = new Intent(ACTION_REQUEST).putExtra("result", resultCode);
        setResult(resultCode, result);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 18) {
            if (!WelcomeActivity.isNotificationListenerEnabled(getApplicationContext())) {
                showErrorDialog();
                return;
            }

            showAccessDialog(appName);
        }
    }
}