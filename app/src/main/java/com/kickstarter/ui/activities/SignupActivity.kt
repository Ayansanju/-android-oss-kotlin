package com.kickstarter.ui.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rxjava2.subscribeAsState
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.activities.compose.login.SignupScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.startDisclaimerChromeTab
import com.kickstarter.viewmodels.SignupViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class SignupActivity : AppCompatActivity() {

    private lateinit var viewModelFactory: SignupViewModel.Factory
    private val viewModel: SignupViewModel.SignupViewModel by viewModels { viewModelFactory }
    private val disposables = CompositeDisposable()
    var darkModeEnabled = false
    private var environment: Environment? = null
    private var theme = AppThemes.MATCH_SYSTEM.ordinal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            environment = env
            viewModelFactory = SignupViewModel.Factory(env)
            darkModeEnabled = env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
            theme = env.sharedPreferences()
                ?.getInt(SharedPreferenceKey.APP_THEME, AppThemes.MATCH_SYSTEM.ordinal)
                ?: AppThemes.MATCH_SYSTEM.ordinal
        }

        setContent {
            var scaffoldState = rememberScaffoldState()

            var showProgressBar =
                viewModel.outputs.progressBarIsVisible().subscribeAsState(initial = false).value

            var error = viewModel.outputs.errorString().subscribeAsState(initial = "").value

            when {
                error.isNotEmpty() -> {
                    LaunchedEffect(scaffoldState) {
                        scaffoldState.snackbarHostState.showSnackbar(error)
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
                SignupScreen(
                    onBackClicked = { onBackPressedDispatcher.onBackPressed() },
                    onSignupButtonClicked = { name, email, password, sendNewsletters ->
                        viewModel.inputs.name(name)
                        viewModel.inputs.email(email)
                        viewModel.inputs.password(password)
                        viewModel.inputs.sendNewsletters(sendNewsletters)
                        viewModel.inputs.signupClick()
                    },
                    showProgressBar = showProgressBar,
                    isFormSubmitting = viewModel.outputs.formSubmitting().subscribeAsState(initial = false).value,
                    onTermsOfUseClicked = { startDisclaimerScreen(DisclaimerItems.TERMS) },
                    onPrivacyPolicyClicked = { startDisclaimerScreen(DisclaimerItems.PRIVACY) },
                    onCookiePolicyClicked = { startDisclaimerScreen(DisclaimerItems.COOKIES) },
                    onHelpClicked = { startDisclaimerScreen(DisclaimerItems.HELP) },
                    scaffoldState = scaffoldState
                )
            }
        }

        viewModel.outputs.signupSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onSuccess() }
            .addToDisposable(disposables)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    private fun onSuccess() {
        setResult(RESULT_OK)
        finish()
    }

    private fun startDisclaimerScreen(disclaimerItems: DisclaimerItems) {
        startDisclaimerChromeTab(disclaimerItems, environment)
    }
}
