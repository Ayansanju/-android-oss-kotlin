package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Comment
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ThreadActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import timber.log.Timber

interface ThreadViewModel {

    interface Inputs
    interface Outputs {
        /** The anchored root comment */
        fun getRootComment(): Observable<Comment>

        /** Will tell to the compose view if should open the keyboard */
        fun shouldFocusOnCompose(): Observable<Boolean>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ThreadActivity>(environment), Inputs, Outputs {
        private val apolloClient: ApolloClientType = environment.apolloClient()
        private val currentUser: CurrentUserType = environment.currentUser()

        private val rootComment = BehaviorSubject.create<Comment>()
        private val focusOnCompose = BehaviorSubject.create<Boolean>()

        val inputs = this
        val outputs = this

        init {
            getCommentFromIntent()

            val comment = getCommentFromIntent()
                .distinctUntilChanged()
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            intent()
                .map { it.getBooleanExtra(IntentKey.REPLY_EXPAND, false) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.focusOnCompose)

            val commentEnvelope = getCommentFromIntent()
                .switchMap {
                    this.apolloClient.getRepliesForComment(it)
                }
                .share()

            commentEnvelope
                .compose(bindToLifecycle())
                .subscribe {
                    Timber.i(
                        "******* Comment envelope with \n" +
                            "totalComments:${it.totalCount} \n" +
                            "commentsList: ${it.comments?.map { comment -> comment.body() + " |"}} \n" +
                            "commentsListSize: ${it.comments?.size} \n" +
                            "pageInfo: ${it.pageInfoEnvelope} ****"
                    )
                }

            comment
                .compose(bindToLifecycle())
                .subscribe(this.rootComment)
        }

        private fun getCommentFromIntent() = intent()
            .map { it.getParcelableExtra(IntentKey.COMMENT) as Comment? }
            .ofType(Comment::class.java)

        override fun getRootComment(): Observable<Comment> = this.rootComment
        override fun shouldFocusOnCompose(): Observable<Boolean> = this.focusOnCompose
    }
}
