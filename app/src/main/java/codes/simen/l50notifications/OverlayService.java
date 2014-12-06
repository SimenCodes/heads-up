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

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import codes.simen.l50notifications.util.Mlog;

/*
 * Created as a separate class because some devices running 4.2 and earlier failed to
 * load classes containing references to NotificationListener
 */
public class OverlayService extends OverlayServiceCommon {
    private int onBindAction = 0; // 0=nothing, 1=remove, 2=check existence

    @Override
    public void doDismiss (boolean mStopNow) {
        Mlog.d(logTag, packageName + tag + id);

        onBindAction = 1;

        Intent intent = new Intent(this, NotificationListenerService.class);
        intent.setAction(NotificationListenerService.ACTION_CUSTOM);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            NotificationListenerService.LocalBinder binder = (NotificationListenerService.LocalBinder) service;
            NotificationListenerService listenerService = binder.getService();
            Mlog.d(logTag, "serviceConnected");

            switch (onBindAction) {
                case 1:
                    if (Build.VERSION.SDK_INT >= 20) listenerService.doRemove(key);
                    else                             listenerService.doRemove(packageName, tag, id);
                    unbindService(mConnection);
                    stopSelf();
                    break;
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
