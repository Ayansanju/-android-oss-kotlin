package com.kickstarter.features.pledgedprojectsoverview.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.compose.collectAsLazyPagingItems
import com.kickstarter.features.pledgedprojectsoverview.viewmodel.PledgedProjectsOverviewViewModel
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.activities.AppThemes
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.startCreatorMessageActivity
import com.kickstarter.ui.extensions.transition
import kotlinx.coroutines.launch

class PledgedProjectsOverviewActivity : AppCompatActivity() {

    private lateinit var viewModelFactory: PledgedProjectsOverviewViewModel.Factory
    private val viewModel: PledgedProjectsOverviewViewModel by viewModels { viewModelFactory }
    private var theme = AppThemes.MATCH_SYSTEM.ordinal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            setContent {
                viewModelFactory = PledgedProjectsOverviewViewModel.Factory(env)

                theme = env.sharedPreferences()
                    ?.getInt(SharedPreferenceKey.APP_THEME, AppThemes.MATCH_SYSTEM.ordinal)
                    ?: AppThemes.MATCH_SYSTEM.ordinal

                val darkModeEnabled = this.isDarkModeEnabled(env = env)
                val lazyListState = rememberLazyListState()
                val snackbarHostState = remember { SnackbarHostState() }

                val ppoCardPagingSource = viewModel.ppoCardsState.collectAsLazyPagingItems()
                val totalAlerts = viewModel.totalAlertsState.collectAsStateWithLifecycle()

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
                        isSystemInDarkTheme()
                    } else false
                ) {
                    PledgedProjectsOverviewScreen(
                        modifier = Modifier,
                        onBackPressed = { onBackPressedDispatcher.onBackPressed() },
                        lazyColumnListState = lazyListState,
                        errorSnackBarHostState = snackbarHostState,
                        ppoCards = ppoCardPagingSource,
                        totalAlerts = totalAlerts.value,
                        onAddressConfirmed = { viewModel.showSnackbarAndRefreshCardsList() },
                        onSendMessageClick = { projectName -> viewModel.onMessageCreatorClicked(projectName) }
                    )
                }

                LaunchedEffect(Unit) {
                    viewModel.projectFlow
                        .collect {
                            startCreatorMessageActivity(it, previousScreen = MessagePreviousScreenType.PLEDGED_PROJECTS_OVERVIEW)
                        }
                }

                viewModel.provideSnackbarMessage {
                    lifecycleScope.launch {
                        snackbarHostState.showSnackbar(getString(it))
                    }
                }

                onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        finish()
                        this@PledgedProjectsOverviewActivity.transition(TransitionUtils.slideInFromLeft())
                    }
                })
            }
        }
    }
}
