package com.kickstarter.features.pledgedprojectsoverview.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSCoralBadge
import com.kickstarter.ui.compose.designsystem.KSDividerLineGrey
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSSecondaryRedButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.compose.designsystem.shapes

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PPOCardPreview() {
    KSTheme {
        LazyColumn(
            Modifier
                .background(color = colors.backgroundSurfacePrimary),
            contentPadding = PaddingValues(dimensions.paddingMedium)
        ) {
            item {
                PPOCardView(
                    viewType = PPOCardViewType.FIX_PAYMENT,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$50.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    showBadge = true,
                    onActionButtonClicked = {},
                    timeNumberForAction = 6
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.PAYMENT_FIXED,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$50.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    showBadge = false,
                    onActionButtonClicked = {},
                    timeNumberForAction = 6
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.AUTHENTICATE_CARD,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$60.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    showBadge = true,
                    onActionButtonClicked = {},
                    timeNumberForAction = 7
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.CARD_AUTHENTICATED,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$60.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    showBadge = false,
                    onActionButtonClicked = {},
                    timeNumberForAction = 7
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.TAKE_SURVEY,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    showBadge = true,
                    onActionButtonClicked = {},
                    timeNumberForAction = 8
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.SURVEY_SUBMITTED,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    showBadge = false,
                    onActionButtonClicked = {},
                    timeNumberForAction = 8
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }
        }
    }
}

enum class PPOCardViewType {
    CONFIRM_ADDRESS,
    ADDRESS_CONFIRMED,
    FIX_PAYMENT,
    PAYMENT_FIXED,
    AUTHENTICATE_CARD,
    CARD_AUTHENTICATED,
    TAKE_SURVEY,
    SURVEY_SUBMITTED
}

@Composable
fun PPOCardView(
    viewType: PPOCardViewType,
    onCardClick: () -> Unit,
    projectName: String? = null,
    pledgeAmount: String? = null,
    imageUrl: String? = null,
    imageContentDescription: String? = null,
    creatorName: String? = null,
    sendAMessageClickAction: () -> Unit,
    showBadge: Boolean = false,
    onActionButtonClicked: () -> Unit,
    timeNumberForAction: Int = 0
) {

    BadgedBox(
        badge = { if (showBadge) Badge(backgroundColor = colors.textAccentGreen) }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = shapes.small,
            contentColor = colors.backgroundSurfacePrimary,
            backgroundColor = colors.backgroundSurfacePrimary,
            border = BorderStroke(width = dimensions.borderThickness, color = colors.borderSubtle),
        ) {
            Column(
                Modifier.clickable { onCardClick.invoke() }
            ) {
                when (viewType) {
                    PPOCardViewType.CONFIRM_ADDRESS -> {}
                    PPOCardViewType.ADDRESS_CONFIRMED -> {}
                    PPOCardViewType.FIX_PAYMENT -> FixPaymentAlertsView(timeNumberForAction)
                    PPOCardViewType.PAYMENT_FIXED -> {}
                    PPOCardViewType.AUTHENTICATE_CARD -> AuthenticateCardAlertsView(timeNumberForAction)
                    PPOCardViewType.CARD_AUTHENTICATED -> {}
                    PPOCardViewType.TAKE_SURVEY -> TakeSurveyAlertsView(timeNumberForAction)
                    PPOCardViewType.SURVEY_SUBMITTED -> SurveySubmittedAlertsView(timeNumberForAction)
                }

                ProjectPledgeSummaryView(
                    projectName = projectName,
                    pledgeAmount = pledgeAmount,
                    imageUrl = imageUrl,
                    imageContentDescription = imageContentDescription
                )

                CreatorNameSendMessageView(
                    creatorName = creatorName,
                    sendAMessageClickAction = sendAMessageClickAction
                )

                when (viewType) {
                    PPOCardViewType.CONFIRM_ADDRESS -> {}
                    PPOCardViewType.ADDRESS_CONFIRMED -> {}
                    PPOCardViewType.FIX_PAYMENT -> FixPaymentButtonView(onActionButtonClicked)
                    PPOCardViewType.PAYMENT_FIXED -> PaymentFixedButtonView { }
                    PPOCardViewType.AUTHENTICATE_CARD -> AuthenticateCardButtonView(onActionButtonClicked)
                    PPOCardViewType.CARD_AUTHENTICATED -> CardAuthenticatedButtonView { }
                    PPOCardViewType.TAKE_SURVEY -> TakeSurveyButtonView(onActionButtonClicked)
                    PPOCardViewType.SURVEY_SUBMITTED -> SurveySubmittedButtonView { }
                }
            }
        }
    }
}

