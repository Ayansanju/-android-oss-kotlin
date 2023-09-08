package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rxjava2.subscribeAsState
import com.kickstarter.R
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.activities.compose.login.SetPasswordScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.viewmodels.SetPasswordViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class SetPasswordActivity : AppCompatActivity() {
    private lateinit var viewModelFactory: SetPasswordViewModel.Factory
    private val viewModel: SetPasswordViewModel.SetPasswordViewModel by viewModels { viewModelFactory }
    private var errorTitleString = R.string.general_error_oops
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var darkModeEnabled = false

        this.getEnvironment()?.let { env ->
            viewModelFactory = SetPasswordViewModel.Factory(env)
            darkModeEnabled = env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
        }

        setContent {
            var showProgressBar =
                    viewModel.outputs.progressBarIsVisible().subscribeAsState(initial = false).value

            var error = viewModel.outputs.error().subscribeAsState(initial = "").value

            var scaffoldState = rememberScaffoldState()

            when {
                error.isNotEmpty() -> {
                    LaunchedEffect(scaffoldState) {
                        scaffoldState.snackbarHostState.showSnackbar(error)
                        viewModel.resetError()
                    }
                }
            }

            KickstarterApp(useDarkTheme = if (darkModeEnabled) isSystemInDarkTheme() else false) {
                SetPasswordScreen(
                        onBackClicked = { onBackPressedDispatcher.onBackPressed() },
                        onAcceptButtonClicked = { newPassword->
                            viewModel.inputs.newPassword(newPassword)
                            viewModel.inputs.confirmPassword(newPassword)
                            viewModel.inputs.changePasswordClicked()
                        },
                        showProgressBar = showProgressBar,
                        email = viewModel.outputs.setUserEmail().subscribeAsState(initial = "").value ,
                        isFormSubmitting = viewModel.outputs.isFormSubmitting().subscribeAsState(initial = false).value,
                        scaffoldState = scaffoldState
                )
            }

        }
        viewModel.configureWith(intent)

        this.viewModel.outputs.error()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showDialog(this, getString(this.errorTitleString), it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.success()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { finish() }
            .addToDisposable(disposables)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // do nothing
            }
        })
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
