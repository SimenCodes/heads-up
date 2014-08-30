package codes.simen.l50notifications;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.view.View;

import codes.simen.l50notifications.util.Mlog;

/*
 * Created as a separate class because some devices running 4.2- failed to
 * load classes containing references to NotificationListener
 */
public class OverlayService extends OverlayServiceCommon {
    private boolean stopNow = true;

    @Override
    public void doDismiss (boolean mStopNow) {
        if (Build.VERSION.SDK_INT >= 18) {
            stopNow = mStopNow;
            Mlog.d(logTag, packageName + tag + id);
            Intent intent = new Intent(this, NotificationListenerService.class);
            intent.setAction(NotificationListenerService.ACTION_CUSTOM);
            bindService(intent, mConnection, BIND_AUTO_CREATE);
        } else if (mStopNow) {
            stopSelf();
        } else {
            layout.setVisibility(View.GONE);
            Mlog.v(logTag, "delayStop");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopSelf();
                }
            }, 10000);
        }

    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            NotificationListenerService.LocalBinder binder = (NotificationListenerService.LocalBinder) service;
            NotificationListenerService listenerService = binder.getService();
            Mlog.d(logTag, "serviceConnected");
            listenerService.doRemove(packageName, tag, id);
            unbindService(mConnection);
            if (stopNow)
                stopSelf();
            else {
                layout.setVisibility(View.GONE);
                Mlog.v(logTag, "delayStop");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopSelf();
                    }
                }, 10000);
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
