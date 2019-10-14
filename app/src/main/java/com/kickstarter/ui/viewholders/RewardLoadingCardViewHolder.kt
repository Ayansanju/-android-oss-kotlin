package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.AnimationUtils
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.viewmodels.RewardLoadingCardViewHolderViewModel
import kotlinx.android.synthetic.main.item_reward_loading_card.view.*
import kotlinx.android.synthetic.main.reward_card_details.view.*

class RewardLoadingCardViewHolder(val view: View) : KSViewHolder(view) {

    private val viewModel: RewardLoadingCardViewHolderViewModel.ViewModel = RewardLoadingCardViewHolderViewModel.ViewModel(environment())
    private val ksString: KSString = environment().ksString()

    private val creditCardExpirationString = this.context().getString(R.string.Credit_card_expiration)
    private val endingInString = this.context().getString(R.string.Ending_in_last_four)

    init {

        this.viewModel.outputs.expirationDate()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setExpirationDateTextView(it) }

        this.viewModel.outputs.issuerImage()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.reward_card_logo.setImageResource(it) }

        this.viewModel.outputs.issuer()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.reward_card_logo.contentDescription = it }

        this.viewModel.outputs.lastFour()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setLastFourTextView(it) }

        this.viewModel.outputs.id()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { animateProgressBar() }
    }

    private fun animateProgressBar() {
        AnimationUtils.fadeInAndScale(this.view.reward_loading_progress_bar, 120L).start()
    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val cardAndProject = requireNotNull(data) as Pair<StoredCard, Project>
        this.viewModel.inputs.configureWith(cardAndProject)
    }

    private fun setExpirationDateTextView(date: String) {
        this.view.reward_card_expiration_date.text = this.ksString.format(this.creditCardExpirationString,
                "expiration_date", date)
    }

    private fun setLastFourTextView(lastFour: String) {
        this.view.reward_card_last_four.text = this.ksString.format(this.endingInString, "last_four", lastFour)
    }

}
