package com.kickstarter.ui.activities.compose

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.isNotEmptyAndAtLeast6Chars
import com.kickstarter.ui.compose.designsystem.KSErrorSnackbar
import com.kickstarter.ui.compose.designsystem.KSHiddenTextInput
import com.kickstarter.ui.compose.designsystem.KSLinearProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.toolbars.compose.TopToolBar

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ChangePasswordPreview() {
    KSTheme {
        ChangePasswordScreen(
            onBackClicked = { },
            onAcceptButtonClicked = { _, _ -> },
            showProgressBar = false,
            scaffoldState = rememberScaffoldState()
        )
    }
}

@Composable
fun ChangePasswordScreen(
    onBackClicked: () -> Unit,
    onAcceptButtonClicked: (currentPass: String, newPass: String) -> Unit,
    showProgressBar: Boolean,
    scaffoldState: ScaffoldState
) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPasswordLine1 by rememberSaveable { mutableStateOf("") }
    var newPasswordLine2 by rememberSaveable { mutableStateOf("") }

    val acceptButtonEnabled = when {
        currentPassword.isNotEmptyAndAtLeast6Chars() &&
            newPasswordLine1.isNotEmptyAndAtLeast6Chars() &&
            newPasswordLine2.isNotEmptyAndAtLeast6Chars() &&
            newPasswordLine1 == newPasswordLine2 -> true

        else -> false
    }

    val warningText = when {
        newPasswordLine1.isNotEmptyAndAtLeast6Chars() &&
            newPasswordLine2.isNotEmpty() &&
            newPasswordLine2 != newPasswordLine1 -> {
            stringResource(id = R.string.Passwords_matching_message)
        }

        newPasswordLine1.isNotEmpty() && newPasswordLine1.length < 6 -> {
            stringResource(id = R.string.Password_min_length_message)
        }

        else -> ""
    }

    val focusManager = LocalFocusManager.current

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopToolBar(
                title = stringResource(id = R.string.Change_password),
                titleColor = colors.kds_support_700,
                leftOnClickAction = onBackClicked,
                leftIconColor = colors.kds_support_700,
                backgroundColor = colors.kds_white,
                right = {
                    IconButton(
                        onClick = {
                            onAcceptButtonClicked.invoke(
                                currentPassword,
                                newPasswordLine1
                            )
                        },
                        enabled = acceptButtonEnabled
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icon__check),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                color =
                                if (acceptButtonEnabled) colors.kds_create_700
                                else colors.kds_support_300
                            )
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = scaffoldState.snackbarHostState,
                snackbar = { data ->
                    KSErrorSnackbar(text = data.message)
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .background(colors.kds_support_100)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            AnimatedVisibility(visible = showProgressBar) {
                KSLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Text(
                modifier = Modifier.padding(dimensions.paddingMedium),
                text = stringResource(
                    id = R.string.Well_ask_you_to_sign_back_into_the_Kickstarter_app_once_youve_changed_your_password
                ),
                style = typography.body2,
                color = colors.kds_support_700
            )

            Column(
                Modifier
                    .background(color = colors.kds_white)
                    .padding(dimensions.paddingMedium)
                    .fillMaxWidth()
            ) {

                KSHiddenTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    onValueChanged = { currentPassword = it },
                    label = stringResource(id = R.string.Current_password),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(
                                focusDirection = FocusDirection.Down
                            )
                        }
                    )
                )

                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMedium))

                KSHiddenTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    onValueChanged = { newPasswordLine1 = it },
                    label = stringResource(id = R.string.New_password),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(
                                focusDirection = FocusDirection.Down
                            )
                        }
                    )
                )

                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMedium))

                KSHiddenTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    onValueChanged = { newPasswordLine2 = it },
                    label = stringResource(id = R.string.Confirm_password),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    )
                )
            }

            Divider(color = colors.kds_support_300)

            AnimatedVisibility(visible = warningText.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(dimensions.paddingMedium),
                    text = warningText,
                    style = typography.body2,
                    color = colors.kds_support_700
                )
            }
        }
    }
}
