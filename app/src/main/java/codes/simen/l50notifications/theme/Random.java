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
 * Created by simen on 17.07.14.
 */
public class Random extends HoloDark {
    ThemeClass subTheme;

    public Random(ViewStub stub) {
        super(stub);
        stub.setLayoutResource(R.layout.activity_read_inner_holo);
    }

    @Override
    public void init(LinearLayout layout) {
        LinearLayout linearLayout = (LinearLayout) layout.findViewById(R.id.linearLayout);

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