@Composable
fun ProjectPledgeSummaryView(
    projectName: String? = null,
    pledgeAmount: String? = null,
    imageUrl: String? = null,
    imageContentDescription: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = dimensions.paddingMediumSmall)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = imageContentDescription,
            modifier = Modifier
                .weight(0.25f)
                .height(dimensions.clickableButtonHeight)
                .clip(shapes.small),
            placeholder = ColorPainter(color = colors.backgroundDisabled),
            contentScale = ContentScale.FillWidth
        )

        Spacer(modifier = Modifier.width(dimensions.paddingSmall))

        Column(
            modifier = Modifier
                .weight(0.75f)
                .height(dimensions.clickableButtonHeight)
        ) {
            Text(
                text = projectName ?: "",
                color = colors.textPrimary,
                style = typography.footnoteMedium,
                overflow = TextOverflow.Ellipsis,
                minLines = 2,
                maxLines = 2
            )

            Spacer(modifier = Modifier.weight(1f))

            // TODO: Replace with translated string
            Text(
                text = "$pledgeAmount pledged",
                color = colors.textSecondary,
                style = typography.caption2
            )
        }
    }
}

@Composable
fun CreatorNameSendMessageView(
    creatorName: String? = null,
    sendAMessageClickAction: () -> Unit
) {
    KSDividerLineGrey()

    Row(
        Modifier
            .fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .weight(0.7f)
                .padding(
                    top = dimensions.paddingMediumSmall,
                    bottom = dimensions.paddingMediumSmall,
                    start = dimensions.paddingMediumSmall,
                    end = dimensions.paddingSmall
                )
        ) {
            Text(
                text = stringResource(id = R.string.project_menu_created_by),
                color = colors.textSecondary,
                style = typography.caption2
            )

            Text(
                text = " ${creatorName.orEmpty()}",
                overflow = TextOverflow.Ellipsis,
                color = colors.textSecondary,
                style = typography.caption2Medium,
                maxLines = 1
            )
        }

        Row(
            modifier = Modifier
                .weight(0.3f)
                .padding(
                    end = dimensions.paddingMediumSmall,
                    top = dimensions.paddingMediumSmall,
                    bottom = dimensions.paddingMediumSmall
                )
                .clickable { sendAMessageClickAction.invoke() }
        ) {
            // TODO: Replace with translated string
            Text(
                text = "Send a message",
                color = colors.textAccentGreen,
                style = typography.caption2
            )

            Image(
                modifier = Modifier.size(dimensions.paddingMediumSmall),
                imageVector = ImageVector.vectorResource(id = R.drawable.chevron_right),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = colors.textAccentGreen)
            )
        }
    }

    KSDividerLineGrey()
}

