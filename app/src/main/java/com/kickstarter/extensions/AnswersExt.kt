package com.kickstarter.extensions

import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent

fun logCustomEvent(customEvent: String) {
    Answers.getInstance().logCustom(CustomEvent(customEvent))
}

fun logCustomEventWithAttributes(eventName: String, keyValue: String, customAttribute: String) {
    Answers.getInstance().logCustom(CustomEvent(eventName)
            .putCustomAttribute(keyValue, customAttribute))
}