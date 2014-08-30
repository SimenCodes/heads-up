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
import codes.simen.l50notifications.util.RoundDrawable;

/**
 * Created by simen on 17.07.14.
 */
public class HoloLight extends ThemeClass {

    public HoloLight(ViewStub stub) {
        super(stub);
        stub.setLayoutResource(R.layout.activity_read_inner_holo_light);
    }
    public HoloLight(){super();}

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
            icon.mutate().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
            button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        }
        button.setOnClickListener(clickListener);
    }

    /*
     Set the notification icon from a bitmap.
     */
    @Override
    public void setIcon(ImageView imageView, Bitmap bitmap, boolean round_icons) {
        if (bitmap == null) return;
        if (round_icons) {
            final float width = imageView.getContext().getResources().getDimension(R.dimen.notification_ic_size);
            //Mlog.v(density, width);
            //Mlog.v(bitmap.getWidth(), width);
            if (bitmap.getWidth() >= width) {
                try {
                    RoundDrawable roundedDrawable = new RoundDrawable(bitmap);
                    imageView.setImageDrawable(roundedDrawable);
                } catch (Exception e) {
                    e.printStackTrace();
                    imageView.setImageBitmap(bitmap);
                }
            } else {
                imageView.setImageBitmap(bitmap);
            }
            imageView.setBackgroundResource(R.drawable.circle_grey);
        } else
            imageView.setImageBitmap(bitmap);
    }

    @Override
    public ImageView getSmallIconView(LinearLayout layout) {
        return null;
    }


}
