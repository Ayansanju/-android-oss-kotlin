package com.kickstarter.ui.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.setValue
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.activities.compose.login.TwoFactorScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.startDisclaimerChromeTab
import com.kickstarter.viewmodels.TwoFactorViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class TwoFactorActivity : AppCompatActivity() {
    private lateinit var viewModelFactory: TwoFactorViewModel.Factory
    private val viewModel: TwoFactorViewModel.TwoFactorViewModel by viewModels { viewModelFactory }
    private val disposables = CompositeDisposable()
    private var darkModeEnabled = false
    private var environment: Environment? = null
    private var theme = AppThemes.MATCH_SYSTEM.ordinal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            environment = env
            viewModelFactory = TwoFactorViewModel.Factory(env, intent)
            darkModeEnabled =
                env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
            theme = env.sharedPreferences()
                ?.getInt(SharedPreferenceKey.APP_THEME, AppThemes.MATCH_SYSTEM.ordinal)
                ?: AppThemes.MATCH_SYSTEM.ordinal
        }

        setContent {
            var error by remember { mutableStateOf("") }

            errorMessages().subscribe {
                error = it
            }.addToDisposable(disposables)

            var resendMessage by remember { mutableStateOf("") }

            viewModel.showResendCodeConfirmation().subscribe {
                resendMessage = getString(R.string.messages_navigation_sent)
            }.addToDisposable(disposables)

            var scaffoldState = rememberScaffoldState()

            var formSubmitting = viewModel.formSubmitting().subscribeAsState(initial = false).value

            when {
                error.isNotEmpty() -> {
                    LaunchedEffect(scaffoldState) {
                        scaffoldState.snackbarHostState.showSnackbar(message = error, actionLabel = "error")
                        error = ""
                    }
                }

                resendMessage.isNotEmpty() -> {
                    LaunchedEffect(scaffoldState) {
                        scaffoldState.snackbarHostState.showSnackbar(message = resendMessage, actionLabel = "action")
                        resendMessage = ""
                    }
                }
            }

            KickstarterApp(
                useDarkTheme =
                if (darkModeEnabled) {
                    when (theme) {
                        AppThemes.MATCH_SYSTEM.ordinal -> isSystemInDarkTheme()
                        AppThemes.DARK.ordinal -> true
                        AppThemes.LIGHT.ordinal -> false
                        else -> false
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    isSystemInDarkTheme() // Force dark mode uses system theme
                } else false
            ) {
                TwoFactorScreen(
                    scaffoldState = scaffoldState,
                    onBackClicked = { onBackPressedDispatcher.onBackPressed() },
                    onTermsOfUseClicked = { startDisclaimerScreen(DisclaimerItems.TERMS) },
                    onPrivacyPolicyClicked = { startDisclaimerScreen(DisclaimerItems.PRIVACY) },
                    onCookiePolicyClicked = { startDisclaimerScreen(DisclaimerItems.COOKIES) },
                    onHelpClicked = { startDisclaimerScreen(DisclaimerItems.HELP) },
                    onResendClicked = { resendButtonOnClick() },
                    onSubmitClicked = {
                        codeEditTextOnTextChanged(it)
                        loginButtonOnClick()
                    },
                    isLoading = formSubmitting
                )
            }
        }

        viewModel.outputs.tfaSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onSuccess() }
            .addToDisposable(disposables)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    private fun startDisclaimerScreen(disclaimerItems: DisclaimerItems) {
        startDisclaimerChromeTab(disclaimerItems, environment)
    }

    private fun errorMessages(): Observable<String> {
        return viewModel.outputs.tfaCodeMismatchError().map { getString(R.string.two_factor_error_message) }
            .mergeWith(viewModel.outputs.genericTfaError().map { getString(R.string.login_errors_unable_to_log_in) })
    }

    private fun codeEditTextOnTextChanged(code: CharSequence) {
        viewModel.inputs.code(code.toString())
    }

    private fun resendButtonOnClick() {
        viewModel.inputs.resendClick()
    }

    private fun loginButtonOnClick() {
        viewModel.inputs.loginClick()
    }

    private fun onSuccess() {
        setResult(RESULT_OK)
        finish()
    }
}
