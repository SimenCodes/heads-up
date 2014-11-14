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

package codes.simen.l50notifications.theme;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;

import codes.simen.l50notifications.R;

/**
 * Created by simen on 17.07.14.
 */
public class L5Light extends ThemeClass {

    public L5Light (ViewStub stub) {
        stub.setLayoutResource(R.layout.activity_read_inner);
    }

    @Override
    public void addActionButton(ViewGroup actionButtons, String actionTitle, Drawable icon, View.OnClickListener clickListener, float fontMultiplier) {
        LayoutInflater inflater = LayoutInflater.from(actionButtons.getContext());
        ViewGroup v = (ViewGroup) inflater.inflate(
                R.layout.button_notification, actionButtons);

        Button button = (Button) v.getChildAt(v.getChildCount() - 1);
        button.setText(actionTitle);
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontMultiplier * button.getTextSize());
        button.setTextColor(Color.BLACK);
        //Typeface typeface = Typeface.createFromAsset(actionButtons.getContext().getAssets(), "RobotoCondensed-Regular.ttf");
        //button.setTypeface(typeface);
        if (icon != null) {
            icon.mutate().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
            button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        }
        button.setOnClickListener(clickListener);
    }

}
