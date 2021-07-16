package com.kickstarter.screenshoot.testing

import android.app.Application
import android.content.Context
import com.karumi.shot.ShotTestRunner

class KSScreenShotTestRunner : ShotTestRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return InstrumentedApp()
    }
}
