package com.kickstarter.viewmodels

import android.os.Bundle
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.EMAIL_VERIFICATION_SKIP
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.AccessTokenEnvelopeFactory
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.mock.services.MockApolloClient
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class EmailVerificationInterstitialFragmentViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: EmailVerificationInterstitialFragmentViewModel.ViewModel
    private val startEmailActivity = TestSubscriber.create<Void>()
    private val isSkipLinkShown = TestSubscriber.create<Boolean>()
    private val dismissInterstitial = TestSubscriber.create<Void>()
    private val showSnackbar = TestSubscriber.create<Int>()
    private val loadingIndicatorGone = TestSubscriber.create<Boolean>()

    private fun setUpEnvironment(envelope: AccessTokenEnvelope = AccessTokenEnvelopeFactory.envelope(),
                                 @NonNull environment: Environment) {

        this.vm = EmailVerificationInterstitialFragmentViewModel.ViewModel(environment)

        this.vm.outputs.startEmailActivity().subscribe(startEmailActivity)
        this.vm.outputs.isSkipLinkShown().subscribe(isSkipLinkShown)
        this.vm.outputs.dismissInterstitial().subscribe(dismissInterstitial)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.ENVELOPE, envelope)
        // - set up intent arguments
        this.vm.arguments(bundle)
        this.vm.outputs.showSnackbar().subscribe(showSnackbar)
        this.vm.outputs.loadingIndicatorGone().subscribe(loadingIndicatorGone)
    }

    @Test
    fun init_whenOpenEmailInboxPressedEmits_shouldEmitToStartEmailActivityStream() {
        setUpEnvironment(environment = environment())

        this.vm.inputs.openInboxButtonPressed()
        this.startEmailActivity.assertValue(null)
    }

    @Test
    fun isSkipLinkShown_whenFeatureFlagActive_shouldBeShown() {

        val mockConfig = MockCurrentConfig()
        mockConfig.config(ConfigFactory.configWithFeaturesEnabled(mapOf(
                Pair(EMAIL_VERIFICATION_SKIP, true)
        )))

        val environment = environment().toBuilder()
                .currentConfig(mockConfig)
                .build()
        val envelope = AccessTokenEnvelopeFactory.envelope(UserFactory.userNotVerifiedEmail(), "")

        setUpEnvironment(envelope, environment)

        this.isSkipLinkShown.assertValue(true)
    }

    @Test
    fun isSkipLinkShown_whenFeatureFlagNotActive_shouldBeShown() {

        val mockConfig = MockCurrentConfig()
        mockConfig.config(ConfigFactory.configWithFeaturesEnabled(mapOf(
                Pair(EMAIL_VERIFICATION_SKIP, second = false)
        )))

        val environment = environment().toBuilder()
                .currentConfig(mockConfig)
                .build()

        setUpEnvironment(environment = environment)

        this.isSkipLinkShown.assertValue(false)
    }

    @Test
    fun dismissInterstitial_whenSkipButtonPressed_dismissInterstitial() {
        setUpEnvironment(AccessTokenEnvelopeFactory.envelope(), environment())

        this.vm.inputs.skipButtonPressed()
        this.dismissInterstitial.assertValueCount(1)
    }

    @Test
    fun loggedInUser_whenNotVerifiedUser_userLoggedIn () {
        val user = UserFactory.userNotVerifiedEmail()
        val token = "Token"
        val envelope = AccessTokenEnvelopeFactory.envelope(user, token)

        setUpEnvironment(envelope, environment())

        this.vm.environment.currentUser().observable().subscribe {
            assertEquals(user, it)
        }

        this.vm.environment.currentUser().isLoggedIn.subscribe {
            assertTrue(it)
        }
    }

    @Test
    fun init_whenResendButtonPressedAndSuccessful_shouldEmitSuccesStateToShowSnackbarStreamAndResendEmail() {
        val environmentWithResendSuccess = environment()
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data> {
                        return Observable
                                .just(SendEmailVerificationMutation.Data(
                                        SendEmailVerificationMutation.UserSendEmailVerification(
                                                "",
                                                CLIENT_MUTATION_ID)))
                    }
                }).build()

        setUpEnvironment(AccessTokenEnvelopeFactory.envelope(), environmentWithResendSuccess)

        this.vm.inputs.resendEmailButtonPressed()
        this.loadingIndicatorGone.assertValueCount(2)
        this.loadingIndicatorGone.assertValues(false, true)
        this.showSnackbar.assertValue(R.string.verification_email_sent_inbox)
    }


    @Test
    fun init_whenResendButtonPressedAndError_shouldEmitErrorStateToShowSnackbarStream() {
        val environmentWithResendError = environment()
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data> {
                        return Observable.error(Throwable())
                    }
                }).build()

        setUpEnvironment(AccessTokenEnvelopeFactory.envelope(), environmentWithResendError)

        this.vm.inputs.resendEmailButtonPressed()
        this.loadingIndicatorGone.assertValueCount(2)
        this.loadingIndicatorGone.assertValues(false, true)
        this.showSnackbar.assertValue(R.string.we_couldnt_resend_this_email_please_try_again)
    }

    companion object {
        private const val CLIENT_MUTATION_ID = "1234"
    }
}
