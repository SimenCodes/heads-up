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
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import codes.simen.l50notifications.R;
import codes.simen.l50notifications.util.Mlog;

/**
 * A random, psychedelic theme. I don't know why anyone would use this, but it's here.
 * Consider it a weird easter egg.
 */
public class Random extends HoloDark {
    ThemeClass subTheme;

    public Random(ViewStub stub) {
        super(stub);
        stub.setLayoutResource(R.layout.activity_read_inner_holo);
    }

    @Override
    public void init(LinearLayout layout) {
        LinearLayout linearLayout = (LinearLayout) layout.findViewById(R.id.contentContainer);

        final int red = randomColorValue();
        final int green = randomColorValue();
        final int blue = randomColorValue();
        final double brightness = (0.299*red + 0.587*green + 0.114*blue);

        Mlog.v(red+blue+green, brightness);
        linearLayout.setBackgroundColor(
                Color.rgb(red, green, blue)
        );

        if (brightness > 130) {
            subTheme = new HoloLight();
            ((TextView)linearLayout.findViewById(R.id.notification_title)).setTextColor(Color.BLACK);
            ((TextView)linearLayout.findViewById(R.id.notification_subtitle)).setTextColor(Color.BLACK);
        }
        else {
            subTheme = new HoloDark();
        }
    }

    private int randomColorValue() {
        return (int) (Math.random() * 200);
    }

    @Override
    public void addActionButton(ViewGroup actionButtons, String actionTitle, Drawable icon, View.OnClickListener clickListener, float fontMultiplier) {
        subTheme.addActionButton(actionButtons, actionTitle, icon, clickListener, fontMultiplier);
    }

    @Override
    public ImageView getSmallIconView(LinearLayout layout) {
        return null;
    }

}
