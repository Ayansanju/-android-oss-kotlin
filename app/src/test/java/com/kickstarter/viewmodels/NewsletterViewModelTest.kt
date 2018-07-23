package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.factories.UserFactory
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.models.User
import com.kickstarter.ui.data.Newsletter
import org.junit.Test
import rx.observers.TestSubscriber

class NewsletterViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: NewsletterViewModel.ViewModel
    private val currentUserTest = TestSubscriber<User>()
    private val showOptInPromptTest = TestSubscriber<Newsletter>()

    private fun setUpEnvironment( user : User) {
        val currentUser = MockCurrentUser(user)
        val environment = environment().toBuilder()
                .currentUser(currentUser)
                .build()

        currentUser.observable().subscribe(this.currentUserTest)

        this.vm = NewsletterViewModel.ViewModel(environment)
        this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)
    }

    @Test
    fun testUserEmits() {
        val user = UserFactory.user()

        setUpEnvironment(user)

        this.currentUserTest.assertValues(user)
    }

    @Test
    fun testSettingsViewModel_sendHappeningNewsletter() {
        val user = UserFactory.user().toBuilder().happeningNewsletter(false).build()

        setUpEnvironment(user)

        this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.sendHappeningNewsletter(true)
        this.koalaTest.assertValues("Newsletter Subscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().happeningNewsletter(true).build())

        this.vm.inputs.sendHappeningNewsletter(false)
        this.koalaTest.assertValues("Newsletter Subscribe", "Newsletter Unsubscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().happeningNewsletter(true).build(), user)

        this.showOptInPromptTest.assertNoValues()
    }

    @Test
    fun testSettingsViewModel_sendPromoNewsletter() {
        val user = UserFactory.user().toBuilder().promoNewsletter(false).build()

        setUpEnvironment(user)

        this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.sendPromoNewsletter(true)
        this.koalaTest.assertValues("Newsletter Subscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().promoNewsletter(true).build())

        this.vm.inputs.sendPromoNewsletter(false)
        this.koalaTest.assertValues("Newsletter Subscribe", "Newsletter Unsubscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().promoNewsletter(true).build(), user)

        this.showOptInPromptTest.assertNoValues()
    }

    @Test
    fun testSettingsViewModel_sendWeeklyNewsletter() {
        val user = UserFactory.user().toBuilder().weeklyNewsletter(false).build()

        setUpEnvironment(user)

        this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.sendWeeklyNewsletter(true)
        this.koalaTest.assertValues("Newsletter Subscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().weeklyNewsletter(true).build())

        this.vm.inputs.sendWeeklyNewsletter(false)
        this.koalaTest.assertValues("Newsletter Subscribe", "Newsletter Unsubscribe")
        this.currentUserTest.assertValues(user, user.toBuilder().weeklyNewsletter(true).build(), user)

        this.showOptInPromptTest.assertNoValues()
    }
}
