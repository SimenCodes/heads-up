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

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package codes.simen.l50notifications;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;

import java.io.InputStream;

import codes.simen.l50notifications.util.Mlog;

public class SmsMessageReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "SmsMessageReceiver";
    private Bitmap contactImg;
    private int contactId = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        Mlog.i(LOG_TAG, "onReceive");
        if (extras == null)
            return;

        Object[] pdus = (Object[]) extras.get("pdus");
        String from = "";
        String fullmessage = "";
        for (Object pdu : pdus) {
            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
            String fromAddress = message.getOriginatingAddress();
            String messageBody = message.getMessageBody();

            Mlog.i(LOG_TAG, "From: " + fromAddress + " message: " + messageBody);
            fullmessage = fullmessage + messageBody;
            from = fromAddress;

        }
	    String name = getContact(context, from);
        //addNotification(context, from, name, fullmessage);
        displayPopup(context, from, name, fullmessage);
    }

    private void displayPopup(Context context, String from, String name, String message) {
        Intent intent = new Intent(context, OverlayService.class);
        if (Build.VERSION.SDK_INT >= 18) intent.setClass(context, OverlayService.class);
        else                             intent.setClass(context, OverlayServiceCommon.class);

        intent.setAction("SMSFORREPLY");

        intent.putExtra("title", name);
        intent.putExtra("number", from);
        intent.putExtra("text", message);

        intent.putExtra("tag", "SMSFORREPLY");

        context.startService(intent);
    }

    private String getContact(Context context, String number) {
    	String name;
    	InputStream input;

    	String[] projection = new String[] {
    	        ContactsContract.PhoneLookup.DISPLAY_NAME,
    	        ContactsContract.PhoneLookup._ID};

    	// encode the phone number and build filter URI
    	Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

    	Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

    	if (cursor.moveToFirst()) {
    	    // Get values
    	    contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
    	    name =      cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));

    	    // Get photo as input stream:
    	    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
    	    input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);

    	    Mlog.v(LOG_TAG, "Contact Found @ " + number);
    	    Mlog.v(LOG_TAG, "Contact name  = " + name);
    	    Mlog.v(LOG_TAG, "Contact id    = " + contactId);
    	    
        	// Decode photo and store in variable if it was found
        	if (input != null) {
        	    contactImg = BitmapFactory.decodeStream(input);
        	    Mlog.v(LOG_TAG, "Photo found, id = " + contactId + " name = " + name);
        	}
    	    return name;
    	} else {

    	    Mlog.v(LOG_TAG, "Contact Not Found @ " + number);
    	    return number; // contact not found

    	}
    }
}
