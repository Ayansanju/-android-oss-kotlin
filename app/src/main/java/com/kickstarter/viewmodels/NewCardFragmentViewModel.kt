package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.fragments.NewCardFragment
import com.stripe.android.CardUtils
import com.stripe.android.TokenCallback
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import type.PaymentTypes

interface NewCardFragmentViewModel {
    interface Inputs {
        /** Call when the card validity changes. */
        fun card(card: Card?)

        /** Call when the card number text changes. */
        fun cardNumber(cardNumber: String)

        /** Call when the name field changes. */
        fun name(name: String)

        /** Call when the postal code field changes. */
        fun postalCode(postalCode: String)

        /** Call when the reusable switch is toggled. */
        fun reusable(reusable: Boolean)

        /** Call when the user clicks the save icon. */
        fun saveCardClicked()

        /** Call when the card input has focus. */
        fun cardFocus(hasFocus: Boolean)
    }

    interface Outputs {
        /** Emits a boolean determining if the allowed card warning should be visible. */
        fun allowedCardWarningIsVisible(): Observable<Boolean>

        /** Emits a boolean determining if the AppBarLayout should have elevation. */
        fun appBarLayoutHasElevation(): Observable<Boolean>

        /** Emits a drawable to be shown based on when the card widget has focus. */
        fun cardWidgetFocusDrawable(): Observable<Int>

        /** Emits a boolean determining if the form divider should be visible. */
        fun dividerIsVisible(): Observable<Boolean>

        /** Emits when saving the card was unsuccessful and the fragment is not modal. */
        fun error(): Observable<Void>

        /** Emits when saving the card was unsuccessful and the fragment is modal. */
        fun modalError(): Observable<Void>

        /** Emits when the progress bar should be visible. */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits a boolean determining if the reusable switch should be visible. */
        fun reusableContainerIsVisible(): Observable<Boolean>

        /** Emits a boolean determining if the save button should be enabled. */
        fun saveButtonIsEnabled(): Observable<Boolean>

