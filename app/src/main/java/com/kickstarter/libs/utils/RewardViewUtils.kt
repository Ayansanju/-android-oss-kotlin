package com.kickstarter.libs.utils

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Pair
import androidx.annotation.StringRes
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.models.Country
import com.kickstarter.libs.utils.extensions.isBacked
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.trimAllWhitespace
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import java.math.RoundingMode

object RewardViewUtils {

    /**
     * Returns the string resource ID of the rewards button based on project and reward status.
     */
    @StringRes
    fun pledgeButtonText(project: Project, reward: Reward): Int {
        val backing = project.backing()
        val hasAddOnsSelected = backing?.addOns()?.isNotEmpty() ?: false

        if ((backing == null || !backing.isBacked(reward)) && RewardUtils.isAvailable(project, reward)) {
            return R.string.Select
        }

        return if (backing != null && backing.isBacked(reward)) {
            when {
                !reward.hasAddons() -> R.string.Selected
                reward.hasAddons() || hasAddOnsSelected -> R.string.Continue
                else -> R.string.No_longer_available
            }
        } else {
            R.string.No_longer_available
        }
    }

    /**
     * Returns the shipping summary for a reward.
     */
    fun shippingSummary(context: Context, ksString: KSString, stringResAndLocationName: Pair<Int, String?>): String {
        val stringRes = stringResAndLocationName.first
        val locationName = stringResAndLocationName.second
        val shippingSummary = context.getString(stringRes)

        return when (stringRes) {
            R.string.location_name_only -> when (locationName) {
                null -> context.getString(R.string.Limited_shipping)
                else -> ksString.format(shippingSummary, "location_name", locationName)
            }
            else -> context.getString(stringRes)
        }
    }

