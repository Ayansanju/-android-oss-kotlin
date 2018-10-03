package com.kickstarter.viewmodels

import UpdateUserPasswordMutation
import android.support.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.activities.ChangePasswordActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

const val MINIMUM_PASSWORD_LENGTH = 6

interface ChangePasswordViewModel {

    interface Inputs {
        /** Call when the user clicks the change password button. */
        fun changePasswordClicked()

        /** Call when the current password field changes.  */
        fun confirmPassword(confirmPassword: String)

        /** Call when the current password field changes.  */
        fun currentPassword(currentPassword: String)

        /** Call when the new password field changes.  */
        fun newPassword(newPassword: String)
    }

    interface Outputs {
        /** Emits when the password update was unsuccessful. */
        fun error(): Observable<String>

        /** Emits when the user types a password confirmation that doesn't match the new password. */
        fun passwordConfirmationWarningIsVisible(): Observable<Boolean>

        /** Emits when the user types a new password less than 6 characters. */
        fun passwordLengthWarningIsVisible(): Observable<Boolean>

        /** Emits when the progress bar should be visible. */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits when the save button should be enabled. */
        fun saveButtonIsEnabled(): Observable<Boolean>

        /** Emits when the password update was unsuccessful. */
        fun success(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ChangePasswordActivity>(environment), Inputs, Outputs {

        private val changePasswordClicked = PublishSubject.create<Void>()
        private val confirmPassword = PublishSubject.create<String>()
        private val currentPassword = PublishSubject.create<String>()
        private val newPassword = PublishSubject.create<String>()

        private val error = BehaviorSubject.create<String>()
        private val passwordConfirmationWarningIsVisible = BehaviorSubject.create<Boolean>()
        private val passwordLengthWarningIsVisible = BehaviorSubject.create<Boolean>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val saveButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<String>()

        val inputs: ChangePasswordViewModel.Inputs = this
        val outputs: ChangePasswordViewModel.Outputs = this

        private val apolloClient: ApolloClientType = this.environment.apolloClient()

        init {

            val changePassword = Observable.combineLatest(this.currentPassword,
                    this.confirmPassword,
                    this.newPassword,
                    { current, new, confirm -> ChangePassword(current, new, confirm) })

            this.newPassword
                    .map<Boolean> { it.length in 1 until MINIMUM_PASSWORD_LENGTH }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.passwordLengthWarningIsVisible)

            this.confirmPassword
                    .compose(combineLatestPair<String, String>(this.newPassword))
                    .filter { it.first.isNotEmpty() }
                    .map<Boolean> { it.second != it.first }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.passwordConfirmationWarningIsVisible)

            changePassword
                    .map { cp -> cp.isValid() }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.saveButtonIsEnabled)

            val changePasswordNotification = changePassword
                    .compose(takeWhen<ChangePassword, Void>(this.changePasswordClicked))
                    .switchMap { cp -> submit(cp).materialize() }
                    .compose(bindToLifecycle())
                    .share()

            changePasswordNotification
                    .compose(errors())
                    .subscribe({ this.error.onNext(it.localizedMessage) })

            changePasswordNotification
                    .compose(values())
                    .map { it.updateUserAccount()?.user()?.email() }
                    .subscribe(this.success)

        }

        private fun submit(changePassword: ChangePasswordViewModel.ViewModel.ChangePassword): Observable<UpdateUserPasswordMutation.Data> {
            return this.apolloClient.updateUserPassword(changePassword.currentPassword, changePassword.newPassword, changePassword.confirmPassword)
                    .doOnSubscribe { this.progressBarIsVisible.onNext(true) }
                    .doAfterTerminate { this.progressBarIsVisible.onNext(false) }
        }

        override fun changePasswordClicked() {
            this.changePasswordClicked.onNext(null)
        }

        override fun confirmPassword(confirmPassword: String) {
            this.confirmPassword.onNext(confirmPassword)
        }

        override fun currentPassword(currentPassword: String) {
            this.currentPassword.onNext(currentPassword)
        }

        override fun newPassword(newPassword: String) {
            this.newPassword.onNext(newPassword)
        }

        override fun error(): Observable<String> {
            return this.error
        }

        override fun passwordConfirmationWarningIsVisible(): Observable<Boolean> {
            return this.passwordConfirmationWarningIsVisible
        }

        override fun passwordLengthWarningIsVisible(): Observable<Boolean> {
            return this.passwordLengthWarningIsVisible
        }

        override fun progressBarIsVisible(): Observable<Boolean> {
            return this.progressBarIsVisible
        }

        override fun saveButtonIsEnabled(): Observable<Boolean> {
            return this.saveButtonIsEnabled
        }

        override fun success(): Observable<String> {
            return this.success
        }

        data class ChangePassword(val currentPassword: String, val newPassword: String, val confirmPassword: String) {
            fun isValid(): Boolean {
                return isNotEmptyAndAtLeast6Chars(this.currentPassword)
                        && isNotEmptyAndAtLeast6Chars(this.newPassword)
                        && isNotEmptyAndAtLeast6Chars(this.confirmPassword)
                        && this.confirmPassword == this.newPassword
            }

            private fun isNotEmptyAndAtLeast6Chars(password: String) = !password.isEmpty() && password.length >= MINIMUM_PASSWORD_LENGTH
        }
    }
}
