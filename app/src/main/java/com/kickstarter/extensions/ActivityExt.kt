package com.kickstarter.extensions

import android.app.Activity
import android.content.Intent
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.kickstarter.R

fun Activity.startActivityWithSlideUpTransition(intent: Intent) {
    this.startActivity(intent)
    this.overridePendingTransition(R.anim.settings_slide_in_from_bottom, R.anim.settings_slide_out_from_top)
}

fun Activity.startActivityWithSlideLeftTransition(intent: Intent) {
    this.startActivity(intent)
    this.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}

fun Activity.showConfirmationSnackbar(anchor: View, stringResId: Int) {
    showConfirmationSnackbar(anchor, getString(stringResId))
}

fun showConfirmationSnackbar(anchor: View, message: String) {
    Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).confirmation().show()
}

fun Activity.showErrorSnackbar(anchor: View, stringResId: Int) {
    showErrorSnackbar(anchor, getString(stringResId))
}

fun showErrorSnackbar(anchor: View, message: String) {
    Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).error().show()
}

fun Activity.showHeadsUpSnackbar(anchor: View, stringResId: Int) {
    showHeadsUpSnackbar(anchor, getString(stringResId))
}

fun showHeadsUpSnackbar(anchor: View, message: String) {
    Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).headsUp().show()
}

fun Activity.showNetworkErrorSnackbar(anchor: View, stringResId: Int) {
    showErrorSnackbar(anchor, getString(stringResId))
}

fun showNetworkErrorSnackbar(anchor: View, message: String) {
    Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).networkError().show()
}
