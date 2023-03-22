package com.kickstarter.libs

import android.content.SharedPreferences
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.ui.SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE

interface FirebaseAnalyticsClientType {
    fun isEnabled(): Boolean

    fun trackEvent(eventName: String, parameters: Bundle)
}

open class FirebaseAnalyticsClient(
    private var optimizely: ExperimentsClientType,
    private var preference: SharedPreferences,
    private val firebaseAnalytics: FirebaseAnalytics?,
) : FirebaseAnalyticsClientType {

    override fun isEnabled() = preference.getBoolean(CONSENT_MANAGEMENT_PREFERENCE, false) && optimizely.isFeatureEnabled(OptimizelyFeature.Key.ANDROID_GOOGLE_ANALYTICS)

    override fun trackEvent(eventName: String, parameters: Bundle) {
        firebaseAnalytics?.let {
            if (isEnabled()) {
                firebaseAnalytics.logEvent(eventName, parameters)
            }
        }
    }
}
