package com.kickstarter.viewmodels

import android.support.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.rx.transformers.Transformers.values
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.fragments.NewCardFragment
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import type.PaymentTypes
import java.lang.Exception

interface NewCardFragmentViewModel {
    interface Inputs {
        /** Call when the card validity changes. */
        fun card(card: Card?)

        /** Call when the name field changes. */
        fun name(name: String)

        /** Call when the postal code field changes. */
        fun postalCode(postalCode: String)

        /** Call when the user clicks the save icon. */
        fun saveCardClicked()

        /** Call when the card input has focus. */
        fun cardFocus(hasFocus: Boolean)
    }

    interface Outputs {
        /** Emits when the drawable to be shown when the card widget has focus. */
        fun cardWidgetFocusDrawable(): Observable<Int>

        /** Emits when the password update was unsuccessful. */
        fun error(): Observable<String>

        /** Emits when the progress bar should be visible. */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits when the save button should be enabled. */
        fun saveButtonIsEnabled(): Observable<Boolean>

        /** Emits when the card was saved successfully. */
        fun success(): Observable<Void>

    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<NewCardFragment>(environment), Inputs, Outputs {

        private val card = PublishSubject.create<Card?>()
        private val cardFocus = PublishSubject.create<Boolean>()
        private val name = PublishSubject.create<String>()
        private val postalCode = PublishSubject.create<String>()
        private val saveCardClicked = PublishSubject.create<Void>()

        private val cardWidgetFocusDrawable = BehaviorSubject.create<Int>()
        private val error = BehaviorSubject.create<String>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val saveButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<Void>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val apolloClient = this.environment.apolloClient()
        private val stripe = this.environment.stripe()

        init {
            val cardForm = Observable.combineLatest(this.name.startWith(""),
                    this.card.startWith(null, null),
                    this.postalCode.startWith(""),
                    { name, card, postalCode -> CardForm(name, card, postalCode) })
                    .skip(1)

            cardForm
                    .map { it.isValid() }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.saveButtonIsEnabled)

            this.cardFocus
                    .map {
                        when {
                            it -> R.drawable.divider_green_horizontal
                            else -> R.drawable.divider_dark_grey_500_horizontal
                        }
                    }
                    .subscribe { this.cardWidgetFocusDrawable.onNext(it) }

            val saveCardNotification = cardForm
                    .compose<CardForm>(takeWhen(this.saveCardClicked))
                    .map { storeNameAndPostalCode(it) }
                    .switchMap { createTokenAndSaveCard(it).materialize() }
                    .compose(bindToLifecycle())
                    .share()

            saveCardNotification
                    .compose(values())
                    .subscribe { this.success.onNext(null) }

            saveCardNotification
                    .compose(Transformers.errors())
                    .subscribe { this.error.onNext(it.localizedMessage) }

        }

        private fun storeNameAndPostalCode(cardForm: CardForm): Card {
            val card = cardForm.card!!
            card.name = cardForm.name
            card.addressZip = cardForm.postalCode
            return card
        }

        override fun card(card: Card?) {
            this.card.onNext(card)
        }

        override fun cardFocus(hasFocus: Boolean) {
            this.cardFocus.onNext(hasFocus)
        }

        override fun name(name: String) {
            this.name.onNext(name)
        }

        override fun postalCode(postalCode: String) {
            this.postalCode.onNext(postalCode)
        }

        override fun saveCardClicked() {
            this.saveCardClicked.onNext(null)
        }

        override fun cardWidgetFocusDrawable(): Observable<Int> {
            return this.cardWidgetFocusDrawable
        }

        override fun error(): Observable<String> {
            return this.error
        }

        override fun progressBarIsVisible(): Observable<Boolean> {
            return this.progressBarIsVisible
        }

        override fun saveButtonIsEnabled(): Observable<Boolean> {
            return this.saveButtonIsEnabled
        }

        override fun success(): Observable<Void> {
            return this.success
        }

        data class CardForm(val name: String, val card: Card?, val postalCode: String) {
            fun isValid(): Boolean {
                return isNotEmpty(this.name)
                        && isNotEmpty(this.postalCode)
                        && isValidCard(this.card)
            }

            private fun isValidCard(card: Card?): Boolean {
                return card != null && card.validateNumber() && card.validateExpiryDate() && card.validateCVC()
            }

            private fun isNotEmpty(s: String): Boolean {
                return !s.isEmpty()
            }
        }

        private fun createTokenAndSaveCard(card: Card): Observable<Void> {
            return Observable.defer {
                val ps = PublishSubject.create<Void>()
                this.stripe.createToken(card, object : TokenCallback {
                    override fun onSuccess(token: Token) {
                        saveCard(token, ps)
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

        private fun saveCard(token: Token, ps: PublishSubject<Void>) {
            this.apolloClient.savePaymentMethod(PaymentTypes.CREDIT_CARD, token.id, token.card.id)
                    .subscribe({
                        ps.onNext(null)
                        ps.onCompleted()
                    }, { ps.onError(it) })
        }
    }
}
