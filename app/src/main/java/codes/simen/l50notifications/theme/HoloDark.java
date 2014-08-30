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
import android.widget.ImageView;
import android.widget.LinearLayout;

import codes.simen.l50notifications.R;

/**
 * Created by simen on 17.07.14.
 */
public class HoloDark extends ThemeClass {

    public HoloDark(ViewStub stub) {
        super(stub);
        stub.setLayoutResource(R.layout.activity_read_inner_holo);
    }
    public HoloDark(){super();}

    @Override
    public void addActionButton(ViewGroup actionButtons, String actionTitle, Drawable icon, View.OnClickListener clickListener, float fontMultiplier) {

        LayoutInflater inflater = LayoutInflater.from(actionButtons.getContext());
        ViewGroup v = (ViewGroup) inflater.inflate(
                R.layout.button_notification, actionButtons);

        Button button = (Button) v.getChildAt(v.getChildCount() - 1);
        button.setText(actionTitle);
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontMultiplier * button.getTextSize());
        button.setTextColor(Color.WHITE);
        if (icon != null) {
            icon.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        }
        button.setOnClickListener(clickListener);
    }

    @Override
    public ImageView getSmallIconView(LinearLayout layout) {
        return null;
    }

}
