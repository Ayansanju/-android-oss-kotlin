package com.kickstarter.libs.utils.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.fragments.PledgeFragment
import com.kickstarter.ui.fragments.PledgeFragmentLegacy

fun Fragment.selectPledgeFragment(
    pledgeData: PledgeData,
    pledgeReason: PledgeReason,
    shouldShowPaymentSheet: Boolean
): Fragment {
    return if (shouldShowPaymentSheet) PledgeFragment().withData(pledgeData, pledgeReason)
    else PledgeFragmentLegacy().withData(pledgeData, pledgeReason)
}

fun Fragment.withData(pledgeData: PledgeData?, pledgeReason: PledgeReason?): Fragment {
    val argument = Bundle()

    pledgeData?.let {
        argument.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, it)
    }
    pledgeReason?.let {
        argument.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, it)
    }

    this.arguments = argument
    return this
}
