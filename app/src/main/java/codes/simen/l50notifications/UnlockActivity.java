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

package codes.simen.l50notifications;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

import codes.simen.l50notifications.util.Mlog;


public class UnlockActivity extends Activity {
    public static final String logTag = "UnlockActivity";

    private boolean isPendingIntentStarted = false;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //register for user present so we don't have to manually check kg with the keyguard manager
        IntentFilter userUnlock = new IntentFilter (Intent.ACTION_USER_PRESENT);
        registerReceiver(unlockDone, userUnlock);

        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Mlog.v(logTag, "creating dismiss window");

        handler.postDelayed(timeoutRunnable, 2000);
    }

    private final Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isFinishing()) {
                startPendingIntent();
                finish();
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        Mlog.d(logTag, "Intent received: " + intent);
        setIntent(intent);
    }

    BroadcastReceiver unlockDone = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Double-check we got the right intent
            if (!intent.getAction().equals(Intent.ACTION_USER_PRESENT)) return;

            Mlog.v(logTag, "Unlocked!");

            startPendingIntent();

            moveTaskToBack(true);
            finish();
        }
    };

    private void startPendingIntent() {
        if (isPendingIntentStarted) return;
        isPendingIntentStarted = true;
        Intent intent;Bundle extras = getIntent().getExtras();
        PendingIntent pendingIntent = (PendingIntent) extras.get("action");

        try {
            intent = new Intent();
            if (extras.getBoolean("floating", false)) {
                intent.addFlags(OverlayServiceCommon.FLAG_FLOATING_WINDOW);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            pendingIntent.send(getApplicationContext(), 0, intent);

        } catch (PendingIntent.CanceledException e) {
            OverlayServiceCommon.reportError(e, "App has canceled action", getApplicationContext());
            Toast.makeText(getApplicationContext(), getString(R.string.pendingintent_cancel_exception), Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            OverlayServiceCommon.reportError(e, "No action defined", getApplicationContext());
            Toast.makeText(getApplicationContext(), getString(R.string.pendingintent_null_exception), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        startPendingIntent();
        super.onDestroy();
        Mlog.v(logTag, "Destroy");
        unregisterReceiver(unlockDone);
    }
}
