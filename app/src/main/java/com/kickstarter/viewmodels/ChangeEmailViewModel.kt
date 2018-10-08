package com.kickstarter.viewmodels

import UpdateUserEmailMutation
import android.support.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.StringUtils
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.activities.ChangeEmailActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface ChangeEmailViewModel {

    interface Inputs {
        /** Call when the new email field changes.  */
        fun email(email: String)

        /** Call when the current password field changes.  */
        fun password(password: String)

        /** Call when save button has been clicked.  */
        fun updateEmailClicked()
    }

    interface Outputs {
        /** Emits the logged in user's email address.  */
        fun currentEmail(): Observable<String>

        /** Emits a string to display when user could not be found.  */
        fun error(): Observable<String>

        /** Emits a boolean that determines if the save button should be enabled.  */
        fun saveButtonIsEnabled(): Observable<Boolean>

        /** Emits a boolean that determines if a network call is in progress.  */
        fun showProgressBar(): Observable<Boolean>

        /** Emits when the user's email is changed successfully. */
        fun success(): Observable<Void>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ChangeEmailActivity>(environment), Inputs, Outputs {

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val newEmail = PublishSubject.create<String>()
        private val currentPassword = PublishSubject.create<String>()
        private val updateEmailClicked = PublishSubject.create<Void>()

        private val currentEmail = BehaviorSubject.create<String>()
        private val saveButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val showProgressBar = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<Void>()

        private val error = BehaviorSubject.create<String>()

        private val apolloClient: ApolloClientType = environment.apolloClient()

        init {

            this.apolloClient.userPrivacy()
                    .compose(neverError())
                    .compose(bindToLifecycle())
                    .subscribe {
                        this@ViewModel.currentEmail.onNext(it.me()?.email())
                    }

            val changeEmail = Observable.combineLatest(this.newEmail, this.currentPassword,
                    { email, password -> ChangeEmail(email, password) })

            changeEmail
                    .map { ce -> ce.isValid() }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe { this.saveButtonIsEnabled.onNext(it) }

            val updateEmailNotification = changeEmail
                    .compose(Transformers.takeWhen<ChangeEmail, Void>(this.updateEmailClicked))
                    .switchMap { updateEmail(it).materialize() }
                    .compose(bindToLifecycle())
                    .share()

            updateEmailNotification
                    .compose(errors())
                    .subscribe({ this.error.onNext(it.localizedMessage) })

            updateEmailNotification
                    .compose(values())
                    .subscribe({
                        this.currentEmail.onNext(it.updateUserAccount()?.user()?.email())
                        this.success.onNext(null)
                    })
        }

        override fun email(email: String) {
            this.newEmail.onNext(email)
        }

        override fun password(password: String) {
            this.currentPassword.onNext(password)
        }

        override fun updateEmailClicked() {
            this.updateEmailClicked.onNext(null)
        }

        override fun currentEmail(): Observable<String> = this.currentEmail

        override fun saveButtonIsEnabled(): Observable<Boolean> = this.saveButtonIsEnabled

        override fun showProgressBar(): Observable<Boolean> = this.showProgressBar

        override fun success(): Observable<Void> = this.success

        override fun error(): Observable<String> = this.error

        private fun updateEmail(changeEmail: ChangeEmail): Observable<UpdateUserEmailMutation.Data> {
            return this.apolloClient.updateUserEmail(changeEmail.email, changeEmail.password)
                    .doOnSubscribe { this.showProgressBar.onNext(true) }
                    .doAfterTerminate { this.showProgressBar.onNext(false) }
        }

        data class ChangeEmail(val email: String, val password: String) {
            fun isValid(): Boolean {
                return StringUtils.isEmail(this.email) && StringUtils.isValidPassword(this.password)
            }
        }
    }
}
