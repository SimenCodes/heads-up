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

package codes.simen.l50notifications.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;


/**
 * Takes care of sending SMS messages from Nigel
 */
public class SmsSender extends Service {
    private static final String logTag = "SmsSender";

    private static final String EXTRA_PHONE_NUMBER = "phone_number";
    private static final String EXTRA_MESSAGE_CONTENT = "message_content";
    private static final String ACTION_MESSAGE = "codes.simen.l50notifications.ACTION_MESSAGE";

    /**
     * Send an SMS message
     * @param context Application context
     * @param recipient Where should we send this message?
     * @param text The message body
     */
    public static void sendSmsWithService(Context context, String recipient, String text) {
        Mlog.d(logTag, "Message to " + recipient + ": " + text);
        context.startService(new Intent(context, SmsSender.class)
                        .setAction(ACTION_MESSAGE)
                        .putExtra(EXTRA_PHONE_NUMBER, recipient)
                        .putExtra(EXTRA_MESSAGE_CONTENT, text)
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null && ACTION_MESSAGE.equals(action)) {
                final String number = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                final String content = intent.getStringExtra(EXTRA_MESSAGE_CONTENT);
                sendSms(getApplicationContext(), number, content);
            }
        }
        return START_NOT_STICKY;
    }

    /**
     * Send an SMS message
     * @param context Application context
     * @param recipient Where should we send this message?
     * @param text The message body
     */
    private void sendSms(Context context, String recipient, String text) {
        if (!text.isEmpty()) {
            try {
                SmsManager sms = SmsManager.getDefault();
                final PendingIntent service = PendingIntent.getService(context, 0,
                        new Intent(context, SmsSender.class)
                                .putExtra(EXTRA_PHONE_NUMBER, recipient)
                                .putExtra(EXTRA_MESSAGE_CONTENT, text),
                        PendingIntent.FLAG_UPDATE_CURRENT);

                context.registerReceiver(sentReceiver, new IntentFilter("SMS_SENT"));

                sms.sendTextMessage(recipient, null, text, service, null);
            } catch (Exception e) {
                onSendFailed(context, recipient, text, null);
            }

            if (Build.VERSION.SDK_INT < 19) {
                ContentValues v = new ContentValues();
                v.put("address", recipient);
                v.put("body", text);

                try {
                    context.getContentResolver().insert(Uri.parse("content://sms/sent"), v);
                } catch (Exception e) {
                    Toast.makeText(context, "Message not written to Sent section", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * If the message failed to send for some reason
     * @param context Application context
     * @param recipient Where should we send this message?
     * @param text The message body
     * @param reason Why the message failed to send. May be null
     */
    private static void onSendFailed(Context context, String recipient, String text, String reason) {
        if (reason == null) reason = "Automatic sending failed";
        Toast.makeText(context, reason, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("vnd.android-dir/mms-sms");
        intent.putExtra("address", recipient);
        intent.putExtra("sms_body", text);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private BroadcastReceiver sentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int resultCode = getResultCode();
            handleError(context, intent, resultCode);
            unregisterReceiver(sentReceiver);
            stopSelf();
        }

        private void handleError(Context context, Intent intent, int resultCode) {
            String error = "";
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS sent", Toast.LENGTH_SHORT).show();
                    return;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    error = "RESULT_ERROR_GENERIC_FAILURE"; break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    error = "RESULT_ERROR_NO_SERVICE"; break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    error = "RESULT_ERROR_NULL_PDU"; break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    error = "RESULT_ERROR_RADIO_OFF"; break;
            }
            onSendFailed(context,
                    intent.getStringExtra(EXTRA_PHONE_NUMBER),
                    intent.getStringExtra(EXTRA_MESSAGE_CONTENT),
                    error);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
