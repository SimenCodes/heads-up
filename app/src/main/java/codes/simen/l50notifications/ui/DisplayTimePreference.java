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

import codes.simen.l50notifications.OverlayService;
import codes.simen.l50notifications.R;

import static android.widget.SeekBar.OnSeekBarChangeListener;

public class DisplayTimePreference extends DialogPreference {
    private final static int mMax = OverlayService.MAX_DISPLAY_TIME;
    private final int mDefault;

    private SeekBar mSeekBar;
    private static TextView mTextView;
    private static Resources resources;

    public DisplayTimePreference(Context context, AttributeSet attrs) {
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
        mSeekBar.setMax(mMax);
        mSeekBar.setProgress(value);
        if (value == mMax)
            mTextView.setText(resources.getString(R.string.pref_overlay_time_max));
        else
            mTextView.setText(resources.getString(R.string.pref_overlay_display_time_counter, String.valueOf(value / 1000) ));


        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            persistInt(mSeekBar.getProgress());
            if (mSeekBar.getProgress() == mMax)
                persistInt(mMax);
        }
    }

    private static final OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (i == mMax)
                mTextView.setText(resources.getString(R.string.pref_overlay_time_max));
            else
                mTextView.setText(resources.getString(R.string.pref_overlay_display_time_counter, String.valueOf(i / 1000) ));
        }
        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
    };

}