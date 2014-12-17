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

/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import codes.simen.l50notifications.R;

public class LLandActivity extends Activity {

    private int highScore = 0;
    private boolean isFirstRun = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) actionBar.hide();
        // Sorry, this doesn't work on older versions than JellyBean
        if (Build.VERSION.SDK_INT < 16) {
            Toast.makeText(getApplicationContext(),
                    "Sorry, I couldn't get this easter egg to work on devices older than 4.1",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.lland);

        LLand world = (LLand) findViewById(R.id.world);
        world.setScoreField((TextView) findViewById(R.id.score));
        world.setSplash(findViewById(R.id.welcome));
        world.setStopListener(new LLand.StopListener() {
            @Override
            public void onStop(final int score) {
                if (score > highScore) {
                    highScore = score;
                    showHighscoreDialog(score);
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit().putInt("score", score).apply();
                }
            }
        });
        Log.v(LLand.TAG, "focus: " + world.requestFocus());

        highScore = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt("score", -1);

        if (highScore == -1) {
            Toast.makeText(getApplicationContext(), "You found the easter egg!", Toast.LENGTH_SHORT).show();
            highScore = 0;
            isFirstRun = true;
        }
    }

    private void showHighscoreDialog(final int score) {
        new AlertDialog.Builder(this)
                .setTitle("New highscore!")
                .setMessage("Submit the highscore?")
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Highscores. Please don't post fake scores. Thanks!
                        final String scoreString = new String(Base64.encode(
                                String.valueOf(score).getBytes(), Base64.NO_WRAP));
                        final String deviceString = new String(Base64.encode(
                                (Build.MODEL + "/" + Build.PRODUCT).getBytes(), Base64.NO_WRAP));

                        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(
                                "http://app.simen.codes/lollipop/?score=" + scoreString + "&hash=" + deviceString
                        )));
                    }
                })
                .setNegativeButton("Play again!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    @Override
    public void onAttachedToWindow() {
        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= 19) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFirstRun) {
            PackageManager packageManager = getPackageManager();
            final ComponentName componentName = new ComponentName(getApplicationContext(),
                    "codes.simen.l50notifications.ui.LLandActivitySys");
            packageManager.setComponentEnabledSetting(componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
        }
    }
}
