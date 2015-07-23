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

package codes.simen.l50notifications.ui;

import android.content.Context;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.util.Date;

public class ClockPreference extends DialogPreference {

    private TimePicker mTimePicker;

    public ClockPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        int defaultValue = getKey().equals("night_mode_start") ? 1320 : 420;
        int value = getPersistedInt(defaultValue);

        mTimePicker = new TimePicker(getContext());

        mTimePicker.setCurrentHour((int) Math.floor(value / 60));
        mTimePicker.setCurrentMinute((int) Math.floor(value % 60));
        mTimePicker.setIs24HourView(DateFormat.is24HourFormat(getContext().getApplicationContext()));

        return mTimePicker;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int hour = mTimePicker.getCurrentHour();
            int minute = mTimePicker.getCurrentMinute();
            persistInt( (hour * 60) + minute);
            // GregorianCalendar crashes with "bad class" on M, so we'll use the old Date.
            // The old thing probably works just fine for this stuff anyway.
            //noinspection deprecation
            setSummary(DateFormat.getTimeFormat(getContext().getApplicationContext())
                    .format(new Date(0, 0, 0, hour, minute)));
        }
    }

}