package com.kickstarter.viewmodels

import android.support.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.ui.activities.NotificationsActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface NotificationsViewModel {
    interface Inputs {

        /** Call when the notify mobile of new followers toggle changes.  */
        fun notifyMobileOfFollower(checked: Boolean)

        /** Call when the notify mobile of friend backs a project toggle changes.  */
        fun notifyMobileOfFriendActivity(checked: Boolean)

        /** Call when the notify mobile of messages toggle changes.  */
        fun notifyMobileOfMessages(checked: Boolean)

        /** Call when the notify mobile of project updates toggle changes.  */
        fun notifyMobileOfUpdates(checked: Boolean)

        /** Call when the notify of new followers toggle changes.  */
        fun notifyOfFollower(checked: Boolean)

        /** Call when the notify of friend backs a project toggle changes.  */
        fun notifyOfFriendActivity(checked: Boolean)

        /** Call when the notify of messages toggle changes.  */
        fun notifyOfMessages(checked: Boolean)

        /** Call when the notify of project updates toggle changes.  */
        fun notifyOfUpdates(checked: Boolean)
    }

    interface Outputs {

        /** Emits user containing settings state.  */
        fun user(): Observable<User>
    }

    interface Errors {
        fun unableToSavePreferenceError(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<NotificationsActivity>(environment), Inputs, Outputs, Errors {
        private val userInput = PublishSubject.create<User>()

        private val userOutput = BehaviorSubject.create<User>()
        private val updateSuccess = PublishSubject.create<Void>()

        private val unableToSavePreferenceError = PublishSubject.create<Throwable>()

        val inputs: Inputs = this
        val outputs: Outputs = this
        val errors: Errors = this

        private val client: ApiClientType = environment.apiClient()
        private val currentUser: CurrentUserType = environment.currentUser()

        init {

            this.client.fetchCurrentUser()
                    .retry(2)
                    .compose(Transformers.neverError())
                    .compose(bindToLifecycle())
                    .subscribe { this.currentUser.refresh(it) }

            this.currentUser.observable()
                    .take(1)
                    .compose(bindToLifecycle())
                    .subscribe({ this.userOutput.onNext(it) })

            this.userInput
                    .concatMap<User>({ this.updateSettings(it) })
                    .compose(bindToLifecycle())
                    .subscribe({ this.success(it) })

            this.userInput
                    .compose(bindToLifecycle())
                    .subscribe(this.userOutput)

            this.userOutput
                    .window(2, 1)
                    .flatMap<List<User>>({ it.toList() })
                    .map<User>({ ListUtils.first(it) })
                    .compose<User>(takeWhen<User, Throwable>(this.unableToSavePreferenceError))
                    .compose(bindToLifecycle())
                    .subscribe(this.userOutput)
        }

        override fun notifyMobileOfFollower(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyMobileOfFollower(checked).build())
        }

        override fun notifyMobileOfFriendActivity(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyMobileOfFriendActivity(checked).build())
        }

        override fun notifyMobileOfMessages(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyMobileOfMessages(checked).build())
        }

        override fun notifyMobileOfUpdates(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyMobileOfUpdates(checked).build())
        }

        override fun notifyOfFollower(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyOfFollower(checked).build())
        }

        override fun notifyOfFriendActivity(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyOfFriendActivity(checked).build())
        }

        override fun notifyOfMessages(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyOfMessages(checked).build())
        }

        override fun notifyOfUpdates(checked: Boolean) {
            this.userInput.onNext(this.userOutput.value.toBuilder().notifyOfUpdates(checked).build())
        }

        override fun user(): Observable<User> {
            return this.userOutput
        }

        override fun unableToSavePreferenceError(): Observable<String> {
            return this.unableToSavePreferenceError
                    .takeUntil(this.updateSuccess)
                    .map { _ -> null }
        }

        private fun success(user: User) {
            this.currentUser.refresh(user)
            this.updateSuccess.onNext(null)
        }

        private fun updateSettings(user: User): Observable<User> {
            return this.client.updateUserSettings(user)
                    .compose(Transformers.pipeErrorsTo(this.unableToSavePreferenceError))
        }
    }
}