@Composable
fun FixPaymentAlertsView(daysRemaining: Int = -1) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensions.paddingMediumSmall, start = dimensions.paddingMediumSmall)
    ) {
        KSCoralBadge(
            leadingIcon = {
                // TODO: Replace with translated string
                Image(
                    modifier = Modifier.padding(end = dimensions.paddingXSmall),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_icon_alert),
                    contentDescription = "Payment Failed",
                    colorFilter = ColorFilter.tint(colors.textAccentRedBold)
                )
            },
            // TODO: Replace with translated string
            text = "Payment failed",
            textColor = colors.textAccentRedBold
        )

        if (daysRemaining > 0) {
            Spacer(modifier = Modifier.height(dimensions.paddingSmall))

            KSCoralBadge(
                leadingIcon = {
                    // TODO: Replace with translated string
                    Image(
                        modifier = Modifier
                            .padding(end = dimensions.paddingXSmall)
                            .size(dimensions.alertIconSize),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_clock),
                        contentDescription = "Pledge will be dropped in $daysRemaining days",
                        colorFilter = ColorFilter.tint(colors.textAccentRedBold)
                    )
                },
                // TODO: Replace with translated string
                text = "Pledge will be dropped in $daysRemaining days",
                textColor = colors.textAccentRedBold
            )
        }
    }
}

@Composable
fun FixPaymentButtonView(onFixPaymentClicked: () -> Unit) {
    // TODO: Replace with translated string
    KSSecondaryRedButton(
        modifier = Modifier.padding(dimensions.paddingMediumSmall),
        onClickAction = { onFixPaymentClicked.invoke() },
        text = "Fix Payment",
        isEnabled = true,
        textStyle = typography.buttonText
    )
}

@Composable
fun AuthenticateCardAlertsView(daysRemaining: Int = -1) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensions.paddingMediumSmall, start = dimensions.paddingMediumSmall)
    ) {
        KSCoralBadge(
            leadingIcon = {
                // TODO: Replace with translated string
                Image(
                    modifier = Modifier.padding(end = dimensions.paddingXSmall),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_icon_alert),
                    contentDescription = "Card needs authentication",
                    colorFilter = ColorFilter.tint(colors.textAccentRedBold)
                )
            },
            // TODO: Replace with translated string
            text = "Card needs authentication",
            textColor = colors.textAccentRedBold
        )

        if (daysRemaining > 0) {
            Spacer(modifier = Modifier.height(dimensions.paddingSmall))

            KSCoralBadge(
                leadingIcon = {
                    // TODO: Replace with translated string
                    Image(
                        modifier = Modifier
                            .padding(end = dimensions.paddingXSmall)
                            .size(dimensions.alertIconSize),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_clock),
                        contentDescription = "Pledge will be dropped in $daysRemaining days",
                        colorFilter = ColorFilter.tint(colors.textAccentRedBold)
                    )
                },
                // TODO: Replace with translated string
                text = "Pledge will be dropped in $daysRemaining days",
                textColor = colors.textAccentRedBold
            )
        }
    }
}

@Composable
fun PaymentFixedButtonView(onFixPaymentClicked: () -> Unit) {
    // TODO: Replace with translated string
    KSSecondaryRedButton(
        modifier = Modifier.padding(dimensions.paddingMediumSmall),
        leadingIcon = {
            Image(
                modifier = Modifier
                    .padding(end = dimensions.paddingXSmall)
                    .size(dimensions.paddingMedium),
                imageVector = ImageVector.vectorResource(id = R.drawable.icon__check),
                contentDescription = "Payment Fixed",
                colorFilter = ColorFilter.tint(color = colors.textSecondary)
            )
        },
        onClickAction = { onFixPaymentClicked.invoke() },
        text = "Payment Fixed",
        isEnabled = false,
        textStyle = typography.buttonText
    )
}

@Composable
fun AuthenticateCardButtonView(onAuthenticateCardClicked: () -> Unit) {
    // TODO: Replace with translated string
    KSSecondaryRedButton(
        modifier = Modifier.padding(dimensions.paddingMediumSmall),
        onClickAction = { onAuthenticateCardClicked.invoke() },
        text = "Authenticate Card",
        isEnabled = true,
        textStyle = typography.buttonText
    )
}

