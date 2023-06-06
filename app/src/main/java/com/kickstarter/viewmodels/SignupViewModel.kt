package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.CurrentConfigTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isEmail
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.viewmodels.usecases.LoginUseCase
import com.kickstarter.viewmodels.usecases.RefreshUserUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface SignupViewModel {
    interface Inputs {
        /** Call when the email field changes.  */
        fun email(email: String)

        /** Call when the name field changes.  */
        fun name(name: String)

        /** Call when the password field changes.  */
        fun password(password: String)

        /** Call when the send newsletter toggle changes.  */
        fun sendNewslettersClick(send: Boolean)

        /** Call when the signup button has been clicked.  */
        fun signupClick()
    }

    interface Outputs {
        /** Emits a string to display when signup fails.  */
        fun errorString(): Observable<String>

        /** Emits a boolean that determines if the sign up button is enabled.  */
        fun formIsValid(): Observable<Boolean>

        /** Emits a boolean that determines if the sign up button is disabled.  */
        fun formSubmitting(): Observable<Boolean>

        /** Emits a boolean that determines if the send newsletter toggle is checked.  */
        fun sendNewslettersIsChecked(): Observable<Boolean>

        /** Finish the activity with a successful result.  */
        fun signupSuccess(): Observable<Unit>
    }

    class SignupViewModel(environment: Environment) :
        ViewModel(),
        Inputs,
        Outputs {
        private val client: ApiClientTypeV2
        private val analyticEvents = requireNotNull(environment.analytics())
        private val currentConfig: CurrentConfigTypeV2
        private val loginUserCase = LoginUseCase(environment)
        private val refreshUserUseCase = RefreshUserUseCase(environment)
        private val disposables = CompositeDisposable()

        private fun submit(data: SignupData): Observable<AccessTokenEnvelope> {
            return client.signup(
                data.name,
                data.email,
                data.password,
                data.password,
                data.sendNewsletters
            )
                .compose(Transformers.pipeApiErrorsToV2(signupError))
                .compose(Transformers.neverErrorV2())
                .doOnSubscribe { formSubmitting.onNext(true) }
                .doAfterTerminate { formSubmitting.onNext(false) }
        }

        private fun success(user: User) {
            refreshUserUseCase.refresh(user)
            signupSuccess.onNext(Unit)
        }

        private val email = PublishSubject.create<String>()
        private val name = PublishSubject.create<String>()
        private val password = PublishSubject.create<String>()
        private val sendNewslettersClick = PublishSubject.create<Boolean>()
        private val signupClick = PublishSubject.create<Unit>()
        private val errorString: Observable<String>
        private val signupSuccess = PublishSubject.create<Unit>()
        private val formSubmitting = BehaviorSubject.create<Boolean>()
        private val formIsValid = BehaviorSubject.create<Boolean>()
        private val sendNewslettersIsChecked = BehaviorSubject.create<Boolean>()
        private val showInterstitial = BehaviorSubject.create<Unit>()
        private val signupError = PublishSubject.create<ErrorEnvelope?>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            client = requireNotNull(environment.apiClientV2())
            currentConfig = requireNotNull(environment.currentConfigV2())

            val signupData = Observable.combineLatest(
                name,
                email,
                password,
                sendNewslettersIsChecked
            ) { name: String, email: String, password: String, sendNewsletters: Boolean ->
                SignupData(
                    name,
                    email,
                    password,
                    sendNewsletters
                )
            }

            // TODO: Note this existing skip logic was moved into the VM as part of MBL-827 migration
            //  without attempting to address this potential newsletters bug:
            //  https://kickstarter.atlassian.net/browse/MBL-847
            sendNewslettersClick
                .skip(1)
                .subscribe { sendNewslettersIsChecked.onNext(it) }
                .addToDisposable(disposables)

            signupData
                .map { it.isValid }
                .subscribe { formIsValid.onNext(it) }
                .addToDisposable(disposables)

            signupData
                .compose(Transformers.takeWhenV2(signupClick))
                .switchMap {
                    submit(it)
                }
                .distinctUntilChanged()
                .switchMap {
                    this.loginUserCase
                        .loginAndUpdateUserPrivacyV2(it.user(), it.accessToken())
                }
                .subscribe { success(it) }
                .addToDisposable(disposables)

            currentConfig.observable()
                .take(1)
                .map { false }
                .subscribe { sendNewslettersIsChecked.onNext(it) }
                .addToDisposable(disposables)

            errorString = signupError
                .takeUntil(signupSuccess)
                .filter { ObjectUtils.isNotNull(it) }
                .map { it.errorMessage() }

            signupClick
                .subscribe { analyticEvents.trackSignUpSubmitCtaClicked() }
                .addToDisposable(disposables)

            analyticEvents.trackSignUpPageViewed()
        }

        override fun email(email: String) {
            this.email.onNext(email)
        }

        override fun name(name: String) {
            this.name.onNext(name)
        }

        override fun password(password: String) {
            this.password.onNext(password)
        }

        override fun sendNewslettersClick(send: Boolean) {
            sendNewslettersClick.onNext(send)
        }

        override fun signupClick() {
            signupClick.onNext(Unit)
        }

        override fun errorString(): Observable<String> = errorString
        override fun formIsValid(): BehaviorSubject<Boolean> = formIsValid
        override fun formSubmitting(): BehaviorSubject<Boolean> = formSubmitting
        override fun sendNewslettersIsChecked(): BehaviorSubject<Boolean> = sendNewslettersIsChecked
        override fun signupSuccess(): PublishSubject<Unit> = signupSuccess

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
        internal class SignupData(
            val name: String,
            val email: String,
            val password: String,
            val sendNewsletters: Boolean
        ) {
            val isValid: Boolean
                get() = name.isNotEmpty() && email.isEmail() && password.length >= 6
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SignupViewModel(environment) as T
        }
    }
}
