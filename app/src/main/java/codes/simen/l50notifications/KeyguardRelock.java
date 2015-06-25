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

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.preference.PreferenceManager;

import codes.simen.l50notifications.util.Mlog;

public class KeyguardRelock extends Service {

    public static final String logTag = "KeyguardRelock";

    public KeyguardRelock() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // not bindable
    }

    private BroadcastReceiver mReceiver;
    @SuppressWarnings("deprecation")
    private KeyguardManager.KeyguardLock keyguardLock = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        super.onStartCommand(intent, flags, startid);

        if (intent != null) {
            final String action = intent.getAction();
            Mlog.d(logTag, action);

            if      (Intent.ACTION_SCREEN_ON.equals(action))  waitForLock();
            else if (Intent.ACTION_SCREEN_OFF.equals(action)) doLock();
            else                                              stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void waitForLock() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        //noinspection deprecation
        keyguardLock = keyguardManager.newKeyguardLock("heads-up");
        try {
            keyguardLock.disableKeyguard();

            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            mReceiver = new OffReceiver();
            registerReceiver(mReceiver, intentFilter);
        } catch (SecurityException se) {
            keyguardLock = null;
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .edit()
                    .putBoolean("dismiss_keyguard", false)
                    .apply();
            stopSelf();
        }
    }

    private void doLock() {
        try {
            unregisterReceiver(mReceiver);
        } catch (RuntimeException rte) {
            Mlog.w(logTag, rte.getMessage());
        }
        if (keyguardLock != null)
            keyguardLock.reenableKeyguard();
        keyguardLock = null;
        stopSelf();
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        if (keyguardLock != null) {
            unregisterReceiver(mReceiver);
            keyguardLock.reenableKeyguard();
            keyguardLock = null;
        }
    }

    class OffReceiver extends BroadcastReceiver {
        public OffReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Intent i = new Intent(context, KeyguardRelock.class);
                i.setAction(Intent.ACTION_SCREEN_OFF);
                context.startService(i);
            }
        }
    }
}
