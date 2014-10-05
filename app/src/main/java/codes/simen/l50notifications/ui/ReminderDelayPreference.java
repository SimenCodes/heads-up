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
 * Copyright (C) 2011 The CyanogenMod Project
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

package codes.simen.l50notifications.ui;

import android.content.Context;
import android.content.res.Resources;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import codes.simen.l50notifications.OverlayServiceCommon;
import codes.simen.l50notifications.R;

import static android.widget.SeekBar.OnSeekBarChangeListener;

public class ReminderDelayPreference extends DialogPreference {
    private final static int mMin = OverlayServiceCommon.MIN_REMINDER_TIME;
    private final static int mMax = OverlayServiceCommon.MAX_REMINDER_TIME;
    private final int mDefault;

    private SeekBar mSeekBar;
    private static TextView mTextView;
    private static Resources resources;

    public ReminderDelayPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        resources = context.getResources();

        mDefault = 15000;
    }

    @Override
    protected View onCreateDialogView() {
        int value = getPersistedInt(mDefault);

        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.number_picker_dialog, null);

        mTextView = (TextView) view.findViewById(R.id.textView);
        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        // Initialize state
        mSeekBar.setMax(mMax - mMin);
        mSeekBar.setProgress(value - mMin);

        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            persistInt(mSeekBar.getProgress() + mMin);
        }
    }

    private static final OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            mTextView.setText(resources.getString(R.string.pref_reminder_delay_counter, String.valueOf( (i+mMin) / 60000 )));
        }
        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
    };

}