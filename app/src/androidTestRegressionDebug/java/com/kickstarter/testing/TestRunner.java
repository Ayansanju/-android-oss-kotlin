package com.kickstarter.testing;

import androidx.test.runner.AndroidJUnitRunner;
import androidx.multidex.MultiDex;
import com.facebook.testing.screenshot.ScreenshotRunner;
import android.os.Bundle;

public class TestRunner extends AndroidJUnitRunner {
    @Override
    public void onCreate(Bundle arguments) {
        MultiDex.install(getTargetContext());
        ScreenshotRunner.onCreate(this, arguments);
        super.onCreate(arguments);
    }

    @Override
    public void finish(int resultCode, Bundle results) {
        ScreenshotRunner.onDestroy();
        super.finish(resultCode, results);
    }
}
