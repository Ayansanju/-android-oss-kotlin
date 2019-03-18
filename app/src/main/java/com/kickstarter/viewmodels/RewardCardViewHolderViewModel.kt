package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.viewholders.RewardCardViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.*

interface RewardCardViewHolderViewModel {
    interface Inputs {
        /** Call to configure view model with a stored card. */
        fun configureWith(storedCard: StoredCard)

        /** Call when the select button has been clicked. */
        fun selectCardButtonClicked()
    }

    interface Outputs {
        /** Emits the drawable for the card issuer (ex Visa, Mastercard, AMEX). */
        fun issuerImage(): Observable<Int>

        /** Emits the expiration date for a credit card. */
        fun expirationDate(): Observable<String>

        /** Emits the last four digits of the credit card. */
        fun lastFour(): Observable<String>
    }

    class ViewModel(val environment: Environment) : ActivityViewModel<RewardCardViewHolder>(environment), Inputs, Outputs {
        private val card = PublishSubject.create<StoredCard>()
        private val deleteCardClick = PublishSubject.create<Void>()

        private val issuerImage = BehaviorSubject.create<Int>()
        private val expirationDate = BehaviorSubject.create<String>()
        private val lastFour = BehaviorSubject.create<String>()

        private val sdf = SimpleDateFormat(StoredCard.DATE_FORMAT, Locale.getDefault())

        val inputs: RewardCardViewHolderViewModel.Inputs = this
        val outputs: RewardCardViewHolderViewModel.Outputs = this

        init {
            this.card.map { it.expiration() }
                    .map { sdf.format(it).toString() }
                    .subscribe { this.expirationDate.onNext(it) }

            this.card.map { it.lastFourDigits() }
                    .subscribe { this.lastFour.onNext(it) }

            this.card.map { it.type() }
                    .map { StoredCard.getCardTypeDrawable(it) }
                    .subscribe { this.issuerImage.onNext(it) }

        }
        override fun configureWith(storedCard: StoredCard) = this.card.onNext(storedCard)

        override fun selectCardButtonClicked() = this.deleteCardClick.onNext(null)

        override fun issuerImage(): Observable<Int> = this.issuerImage

        override fun expirationDate(): Observable<String> = this.expirationDate

        override fun lastFour(): Observable<String> = this.lastFour

    }
}