@Composable
fun CardAuthenticatedButtonView(onFixPaymentClicked: () -> Unit) {
    // TODO: Replace with translated string
    KSSecondaryRedButton(
        modifier = Modifier.padding(dimensions.paddingMediumSmall),
        leadingIcon = {
            Image(
                modifier = Modifier
                    .padding(end = dimensions.paddingXSmall)
                    .size(dimensions.paddingMedium),
                imageVector = ImageVector.vectorResource(id = R.drawable.icon__check),
                contentDescription = "Card Authenticated",
                colorFilter = ColorFilter.tint(color = colors.textSecondary)
            )
        },
        onClickAction = { onFixPaymentClicked.invoke() },
        text = "Card Authenticated",
        isEnabled = false,
        textStyle = typography.buttonText
    )
}

@Composable
fun TakeSurveyAlertsView(hoursRemaining: Int = -1) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensions.paddingMediumSmall, start = dimensions.paddingMediumSmall)
    ) {
        KSCoralBadge(
            leadingIcon = {
                // TODO: Replace with translated string
                Image(
                    modifier = Modifier.padding(end = dimensions.paddingXSmall),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_icon_alert),
                    contentDescription = "Complete Survey",
                    colorFilter = ColorFilter.tint(colors.textSecondary)
                )
            },
            // TODO: Replace with translated string
            text = "Complete Survey",
        )

        if (hoursRemaining > 0) {
            Spacer(modifier = Modifier.height(dimensions.paddingSmall))

            KSCoralBadge(
                leadingIcon = {
                    // TODO: Replace with translated string
                    Image(
                        modifier = Modifier
                            .padding(end = dimensions.paddingXSmall)
                            .size(dimensions.alertIconSize),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_clock),
                        contentDescription = "Address locks in $hoursRemaining hours",
                        colorFilter = ColorFilter.tint(colors.textSecondary)
                    )
                },
                // TODO: Replace with translated string
                text = "Address locks in $hoursRemaining hours",
            )
        }
    }
}

@Composable
fun TakeSurveyButtonView(onAuthenticateCardClicked: () -> Unit) {
    // TODO: Replace with translated string
    KSPrimaryGreenButton(
        modifier = Modifier.padding(dimensions.paddingMediumSmall),
        onClickAction = { onAuthenticateCardClicked.invoke() },
        text = "Take Survey",
        isEnabled = true,
        textStyle = typography.buttonText
    )
}

@Composable
fun SurveySubmittedAlertsView(hoursRemaining: Int = -1) {
    if (hoursRemaining > 0) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensions.paddingMediumSmall, start = dimensions.paddingMediumSmall)
        ) {
            Spacer(modifier = Modifier.height(dimensions.paddingSmall))

            KSCoralBadge(
                leadingIcon = {
                    // TODO: Replace with translated string
                    Image(
                        modifier = Modifier
                            .padding(end = dimensions.paddingXSmall)
                            .size(dimensions.alertIconSize),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_clock),
                        contentDescription = "Address locks in $hoursRemaining hours",
                        colorFilter = ColorFilter.tint(colors.textSecondary)
                    )
                },
                // TODO: Replace with translated string
                text = "Address locks in $hoursRemaining hours",
            )
        }
    }
}

@Composable
fun SurveySubmittedButtonView(onFixPaymentClicked: () -> Unit) {
    // TODO: Replace with translated string
    KSPrimaryGreenButton(
        modifier = Modifier.padding(dimensions.paddingMediumSmall),
        leadingIcon = {
            Image(
                modifier = Modifier
                    .padding(end = dimensions.paddingXSmall)
                    .size(dimensions.paddingMedium),
                imageVector = ImageVector.vectorResource(id = R.drawable.icon__check),
                contentDescription = "Survey Submitted",
                colorFilter = ColorFilter.tint(color = colors.textSecondary)
            )
        },
        onClickAction = { onFixPaymentClicked.invoke() },
        text = "Survey Submitted",
        isEnabled = false,
        textStyle = typography.buttonText
    )
}