        /** Emits when the card was saved successfully. */
        fun success(): Observable<StoredCard>

    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<NewCardFragment>(environment), Inputs, Outputs {

        private val card = PublishSubject.create<Card?>()
        private val cardFocus = PublishSubject.create<Boolean>()
        private val cardNumber = PublishSubject.create<String>()
        private val name = PublishSubject.create<String>()
        private val postalCode = PublishSubject.create<String>()
        private val reusable = PublishSubject.create<Boolean>()
        private val saveCardClicked = PublishSubject.create<Void>()

        private val allowedCardWarningIsVisible = BehaviorSubject.create<Boolean>()
        private val appBarLayoutHasElevation = BehaviorSubject.create<Boolean>()
        private val cardWidgetFocusDrawable = BehaviorSubject.create<Int>()
        private val dividerIsVisible = BehaviorSubject.create<Boolean>()
        private val error = BehaviorSubject.create<Void>()
        private val modalError = BehaviorSubject.create<Void>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val reusableContainerIsVisible = BehaviorSubject.create<Boolean>()
        private val saveButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<StoredCard>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val apolloClient = this.environment.apolloClient()
        private val stripe = this.environment.stripe()

        init {
            val modal = arguments()
                    .map { it?.getBoolean(ArgumentsKey.NEW_CARD_MODAL)?: false }
                    .distinctUntilChanged()

            modal
                    .map { BooleanUtils.negate(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.appBarLayoutHasElevation)

            modal
                    .map { BooleanUtils.negate(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.dividerIsVisible)

            modal
                    .compose(bindToLifecycle())
                    .subscribe(this.reusableContainerIsVisible)

            val initialReusable = modal
                    .map { BooleanUtils.negate(it) }

            val reusable = Observable.merge(initialReusable, this.reusable)
            val cardForm = Observable.combineLatest(this.name,
                    this.card,
                    this.cardNumber,
                    this.postalCode,
                    reusable)
            { name, card, cardNumber, postalCode, reusable -> CardForm(name, card, cardNumber, postalCode, reusable) }

            cardForm
                    .map { it.isValid() }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.saveButtonIsEnabled)

            this.cardNumber
                    .map { CardForm.isAllowedCard(it) }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.allowedCardWarningIsVisible)

            this.cardFocus
                    .map {
                        when {
                            it -> R.drawable.divider_green_horizontal
                            else -> R.drawable.divider_dark_grey_500_horizontal
                        }
                    }
                    .subscribe { this.cardWidgetFocusDrawable.onNext(it) }

            val saveCardNotification = cardForm
                    .map {
                        it.card?.let { card ->
                            card.name = it.name
                            card.addressZip = it.postalCode
                            card
                        }
                    }
                    .compose<Pair<Card, Boolean>>(combineLatestPair(reusable))
                    .compose<Pair<Card, Boolean>>(takeWhen(this.saveCardClicked))
                    .switchMap { createTokenAndSaveCard(it).materialize() }
                    .compose(bindToLifecycle())
                    .share()

            saveCardNotification
                    .compose(values())
                    .subscribe()

            val error = saveCardNotification
                    .compose(errors())
                    .compose(ignoreValues())
                    .compose<Pair<Void, Boolean>>(combineLatestPair(modal))

            error
                    .filter { !it.second }
                    .map { it.first }
                    .subscribe(this.error)

            error
                    .filter { it.second }
                    .map { it.first }
                    .subscribe(this.modalError)

            saveCardNotification
                    .compose(errors())
                    .subscribe { this.koala.trackFailedPaymentMethodCreation() }

            this.koala.trackViewedAddNewCard()
        }

        override fun card(card: Card?) {
            this.card.onNext(card)
        }

        override fun cardFocus(hasFocus: Boolean) {
            this.cardFocus.onNext(hasFocus)
        }

        override fun cardNumber(cardNumber: String) {
            this.cardNumber.onNext(cardNumber)
        }

        override fun name(name: String) {
            this.name.onNext(name)
        }

        override fun postalCode(postalCode: String) {
            this.postalCode.onNext(postalCode)
        }

        override fun reusable(reusable: Boolean) {
            this.reusable.onNext(reusable)
        }

        override fun saveCardClicked() {
            this.saveCardClicked.onNext(null)
        }

        override fun allowedCardWarningIsVisible(): Observable<Boolean> = this.allowedCardWarningIsVisible

        override fun appBarLayoutHasElevation(): Observable<Boolean> = this.appBarLayoutHasElevation

        override fun cardWidgetFocusDrawable(): Observable<Int> = this.cardWidgetFocusDrawable

        override fun dividerIsVisible(): Observable<Boolean> = this.dividerIsVisible

        override fun error(): Observable<Void> = this.error

        override fun modalError(): Observable<Void> = this.modalError

        override fun progressBarIsVisible(): Observable<Boolean> = this.progressBarIsVisible

        override fun reusableContainerIsVisible(): Observable<Boolean> = this.reusableContainerIsVisible

        override fun saveButtonIsEnabled(): Observable<Boolean> = this.saveButtonIsEnabled

        override fun success(): Observable<StoredCard> = this.success

        data class CardForm(val name: String, val card: Card?, val cardNumber: String, val postalCode: String, val reusable: Boolean) {

            fun isValid(): Boolean {
                return this.name.isNotEmpty()
                        && this.postalCode.isNotEmpty()
                        && isValidCard()
            }

            private fun isValidCard(): Boolean {
                return this.card != null && isAllowedCard(this.cardNumber) && this.card.validateNumber() && this.card.validateExpiryDate() && card.validateCVC()
            }

            companion object {
                fun isAllowedCard(cardNumber: String): Boolean {
                    return cardNumber.length < 3 || CardUtils.getPossibleCardType(cardNumber) in allowedCardTypes
                }

                private val allowedCardTypes = arrayOf(Card.AMERICAN_EXPRESS,
                        Card.DINERS_CLUB,
                        Card.DISCOVER,
                        Card.JCB,
                        Card.MASTERCARD,
                        Card.UNIONPAY,
                        Card.VISA)
            }
        }

        private fun createTokenAndSaveCard(cardAndReusable: Pair<Card, Boolean>): Observable<StoredCard> {
            return Observable.defer {
                val ps = PublishSubject.create<StoredCard>()
                this.stripe.createToken(cardAndReusable.first, object : TokenCallback {
                    override fun onSuccess(token: Token) {
                        saveCard(token, cardAndReusable.second, ps)
                    }

                    override fun onError(error: Exception?) {
                        ps.onError(error)
                    }
                })
                return@defer ps
            }
                    .doOnSubscribe { this.progressBarIsVisible.onNext(true) }
                    .doAfterTerminate { this.progressBarIsVisible.onNext(false) }
        }

        private fun saveCard(token: Token, reusable:Boolean, ps: PublishSubject<StoredCard>) {
            this.apolloClient.savePaymentMethod(PaymentTypes.CREDIT_CARD, token.id, token.card.id, reusable)
                    .subscribe({
                        ps.onCompleted()
                        this.success.onNext(it)
                        this.koala.trackSavedPaymentMethod()
                    }, { ps.onError(it) })
        }
    }
}