    /**
     * Returns a SpannableString representing currency that shrinks currency symbol if it's necessary.
     * Special case: US people looking at US currency just get the currency symbol.
     *
     */
    fun styleCurrency(value: Double, project: Project, ksCurrency: KSCurrency): SpannableString {
        val formattedCurrency = ksCurrency.format(value, project, RoundingMode.HALF_UP)
        val spannableString = SpannableString(formattedCurrency)

        val country = Country.findByCurrencyCode(project.currency()) ?: return spannableString

        val currencyNeedsCode = ksCurrency.currencyNeedsCode(country, true)
        val currencySymbolToDisplay = ksCurrency.getCurrencySymbol(country, true).trimAllWhitespace()

        if (currencyNeedsCode) {
            val startOfSymbol = formattedCurrency.indexOf(currencySymbolToDisplay)
            val endOfSymbol = startOfSymbol + currencySymbolToDisplay.length
            spannableString.setSpan(RelativeSizeSpan(.7f), startOfSymbol, endOfSymbol, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }

        return spannableString
    }

    /**
     * Returns the title for an Add On ie: 1 x TITLE
     *  [1 x] in green
     *  TITLE regular string
     */
    fun styleTitleForAddOns(context: Context, title: String?, quantity: Int?): SpannableString {
        val symbol = " x "
        val numberGreenCharacters = quantity.toString().length + symbol.length
        val spannable = SpannableString(quantity.toString() + symbol + title)
        spannable.setSpan(
            ForegroundColorSpan(context.getColor(R.color.kds_create_700)),
            0, numberGreenCharacters,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    /**
     * Returns the string for the error message a user receives when their inputted bonus amount causes
     * the total pledge amount to exceed the max pledge amount:
     *
     *  Enter an amount less than $X.
     *
     *  where X is calculated as maxPledgeAmount - rewardAmount
     */
    fun getMaxInputString(
        context: Context,
        selectedReward: Reward?,
        maxPledgeAmount: Double,
        totalAmount: Double,
        totalBonusSupport: Double,
        currencySymbolStartAndEnd: kotlin.Pair<String?, String?>,
        environment: Environment?
    ): String {

        // rewardAmount + totalBonusSupport = totalAmount
        // totalAmount must be <= maxPledgeAmount

        val maxInputAmount = if (selectedReward != null && RewardUtils.isNoReward(selectedReward)) {
            maxPledgeAmount
        } else {
            val rewardAmount = totalAmount - totalBonusSupport
            maxPledgeAmount - rewardAmount
        }
        val maxInputAmountWithCurrency =
            (currencySymbolStartAndEnd.first ?: "") +
                if (maxInputAmount % 1.0 == 0.0) maxInputAmount.toInt().toString()
                else maxInputAmount.toString() + (currencySymbolStartAndEnd.second ?: "")

        return environment?.ksString()?.format(
            context.getString(R.string.Enter_an_amount_less_than_max_pledge), // TODO: MBL-1416 Copy should say less than or equal to
            "max_pledge",
            maxInputAmountWithCurrency
        ) ?: ""
    }

    /**
     * Return the string for the estimated shipping costs for a given shipping rule
     *
     * Ex. "About $10-$15" or "About $10-%15 each"
     */
    fun getEstimatedShippingCostString(
        context: Context,
        ksCurrency: KSCurrency,
        ksString: KSString,
        project: Project,
        selectedShippingRule: ShippingRule,
        isAddOn: Boolean,
        multipleQuantitiesAllowed: Boolean,
        shippingRules: List<ShippingRule>?,
    ): String {
        var min = ""
        var max = ""
        if (isAddOn) {
            if (!shippingRules.isNullOrEmpty()) {
                shippingRules.filter {
                    it.location()?.id() == selectedShippingRule.location()?.id()
                }.map {
                    if (it.estimatedMin() <= 0 || it.estimatedMax() <= 0) return ""
                    min = ksCurrency.format(it.estimatedMin(), project, RoundingMode.HALF_UP)
                    max = ksCurrency.format(it.estimatedMax(), project, RoundingMode.HALF_UP)
                }
            } else return ""
        } else {
            if (selectedShippingRule.estimatedMin() <= 0 || selectedShippingRule.estimatedMax() <= 0) return ""
            min = ksCurrency.format(selectedShippingRule.estimatedMin(), project, RoundingMode.HALF_UP)
            max = ksCurrency.format(selectedShippingRule.estimatedMax(), project, RoundingMode.HALF_UP)
        }

        if (min.isEmpty() || max.isEmpty()) return ""

        // TODO: Replace with defined string
        val minToMaxString = if (multipleQuantitiesAllowed) "$min-$max each" else "$min-$max"

        return ksString.format(
            context.getString(R.string.About_reward_amount),
            "reward_amount",
            minToMaxString
        )
    }

    /**
     * Returns a string for the shipping costs for add-on cards
     *
     * Ex. " + $5 each"
     */
    fun getAddOnShippingAmountString(
        context: Context,
        project: Project,
        reward: Reward,
        rewardShippingRules: List<ShippingRule>?,
        ksCurrency: KSCurrency?,
        ksString: KSString?,
        selectedShippingRule: ShippingRule
    ): String {
        if (rewardShippingRules.isNullOrEmpty() || ksCurrency.isNull() || ksString.isNull()) return ""
        val shippingAmount =
            if (!RewardUtils.isDigital(reward) && RewardUtils.isShippable(reward) && !RewardUtils.isLocalPickup(reward)) {
                var cost = 0.0
                rewardShippingRules.filter {
                    it.location()?.id() == selectedShippingRule.location()?.id()
                }.map {
                    cost += it.cost()
                }
                if (cost > 0) ksCurrency?.format(cost, project)
                else ""
            } else {
                ""
            }
        if (shippingAmount.isNullOrEmpty()) return ""
        val rewardAndShippingString =
            context.getString(R.string.reward_amount_plus_shipping_cost_each)
        val stringSections = rewardAndShippingString.split("+")
        val shippingString = " +" + stringSections[1]
        val ammountAndShippingString = ksString?.format(
            shippingString,
            "shipping_cost",
            shippingAmount
        )
        return ammountAndShippingString ?: ""
    }
}
