package codes.simen.l50notifications.ui;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

import codes.simen.l50notifications.R;

public class SetLocaleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_locale);
    }

    public void setLocale(View view) {
        if (Build.VERSION.SDK_INT >= 17) {
            Resources resources = getResources();
            Configuration resourcesConfiguration = resources.getConfiguration();
            resourcesConfiguration.setLocale(new Locale(
                    ((EditText) findViewById(R.id.editText)).getText().toString()
            ));
            resources.updateConfiguration(resourcesConfiguration, resources.getDisplayMetrics());
        } else {
            Toast.makeText(getApplicationContext(), "Sorry, not supported on your device (Android <= 4.2)", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}