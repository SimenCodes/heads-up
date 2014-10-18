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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;

import codes.simen.l50notifications.util.Mlog;

/**
 * VoiceOver, like on iPods
 */
public class VoiceOver {
    private final String logTag = "VoiceOver";
    private long lastId = -1;
    private Resources resources;
    private Context context;

    public VoiceOver () {
    }

    public void enableVoiceOver (Context mContext) {
        Mlog.d(logTag, "start");
        context = mContext;
        resources = context.getResources();

        IntentFilter intentFilter = new IntentFilter();

        String filters[] = resources.getStringArray(R.array.voiceover_actions);
        for (String filter : filters) {
            intentFilter.addAction(filter);
        }

        context.registerReceiver(musicReceiver, intentFilter);

    }

    public void disableVoiceOver (Context context) {
        context.unregisterReceiver(musicReceiver);
    }

    private final BroadcastReceiver musicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doReceive(context, intent);
        }
    };

    private boolean doReceive (final Context context, final Intent intent) {
        String action = intent.getAction();
        if (action == null) return false;
        Mlog.d(logTag, action);

        boolean playing = intent.getBooleanExtra("playing", true);
        //boolean isfavorite = intent.getBooleanExtra("isfavorite", false);

        if (!playing) return false;

        long id = -1;
        if ( action.equals("com.spotify.music.metadatachanged") ) {
            // TODO: Fix problem with Spotify sending metachanged when not playing music
            // In Spotify, the ID is a String
            String idStr = intent.getStringExtra("id");
            if (idStr != null)
                id = (long) idStr.hashCode();

            // Let's not read their ads
            int length = intent.getIntExtra("length", -1);
            if (length < 60) {
                Mlog.d(logTag, "Too short, just " + String.valueOf(length));
                return false;
            }

        } else {
            id = intent.getLongExtra("id", -1);
        }

        Mlog.d(String.valueOf(id), String.valueOf(lastId));
        if (id == lastId) return false;
        if (action.equals("com.android.music.metachanged") && id == -1) {
            Mlog.d(logTag, "com.android.music interfering, ignored");
            return false; // This app keeps interfering
        }
        if (lastId == -1) {
            lastId = id;
            Mlog.d(logTag, "last id was -1");
            return false;
        }
        lastId = id;


        String artist = intent.getStringExtra("artist");
        String album = intent.getStringExtra("album");
        String track = intent.getStringExtra("track");

        // Debug code for finding fields when adding app support
        /*for (String str : intent.getExtras().keySet()) {
            Mlog.v(logTag + " " + action, str);
        }*/


        Intent decideIntent = new Intent();
        decideIntent.setClass(context, OverlayServiceCommon.class);
        decideIntent.setAction("add");
        decideIntent.putExtra("packageName", "codes.simen.voiceover");
        decideIntent.putExtra("title", resources.getString(
                R.string.music_title, track
        ));
        decideIntent.putExtra("text", resources.getString(
                R.string.music_text, artist, album
        ));

        decideIntent.setPackage(intent.getPackage());
        decideIntent.setAction(Intent.ACTION_VIEW);

        decideIntent.putExtra("icon", R.drawable.abc_ic_voice_search);
        context.startService(decideIntent);
        Mlog.d(logTag, "started");

        return true;
    }

    /*
    TODO: Doesn't work, kept in case of bright ideas
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            NotificationListenerService.LocalBinder binder = (NotificationListenerService.LocalBinder) service;
            NotificationListenerService listenerService = binder.getService();

            Mlog.d(logTag, "serviceConnected");
            listenerService.pushMusicNotification(lastAction);
            context.unbindService(mConnection);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Mlog.d(logTag, "serviceDisconnected");
        }
    };*/
}
