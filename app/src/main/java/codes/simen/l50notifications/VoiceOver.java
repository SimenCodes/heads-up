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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import codes.simen.l50notifications.util.Mlog;
import codes.simen.l50notifications.util.ObjectSerializer;

/**
 * VoiceOver, like on iPods
 */
class VoiceOver {
    private final String logTag = "VoiceOver";
    private long lastId = -1;
    private Resources resources;
    private boolean broadcastNotifications = false;

    /*
    Used to keep Spotify from spamming users hours
    after they stopped listening to Spotify.
     */
    private boolean spotifyPlaying = true;

    public VoiceOver () {
    }

    public void enableVoiceOver (Context context) {
        Mlog.d(logTag, "start");
        resources = context.getResources();

        IntentFilter intentFilter = new IntentFilter();

        String filters[] = resources.getStringArray(R.array.voiceover_actions);
        for (String filter : filters) {
            intentFilter.addAction(filter);
        }

        context.registerReceiver(musicReceiver, intentFilter);

        broadcastNotifications = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("broadcast_notifications", false);
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

    private void doReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        Mlog.d(logTag, action);

        final Bundle extras = intent.getExtras();

        try {
            // Debug code for finding fields when adding app support
            /*for (String str : extras.keySet()) {
                Mlog.v(logTag + " " + str, extras.get(str));
            }*/

            boolean playing = intent.getBooleanExtra("playing", true);

            if (action.equals("com.spotify.music.playbackstatechanged")) {
                spotifyPlaying = playing;
                return;
            }

            //boolean isfavorite = intent.getBooleanExtra("isfavorite", false);

            long id = -1;
            if (action.equals("com.spotify.music.metadatachanged")) {
                if (!spotifyPlaying) return;

                // In Spotify, the ID is a String
                String idStr = intent.getStringExtra("id");
                if (idStr != null)
                    id = (long) idStr.hashCode();

                // Let's skip their ads
                int length = intent.getIntExtra("length", -1);
                if (length < 60) {
                    Mlog.d(logTag, "Too short, just " + String.valueOf(length));
                    return;
                }

            } else {
                if (!playing) return;
                id = intent.getLongExtra("id", -1);
            }

            Mlog.d(String.valueOf(id), String.valueOf(lastId));
            if (id == lastId) return;
            if (action.equals("com.android.music.metachanged") && id == -1) {
                Mlog.d(logTag, "com.android.music interfering, ignored");
                return;
            }
            if (lastId == -1) {
                lastId = id;
                Mlog.d(logTag, "last id was -1");
                return;
            }
            lastId = id;


            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");
            String track = intent.getStringExtra("track");


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

            decideIntent.putExtra("icon", R.drawable.ic_av_radio);
            context.startService(decideIntent);
            Mlog.d(logTag, "started");

            if (broadcastNotifications) {
                Mlog.d(logTag, "broadcast");
                context.sendBroadcast(
                        new Intent(DecisionMaker.ACTION_ADD)
                                .putExtras(decideIntent.getExtras())
                                .putExtra("artist", artist)
                                .putExtra("album", album)
                                .putExtra("track", track),
                        "codes.simen.permission.NOTIFICATIONS");
            }

        } catch (Exception e) {
            reportError(e, "Error receiving " + action + intent.getExtras(), context);
        }
    }


    private static void reportError(Exception e, String msg, Context c) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            if (e != null) {
                e.printStackTrace();
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                msg = msg.concat(writer.toString());
                editor.putString("lastException", ObjectSerializer.serialize(e));
            }
            editor.putString("lastBug", msg);
            editor.apply();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
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
