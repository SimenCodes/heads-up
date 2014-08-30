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

import codes.simen.l50notifications.R;

/**
 * Created by simen on 17.07.14.
 */
public class L5Light extends ThemeClass {

    public L5Light (ViewStub stub) {
        super(stub);
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

    @Override
    public void setIcon(ImageView imageView, Bitmap bitmap, boolean round_icons) {
        super.setIcon(imageView, bitmap, round_icons);
        if (round_icons) {
            imageView.setBackgroundResource(R.drawable.circle_grey);
        }
    }


}
