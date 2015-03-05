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

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import codes.simen.l50notifications.R;

/**
 * Dark Holo theme. Useful on slow devices
 */
public class HoloLight extends ThemeClass {

    public HoloLight(ViewStub stub) {
        super(stub);
        stub.setLayoutResource(R.layout.activity_read_inner_holo_light);
    }
    public HoloLight(){super();}

    @Override
    public void setIcon(ImageView imageView, Bitmap bitmap, boolean round_icons, int color) {
        super.setIcon(imageView, bitmap, round_icons, color);
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
        if (icon != null) {
            icon.mutate().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        }
        button.setOnClickListener(clickListener);
    }

    @Override
    public ImageView getSmallIconView(LinearLayout layout) {
        return null;
    }


}
