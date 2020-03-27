package com.kickstarter.viewmodels

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.*
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.preferences.MockBooleanPreference
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.*
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.*
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class ProjectViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ProjectViewModel.ViewModel
    private val backingDetailsIsVisible = TestSubscriber<Boolean>()
    private val backingDetailsSubtitle = TestSubscriber<Either<String, Int>?>()
    private val backingDetailsTitle = TestSubscriber<Int>()
    private val expandPledgeSheet = TestSubscriber<Pair<Boolean, Boolean>>()
    private val goBack = TestSubscriber<Void>()
    private val heartDrawableId = TestSubscriber<Int>()
    private val horizontalProgressBarIsGone = TestSubscriber<Boolean>()
    private val managePledgeMenu = TestSubscriber<Int?>()
    private val pledgeActionButtonColor = TestSubscriber<Int>()
    private val pledgeActionButtonContainerIsGone = TestSubscriber<Boolean>()
    private val pledgeActionButtonText = TestSubscriber<Int>()
    private val pledgeContainerIsGone = TestSubscriber<Boolean>()
    private val pledgeToolbarNavigationIcon = TestSubscriber<Int>()
    private val pledgeToolbarTitle = TestSubscriber<Int>()
    private val prelaunchUrl = TestSubscriber<String>()
    private val projectActionButtonContainerIsGone = TestSubscriber<Boolean>()
    private val projectDataAndNativeCheckoutEnabled = TestSubscriber<Pair<ProjectData, Boolean>>()
    private val reloadProjectContainerIsGone = TestSubscriber<Boolean>()
    private val reloadProgressBarIsGone = TestSubscriber<Boolean>()
    private val revealRewardsFragment = TestSubscriber<Void>()
    private val savedTest = TestSubscriber<Boolean>()
    private val scrimIsVisible = TestSubscriber<Boolean>()
    private val setInitialRewardsContainerY = TestSubscriber<Void>()
    private val showCancelPledgeFragment = TestSubscriber<Project>()
    private val showCancelPledgeSuccess = TestSubscriber<Void>()
    private val showPledgeNotCancelableDialog = TestSubscriber<Void>()
    private val showSavedPromptTest = TestSubscriber<Void>()
    private val showShareSheet = TestSubscriber<Pair<String, String>>()
    private val showUpdatePledge = TestSubscriber<Pair<PledgeData, PledgeReason>>()
    private val showUpdatePledgeSuccess = TestSubscriber<Void>()
    private val startBackingActivity = TestSubscriber<Pair<Project, User>>()
    private val startCampaignWebViewActivity = TestSubscriber<ProjectData>()
    private val startCommentsActivity = TestSubscriber<Project>()
    private val startCreatorBioWebViewActivity = TestSubscriber<Project>()
    private val startCreatorDashboardActivity = TestSubscriber<Project>()
    private val startLoginToutActivity = TestSubscriber<Void>()
    private val startManagePledgeActivity = TestSubscriber<Project>()
    private val startMessagesActivity = TestSubscriber<Project>()
    private val startProjectUpdatesActivity = TestSubscriber<Project>()
    private val startThanksActivity = TestSubscriber<Pair<CheckoutData, PledgeData>>()
    private val startVideoActivity = TestSubscriber<Project>()
    private val updateFragments = TestSubscriber<ProjectData>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ProjectViewModel.ViewModel(environment)
        this.vm.outputs.backingDetailsIsVisible().subscribe(this.backingDetailsIsVisible)
        this.vm.outputs.backingDetailsSubtitle().subscribe(this.backingDetailsSubtitle)
        this.vm.outputs.backingDetailsTitle().subscribe(this.backingDetailsTitle)
        this.vm.outputs.expandPledgeSheet().subscribe(this.expandPledgeSheet)
        this.vm.outputs.goBack().subscribe(this.goBack)
        this.vm.outputs.heartDrawableId().subscribe(this.heartDrawableId)
        this.vm.outputs.horizontalProgressBarIsGone().subscribe(this.horizontalProgressBarIsGone)
        this.vm.outputs.managePledgeMenu().subscribe(this.managePledgeMenu)
        this.vm.outputs.pledgeActionButtonColor().subscribe(this.pledgeActionButtonColor)
        this.vm.outputs.pledgeActionButtonContainerIsGone().subscribe(this.pledgeActionButtonContainerIsGone)
        this.vm.outputs.pledgeActionButtonText().subscribe(this.pledgeActionButtonText)
        this.vm.outputs.pledgeContainerIsGone().subscribe(this.pledgeContainerIsGone)
        this.vm.outputs.pledgeToolbarNavigationIcon().subscribe(this.pledgeToolbarNavigationIcon)
        this.vm.outputs.pledgeToolbarTitle().subscribe(this.pledgeToolbarTitle)
        this.vm.outputs.prelaunchUrl().subscribe(this.prelaunchUrl)
        this.vm.outputs.projectActionButtonContainerIsGone().subscribe(this.projectActionButtonContainerIsGone)
        this.vm.outputs.projectDataAndNativeCheckoutEnabled().subscribe(this.projectDataAndNativeCheckoutEnabled)
        this.vm.outputs.reloadProgressBarIsGone().subscribe(this.reloadProgressBarIsGone)
        this.vm.outputs.reloadProjectContainerIsGone().subscribe(this.reloadProjectContainerIsGone)
        this.vm.outputs.revealRewardsFragment().subscribe(this.revealRewardsFragment)
        this.vm.outputs.scrimIsVisible().subscribe(this.scrimIsVisible)
        this.vm.outputs.setInitialRewardsContainerY().subscribe(this.setInitialRewardsContainerY)
        this.vm.outputs.showCancelPledgeFragment().subscribe(this.showCancelPledgeFragment)
        this.vm.outputs.showCancelPledgeSuccess().subscribe(this.showCancelPledgeSuccess)
        this.vm.outputs.showPledgeNotCancelableDialog().subscribe(this.showPledgeNotCancelableDialog)
        this.vm.outputs.showSavedPrompt().subscribe(this.showSavedPromptTest)
        this.vm.outputs.showShareSheet().subscribe(this.showShareSheet)
        this.vm.outputs.showUpdatePledge().subscribe(this.showUpdatePledge)
        this.vm.outputs.showUpdatePledgeSuccess().subscribe(this.showUpdatePledgeSuccess)
        this.vm.outputs.startLoginToutActivity().subscribe(this.startLoginToutActivity)
        this.vm.outputs.projectDataAndNativeCheckoutEnabled().map { pc -> pc.first.project().isStarred }.subscribe(this.savedTest)
        this.vm.outputs.startBackingActivity().subscribe(this.startBackingActivity)
        this.vm.outputs.startCampaignWebViewActivity().subscribe(this.startCampaignWebViewActivity)
        this.vm.outputs.startCommentsActivity().subscribe(this.startCommentsActivity)
        this.vm.outputs.startCreatorBioWebViewActivity().subscribe(this.startCreatorBioWebViewActivity)
        this.vm.outputs.startCreatorDashboardActivity().subscribe(this.startCreatorDashboardActivity)
        this.vm.outputs.startManagePledgeActivity().subscribe(this.startManagePledgeActivity)
        this.vm.outputs.startMessagesActivity().subscribe(this.startMessagesActivity)
        this.vm.outputs.startProjectUpdatesActivity().subscribe(this.startProjectUpdatesActivity)
        this.vm.outputs.startThanksActivity().subscribe(this.startThanksActivity)
        this.vm.outputs.startVideoActivity().subscribe(this.startVideoActivity)
        this.vm.outputs.updateFragments().subscribe(this.updateFragments)
    }

    @Test
    fun testUIOutputs_whenNativeCheckoutDisabled_andFetchProjectFromIntent_isSuccessful() {
        val currentConfig = MockCurrentConfig()
        currentConfig.config(ConfigFactory.config())

        val initialProject = ProjectFactory.initialProject()
        val refreshedProject = ProjectFactory.project()
        val environment = environment()
                .toBuilder()
                .apiClient(apiClientWithSuccessFetchingProject(refreshedProject))
                .currentConfig(currentConfig)
                .build()

        setUpEnvironment(environment)

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, initialProject))

        this.horizontalProgressBarIsGone.assertValues(false, true)
        this.pledgeActionButtonContainerIsGone.assertNoValues()
        this.pledgeContainerIsGone.assertValue(true)
        this.prelaunchUrl.assertNoValues()
        this.projectActionButtonContainerIsGone.assertValues(false)
        this.projectDataAndNativeCheckoutEnabled.assertValues(Pair(ProjectDataFactory.project(refreshedProject), false))
        this.reloadProjectContainerIsGone.assertNoValues()
        this.reloadProgressBarIsGone.assertNoValues()
        this.updateFragments.assertNoValues()
        this.koalaTest.assertValue(KoalaEvent.PROJECT_PAGE)
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testUIOutputs_whenNativeCheckoutDisabled_andFetchProjectFromIntent_isUnsuccessful() {
        val currentConfig = MockCurrentConfig()
        currentConfig.config(ConfigFactory.config())

        val environment = environment()
                .toBuilder()
                .apiClient(apiClientWithErrorFetchingProject())
                .currentConfig(currentConfig)
                .build()
        setUpEnvironment(environment)

        val projectWithNullRewards = ProjectFactory.project()
                .toBuilder()
                .rewards(null)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, projectWithNullRewards))

        this.horizontalProgressBarIsGone.assertValues(false, true)
        this.pledgeActionButtonContainerIsGone.assertNoValues()
        this.pledgeContainerIsGone.assertValue(true)
        this.prelaunchUrl.assertNoValues()
        this.projectActionButtonContainerIsGone.assertValues(false)
        this.projectDataAndNativeCheckoutEnabled.assertValues(Pair(ProjectDataFactory.project(projectWithNullRewards), false))
        this.reloadProjectContainerIsGone.assertNoValues()
        this.reloadProgressBarIsGone.assertNoValues()
        this.updateFragments.assertNoValues()
        this.koalaTest.assertNoValues()
        this.lakeTest.assertNoValues()
    }

    @Test
    fun testUIOutputs_whenNativeCheckoutEnabled_andFetchProjectFromIntent_isSuccessful() {
        val initialProject = ProjectFactory.initialProject()
        val refreshedProject = ProjectFactory.project()
        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(apiClientWithSuccessFetchingProject(refreshedProject))
                .build()

        setUpEnvironment(environment)

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, initialProject))

        this.horizontalProgressBarIsGone.assertNoValues()
        this.pledgeActionButtonContainerIsGone.assertValues(true, false)
        this.pledgeContainerIsGone.assertValue(false)
        this.prelaunchUrl.assertNoValues()
        this.projectActionButtonContainerIsGone.assertValue(true)
        this.projectDataAndNativeCheckoutEnabled.assertValues(Pair(ProjectDataFactory.project(refreshedProject), true))
        this.reloadProjectContainerIsGone.assertValue(true)
        this.reloadProgressBarIsGone.assertValues(false, true)
        this.updateFragments.assertValue(ProjectDataFactory.project(refreshedProject))
        this.koalaTest.assertValue(KoalaEvent.PROJECT_PAGE)
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testUIOutputs_whenNativeCheckoutEnabled_andFetchProjectFromIntent_isUnsuccessful() {
        var error = true
        val initialProject = ProjectFactory.initialProject()
        val refreshedProject = ProjectFactory.project()

        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(object : MockApiClient() {
                    override fun fetchProject(project: Project): Observable<Project> {
                        val observable = when {
                            error -> Observable.error(Throwable("boop"))
                            else -> {
                                Observable.just(refreshedProject)
                            }
                        }
                        return observable
                    }
                })
                .build()
        setUpEnvironment(environment)

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, initialProject))

        this.horizontalProgressBarIsGone.assertNoValues()
        this.pledgeActionButtonContainerIsGone.assertValues(true)
        this.pledgeContainerIsGone.assertValue(false)
        this.prelaunchUrl.assertNoValues()
        this.projectActionButtonContainerIsGone.assertValue(true)
        this.projectDataAndNativeCheckoutEnabled.assertValues(Pair(ProjectDataFactory.project(initialProject), true))
        this.reloadProjectContainerIsGone.assertValue(false)
        this.reloadProgressBarIsGone.assertValues(false, true)
        this.updateFragments.assertNoValues()

        error = false
        this.vm.inputs.reloadProjectContainerClicked()

        this.horizontalProgressBarIsGone.assertNoValues()
        this.pledgeActionButtonContainerIsGone.assertValues(true, false)
        this.pledgeContainerIsGone.assertValue(false)
        this.prelaunchUrl.assertNoValues()
        this.projectActionButtonContainerIsGone.assertValue(true)
        this.projectDataAndNativeCheckoutEnabled.assertValues(Pair(ProjectDataFactory.project(initialProject), true),
                Pair(ProjectDataFactory.project(initialProject), true),
                Pair(ProjectDataFactory.project(refreshedProject), true))
        this.reloadProjectContainerIsGone.assertValues(false, true, true)
        this.reloadProgressBarIsGone.assertValues(false, true, false, true)
        this.updateFragments.assertValue(ProjectDataFactory.project(refreshedProject))
        this.koalaTest.assertValue(KoalaEvent.PROJECT_PAGE)
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testUIOutputs_whenNativeCheckoutDisabled_andFetchProjectFromDeepLink_isSuccessful() {
        val project = ProjectFactory.project()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(ConfigFactory.config())

        val environment = environment().toBuilder()
                .currentConfig(currentConfig)
                .apiClient(object : MockApiClient(){
                    override fun fetchProject(param: String): Observable<Project> {
                        return Observable.just(project)
                    }
                })
                .build()

        setUpEnvironment(environment)
        val intent = deepLinkIntent()
        this.vm.intent(intent)

        this.horizontalProgressBarIsGone.assertValues(false, true)
        this.pledgeActionButtonContainerIsGone.assertNoValues()
        this.pledgeContainerIsGone.assertValue(true)
        this.prelaunchUrl.assertNoValues()
        this.projectActionButtonContainerIsGone.assertValues(false)
        this.projectDataAndNativeCheckoutEnabled.assertValues(Pair(ProjectDataFactory.project(project), false))
        this.reloadProjectContainerIsGone.assertNoValues()
        this.reloadProgressBarIsGone.assertNoValues()
        this.updateFragments.assertNoValues()
        this.koalaTest.assertValue(KoalaEvent.PROJECT_PAGE)
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testUIOutputs_whenNativeCheckoutDisabled_andFetchProjectFromDeepLink_isUnsuccessful() {
        val currentConfig = MockCurrentConfig()
        currentConfig.config(ConfigFactory.config())

        val environment = environment()
                .toBuilder()
                .apiClient(apiClientWithErrorFetchingProjectFromParam())
                .currentConfig(currentConfig)
                .build()

        setUpEnvironment(environment)

        this.vm.intent(deepLinkIntent())

        this.horizontalProgressBarIsGone.assertValues(false, true)
        this.pledgeActionButtonContainerIsGone.assertNoValues()
        this.pledgeContainerIsGone.assertValue(true)
        this.prelaunchUrl.assertNoValues()
        this.projectActionButtonContainerIsGone.assertValues(false)
        this.projectDataAndNativeCheckoutEnabled.assertNoValues()
        this.reloadProjectContainerIsGone.assertNoValues()
        this.reloadProgressBarIsGone.assertNoValues()
        this.updateFragments.assertNoValues()
        this.koalaTest.assertNoValues()
        this.lakeTest.assertNoValues()
    }

    @Test
    fun testUIOutputs_whenNativeCheckoutEnabled_andFetchProjectFromDeepLink_isSuccessful() {
        val project = ProjectFactory.project()

        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(object : MockApiClient(){
                    override fun fetchProject(param: String): Observable<Project> {
                        return Observable.just(project)
                    }
                })
                .build()

        setUpEnvironment(environment)
        val intent = deepLinkIntent()
        this.vm.intent(intent)

        this.horizontalProgressBarIsGone.assertNoValues()
        this.pledgeActionButtonContainerIsGone.assertValues(true, false)
        this.pledgeContainerIsGone.assertValue(false)
        this.prelaunchUrl.assertNoValues()
        this.projectActionButtonContainerIsGone.assertValue(true)
        this.projectDataAndNativeCheckoutEnabled.assertValue(Pair(ProjectDataFactory.project(project), true))
        this.reloadProgressBarIsGone.assertValues(false, true)
        this.updateFragments.assertValue(ProjectDataFactory.project(project))
        this.koalaTest.assertValue(KoalaEvent.PROJECT_PAGE)
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testUIOutputs_whenNativeCheckoutEnabled_andFetchProjectFromDeepLink_isUnsuccessful() {
        var error = true
        val refreshedProject = ProjectFactory.project()

        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(object : MockApiClient() {
                    override fun fetchProject(param: String): Observable<Project> {
                        val observable = when {
                            error -> Observable.error(Throwable("boop"))
                            else -> Observable.just(refreshedProject)
                        }
                        return observable
                    }
                })
                .build()
        setUpEnvironment(environment)

        this.vm.intent(deepLinkIntent())

        this.horizontalProgressBarIsGone.assertNoValues()
        this.pledgeActionButtonContainerIsGone.assertNoValues()
        this.pledgeContainerIsGone.assertValue(false)
        this.prelaunchUrl.assertNoValues()
        this.projectActionButtonContainerIsGone.assertValue(true)
        this.projectDataAndNativeCheckoutEnabled.assertNoValues()
        this.reloadProgressBarIsGone.assertValues(false, true)
        this.reloadProjectContainerIsGone.assertValue(false)
        this.updateFragments.assertNoValues()

        error = false
        this.vm.inputs.reloadProjectContainerClicked()

        this.horizontalProgressBarIsGone.assertNoValues()
        this.pledgeActionButtonContainerIsGone.assertValues(true, false)
        this.pledgeContainerIsGone.assertValue(false)
        this.prelaunchUrl.assertNoValues()
        this.projectActionButtonContainerIsGone.assertValue(true)
        this.projectDataAndNativeCheckoutEnabled.assertValue(Pair(ProjectDataFactory.project(refreshedProject), true))
        this.reloadProgressBarIsGone.assertValues(false, true, false, true)
        this.reloadProjectContainerIsGone.assertValues(false, true, true)
        this.updateFragments.assertValue(ProjectDataFactory.project(refreshedProject))
        this.koalaTest.assertValue(KoalaEvent.PROJECT_PAGE)
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testUIOutputs_whenNativeCheckoutDisabled_andFetchProjectReturnsPrelaunchActivatedProject() {
        val url = "https://www.kickstarter.com/projects/1186238668/skull-graphic-tee"
        val project = ProjectFactory.prelaunchProject(url)
        val currentConfig = MockCurrentConfig()
        currentConfig.config(ConfigFactory.config())

        val environment = environment().toBuilder()
                .currentConfig(currentConfig)
                .apiClient(object : MockApiClient(){
                    override fun fetchProject(param: String): Observable<Project> {
                        return Observable.just(project)
                    }
                })
                .build()

        setUpEnvironment(environment)
        val uri = Uri.parse(url)
        this.vm.intent(Intent(Intent.ACTION_VIEW, uri))

        this.horizontalProgressBarIsGone.assertValues(false, true)
        this.pledgeActionButtonContainerIsGone.assertNoValues()
        this.pledgeContainerIsGone.assertValue(true)
        this.prelaunchUrl.assertValue(url)
        this.projectActionButtonContainerIsGone.assertValues(false)
        this.projectDataAndNativeCheckoutEnabled.assertNoValues()
        this.reloadProgressBarIsGone.assertNoValues()
        this.reloadProjectContainerIsGone.assertNoValues()
        this.updateFragments.assertNoValues()
        this.koalaTest.assertNoValues()
        this.lakeTest.assertNoValues()
    }

    @Test
    fun testUIOutputs_whenNativeCheckoutEnabled_andFetchProjectReturnsPrelaunchActivatedProject() {
        val url = "https://www.kickstarter.com/projects/1186238668/skull-graphic-tee"
        val project = ProjectFactory.prelaunchProject(url)

        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(object : MockApiClient(){
                    override fun fetchProject(param: String): Observable<Project> {
                        return Observable.just(project)
                    }
                })
                .build()

        setUpEnvironment(environment)
        val uri = Uri.parse(url)
        this.vm.intent(Intent(Intent.ACTION_VIEW, uri))

        this.horizontalProgressBarIsGone.assertNoValues()
        this.pledgeActionButtonContainerIsGone.assertNoValues()
        this.pledgeContainerIsGone.assertValue(false)
        this.prelaunchUrl.assertValue(url)
        this.projectActionButtonContainerIsGone.assertValue(true)
        this.projectDataAndNativeCheckoutEnabled.assertNoValues()
        this.reloadProgressBarIsGone.assertValues(false, true)
        this.reloadProjectContainerIsGone.assertNoValues()
        this.updateFragments.assertNoValues()
        this.koalaTest.assertNoValues()
        this.lakeTest.assertNoValues()
    }

    @Test
    fun testLoggedOutStarProjectFlow() {
        val currentUser = MockCurrentUser()
        val environment = environment().toBuilder()
                .currentUser(currentUser)
                .build()
        environment.currentConfig().config(ConfigFactory.config())

        setUpEnvironment(environment)

        // Start the view model with a project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.halfWayProject()))

        this.savedTest.assertValues(false)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline)

        // Try starring while logged out
        this.vm.inputs.heartButtonClicked()

        // The project shouldn't be saved, and a login prompt should be shown.
        this.savedTest.assertValues(false)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline)
        this.showSavedPromptTest.assertValueCount(0)
        this.startLoginToutActivity.assertValueCount(1)

        // A koala event for starring should NOT be tracked
        this.koalaTest.assertValues(KoalaEvent.PROJECT_PAGE)

        // Login
        currentUser.refresh(UserFactory.user())

        // The project should be saved, and a star prompt should be shown.
        this.savedTest.assertValues(false, true)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline, R.drawable.icon__heart)
        this.showSavedPromptTest.assertValueCount(1)

        // A koala event for starring should be tracked
        this.koalaTest.assertValues(
                KoalaEvent.PROJECT_PAGE, KoalaEvent.PROJECT_STAR, KoalaEvent.STARRED_PROJECT
        )
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testShowShareSheet() {
        val creator = UserFactory.creator()
        val slug = "best-project-2k19"
        val projectUrl = "https://www.kck.str/projects/" + creator.id().toString() + "/" + slug

        val webUrls = Project.Urls.Web.builder()
                .project(projectUrl)
                .rewards("$projectUrl/rewards")
                .updates("$projectUrl/posts")
                .build()

        val project = ProjectFactory.project()
                .toBuilder()
                .name("Best Project 2K19")
                .urls(Project.Urls.builder().web(webUrls).build())
                .build()

        setUpEnvironment(environment())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.shareButtonClicked()
        val expectedName = "Best Project 2K19"
        val expectedShareUrl = "https://www.kck.str/projects/" + creator.id().toString() + "/" + slug + "?ref=android_project_share"
        this.showShareSheet.assertValues(Pair(expectedName, expectedShareUrl))
        this.koalaTest.assertValues(
                KoalaEvent.PROJECT_PAGE, KoalaEvent.PROJECT_SHOW_SHARE_SHEET_LEGACY, KoalaEvent.SHOWED_SHARE_SHEET
        )
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testStarProjectThatIsAlmostCompleted() {
        val project = ProjectFactory.almostCompletedProject()

        val currentUser = MockCurrentUser()
        val environment = environment().toBuilder()
                .currentUser(currentUser)
                .build()
        environment.currentConfig().config(ConfigFactory.config())

        setUpEnvironment(environment)

        // Start the view model with an almost completed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // Login
        currentUser.refresh(UserFactory.user())

        // Star the project
        this.vm.inputs.heartButtonClicked()

        // The project should be saved, and a save prompt should NOT be shown.
        this.savedTest.assertValues(false, true)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline, R.drawable.icon__heart)
        this.showSavedPromptTest.assertValueCount(0)
    }

    @Test
    fun testSaveProjectThatIsSuccessful() {
        val currentUser = MockCurrentUser()
        val environment = environment().toBuilder()
                .currentUser(currentUser)
                .build()
        environment.currentConfig().config(ConfigFactory.config())

        setUpEnvironment(environment)

        // Start the view model with a successful project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()))

        // Login
        currentUser.refresh(UserFactory.user())

        // Star the project
        this.vm.inputs.heartButtonClicked()

        // The project should be saved, and a save prompt should NOT be shown.
        this.savedTest.assertValues(false, true)
        this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline, R.drawable.icon__heart)
        this.showSavedPromptTest.assertValueCount(0)
    }

    @Test
    fun testStartBackingActivity() {
        val project = ProjectFactory.project()
        val user = UserFactory.user()

        setUpEnvironment(environment().toBuilder().currentUser(MockCurrentUser(user)).build())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.viewPledgeButtonClicked()
        this.startBackingActivity.assertValues(Pair.create(project, user))
    }

    @Test
    fun testStartCampaignWebViewActivity_whenBlurbClicked_liveNotBackedProject() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.blurbTextViewClicked()
        this.startCampaignWebViewActivity.assertValues(ProjectDataFactory.project(project))
        this.experimentsTest.assertValue("Campaign Details Button Clicked")
    }

    @Test
    fun testStartCampaignWebViewActivity_whenBlurbClicked_backedProject() {
        val project = ProjectFactory.backedProject()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.blurbTextViewClicked()
        this.startCampaignWebViewActivity.assertValues(ProjectDataFactory.project(project))
        this.experimentsTest.assertNoValues()
    }

    @Test
    fun testStartCampaignWebViewActivity_whenBlurbClicked_endedProject() {
        val project = ProjectFactory.successfulProject()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.blurbTextViewClicked()
        this.startCampaignWebViewActivity.assertValues(ProjectDataFactory.project(project))
        this.experimentsTest.assertNoValues()
    }

    @Test
    fun testStartCampaignWebViewActivity_whenBlurbVariantClicked_liveNotBackedProject() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.blurbVariantClicked()
        this.startCampaignWebViewActivity.assertValues(ProjectDataFactory.project(project))
        this.experimentsTest.assertValue("Campaign Details Button Clicked")
    }

    @Test
    fun testStartCampaignWebViewActivity_whenBlurbVariantClicked_backedProject() {
        val project = ProjectFactory.backedProject()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.blurbVariantClicked()
        this.startCampaignWebViewActivity.assertValues(ProjectDataFactory.project(project))
        this.experimentsTest.assertNoValues()
    }

    @Test
    fun testStartCampaignWebViewActivity_whenBlurbVariantClicked_endedProject() {
        val project = ProjectFactory.successfulProject()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.blurbVariantClicked()
        this.startCampaignWebViewActivity.assertValues(ProjectDataFactory.project(project))
        this.experimentsTest.assertNoValues()
    }

    @Test
    fun testStartCreatorBioWebViewActivity_whenClickingControlCreatorDetails_liveNotBackedProject() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.creatorNameTextViewClicked()
        this.startCreatorBioWebViewActivity.assertValues(project)
        this.experimentsTest.assertValue("Creator Details Clicked")
    }

    @Test
    fun testStartCreatorBioWebViewActivity_whenClickingControlCreatorDetails_backedProject() {
        val project = ProjectFactory.backedProject()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.creatorNameTextViewClicked()
        this.startCreatorBioWebViewActivity.assertValues(project)
        this.experimentsTest.assertNoValues()
    }

    @Test
    fun testStartCreatorBioWebViewActivity_whenClickingControlCreatorDetails_endedProject() {
        val project = ProjectFactory.successfulProject()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.creatorNameTextViewClicked()
        this.startCreatorBioWebViewActivity.assertValues(project)
        this.experimentsTest.assertNoValues()
    }

    @Test
    fun testStartCreatorBioWebViewActivity_whenClickingVariantCreatorDetails_liveNotBackedProject() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.creatorInfoVariantClicked()
        this.startCreatorBioWebViewActivity.assertValues(project)
        this.experimentsTest.assertValue("Creator Details Clicked")
    }

    @Test
    fun testStartCreatorBioWebViewActivity_whenClickingVariantCreatorDetails_backedProject() {
        val project = ProjectFactory.backedProject()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.creatorInfoVariantClicked()
        this.startCreatorBioWebViewActivity.assertValues(project)
        this.experimentsTest.assertNoValues()
    }

    @Test
    fun testStartCreatorBioWebViewActivity_whenClickingVariantCreatorDetails_endedProject() {
        val project = ProjectFactory.successfulProject()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.creatorInfoVariantClicked()
        this.startCreatorBioWebViewActivity.assertValues(project)
        this.experimentsTest.assertNoValues()
    }

    @Test
    fun testStartCreatorDashboardActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.creatorDashboardButtonClicked()
        this.startCreatorDashboardActivity.assertValues(project)
    }

    @Test
    fun testStartCommentsActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.commentsTextViewClicked()
        this.startCommentsActivity.assertValues(project)
    }

    @Test
    fun testStartManagePledgeActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // Click on Manage pledge button.
        this.vm.inputs.managePledgeButtonClicked()
        this.startManagePledgeActivity.assertValues(project)
    }

    @Test
    fun testStartProjectUpdatesActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // Click on Updates button.
        this.vm.inputs.updatesTextViewClicked()
        this.startProjectUpdatesActivity.assertValues(project)
    }

    @Test
    fun testStartVideoActivity() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Start the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.vm.inputs.playVideoButtonClicked()
        this.startVideoActivity.assertValues(project)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenNativeCheckoutDisabled() {
        setUpEnvironment(environment())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeActionButtonColor.assertNoValues()
        this.pledgeActionButtonText.assertNoValues()
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenNativeCheckoutEnabled_whenProjectIsLiveAndBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.pledgeActionButtonColor.assertValuesAndClear(R.color.button_pledge_manage)
        this.pledgeActionButtonText.assertValuesAndClear(R.string.Manage)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenNativeCheckoutEnabled_projectIsLiveAndNotBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_live)
        this.pledgeActionButtonText.assertValue(R.string.Back_this_project)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenNativeCheckoutEnabled_projectIsLiveAndNotBacked_control() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_live)
        this.pledgeActionButtonText.assertValue(R.string.Back_this_project)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenNativeCheckoutEnabled_projectIsLiveAndNotBacked_variant1() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled().toBuilder()
                .optimizely(MockExperimentsClientType(OptimizelyExperiment.Variant.VARIANT_1))
                .build())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_live)
        this.pledgeActionButtonText.assertValue(R.string.See_the_rewards)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenNativeCheckoutEnabled_projectIsLiveAndNotBacked_variant2() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled().toBuilder()
                .optimizely(MockExperimentsClientType(OptimizelyExperiment.Variant.VARIANT_2))
                .build())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_live)
        this.pledgeActionButtonText.assertValue(R.string.View_the_rewards)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenNativeCheckoutEnabled_whenProjectIsEndedAndBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        val backedSuccessfulProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedSuccessfulProject))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_ended)
        this.pledgeActionButtonText.assertValue(R.string.View_your_pledge)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenNativeCheckoutEnabled_whenProjectIsEndedAndNotBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_ended)
        this.pledgeActionButtonText.assertValuesAndClear(R.string.View_rewards)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenNativeCheckoutEnabled_whenCurrentUserIsProjectCreator() {
        val creator = UserFactory.creator()
        val creatorProject = ProjectFactory.project()
                .toBuilder()
                .creator(creator)
                .build()
        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .currentUser(MockCurrentUser(creator))
                .build()
        setUpEnvironment(environment)

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, creatorProject))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_ended)
        this.pledgeActionButtonText.assertValuesAndClear(R.string.View_your_rewards)
    }

    @Test
    fun testPledgeActionButtonUIOutputs_whenNativeCheckoutEnabled_whenBackingIsErrored() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        val backedSuccessfulProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(BackingFactory.backing(Backing.STATUS_ERRORED))
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedSuccessfulProject))

        this.pledgeActionButtonColor.assertValue(R.color.button_pledge_error)
        this.pledgeActionButtonText.assertValue(R.string.Manage)
    }

    @Test
    fun testPledgeToolbarNavigationIcon_whenNativeCheckoutDisabled() {
        setUpEnvironment(environment())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeToolbarNavigationIcon.assertNoValues()
    }

    @Test
    fun testPledgeToolbarNavigationIcon_whenNativeCheckoutEnabled() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeToolbarNavigationIcon.assertValue(R.drawable.ic_arrow_down)

        this.vm.inputs.fragmentStackCount(1)

        this.pledgeToolbarNavigationIcon.assertValues(R.drawable.ic_arrow_down, R.drawable.ic_arrow_back)

        this.vm.inputs.fragmentStackCount(0)

        this.pledgeToolbarNavigationIcon.assertValues(R.drawable.ic_arrow_down, R.drawable.ic_arrow_back, R.drawable.ic_arrow_down)
    }

    @Test
    fun testPledgeToolbarTitle_whenNativeCheckoutDisabled() {
        setUpEnvironment(environment())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeToolbarTitle.assertNoValues()
    }

    @Test
    fun testPledgeToolbarTitle_whenNativeCheckoutEnabled_projectIsLiveAndUnbacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.pledgeToolbarTitle.assertValue(R.string.Back_this_project)
    }

    @Test
    fun testPledgeToolbarTitle_whenNativeCheckoutEnabled_projectIsLiveAndBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.pledgeToolbarTitle.assertValue(R.string.Manage_your_pledge)
    }

    @Test
    fun testPledgeToolbarTitle_whenNativeCheckoutEnabled_projectIsEndedAndUnbacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()))

        this.pledgeToolbarTitle.assertValuesAndClear(R.string.View_rewards)
    }

    @Test
    fun testPledgeToolbarTitle_whenNativeCheckoutEnabled_projectIsEndedAndBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        val backedSuccessfulProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedSuccessfulProject))

        this.pledgeToolbarTitle.assertValue(R.string.View_your_pledge)
    }

    @Test
    fun testExpandPledgeSheet_whenCollapsingSheet() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.expandPledgeSheet.assertValue(Pair(true, true))

        this.vm.inputs.pledgeToolbarNavigationClicked()
        this.expandPledgeSheet.assertValues(Pair(true, true), Pair(false, true))
        this.goBack.assertNoValues()
        this.koalaTest.assertValues("Project Page", "Back this Project Button Clicked")
        this.lakeTest.assertValues("Project Page Viewed", "Project Page Pledge Button Clicked")
        this.experimentsTest.assertValues("Project Page Pledge Button Clicked")
    }

    @Test
    fun testExpandPledgeSheet_whenProjectLiveAndNotBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.koalaTest.assertValues("Project Page", "Back this Project Button Clicked")
        this.lakeTest.assertValues("Project Page Viewed", "Project Page Pledge Button Clicked")
        this.experimentsTest.assertValues("Project Page Pledge Button Clicked")
    }

    @Test
    fun testExpandPledgeSheet_whenProjectLiveAndBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.koalaTest.assertValues("Project Page", "Manage Pledge Button Clicked")
        this.lakeTest.assertValue("Project Page Viewed")
        this.experimentsTest.assertNoValues()
    }

    @Test
    fun testExpandPledgeSheet_whenProjectEndedAndNotBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.koalaTest.assertValues("Project Page", "View Rewards Button Clicked")
        this.lakeTest.assertValue("Project Page Viewed")
        this.experimentsTest.assertNoValues()
    }

    @Test
    fun testExpandPledgeSheet_whenProjectEndedAndBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedSuccessfulProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.koalaTest.assertValues("Project Page", "View Your Pledge Button Clicked")
        this.lakeTest.assertValue("Project Page Viewed")
        this.experimentsTest.assertNoValues()
    }

    @Test
    fun testExpandPledgeSheet_whenComingBackFromProjectPage_OKResult() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.activityResult(ActivityResult.create(ActivityRequestCodes.SHOW_REWARDS, Activity.RESULT_OK, null))

        this.expandPledgeSheet.assertValue(Pair(true, true))
        this.koalaTest.assertValues("Project Page")
        this.lakeTest.assertValue("Project Page Viewed")
        this.experimentsTest.assertNoValues()
    }

    @Test
    fun testExpandPledgeSheet_whenComingBackFromProjectPage_CanceledResult() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.activityResult(ActivityResult.create(ActivityRequestCodes.SHOW_REWARDS, Activity.RESULT_CANCELED, null))

        this.expandPledgeSheet.assertNoValues()
        this.koalaTest.assertValue("Project Page")
        this.lakeTest.assertValue("Project Page Viewed")
        this.experimentsTest.assertNoValues()
    }

    @Test
    fun testGoBack_whenFragmentBackStackIsEmpty() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.pledgeToolbarNavigationClicked()
        this.goBack.assertNoValues()
    }

    @Test
    fun testGoBack_whenFragmentBackStackIsNotEmpty() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.fragmentStackCount(3)
        this.vm.inputs.pledgeToolbarNavigationClicked()
        this.goBack.assertValueCount(1)

        this.vm.inputs.fragmentStackCount(2)
        this.vm.inputs.pledgeToolbarNavigationClicked()
        this.goBack.assertValueCount(2)
        this.expandPledgeSheet.assertNoValues()
    }

    @Test
    fun testSetInitialRewardsContainerY() {
        setUpEnvironment(environment())
        this.vm.inputs.onGlobalLayout()
        this.setInitialRewardsContainerY.assertValueCount(1)
    }

    @Test
    fun testBackingDetails_whenProjectNotBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))
        this.backingDetailsIsVisible.assertValue(false)
        this.backingDetailsSubtitle.assertNoValues()
        this.backingDetailsTitle.assertNoValues()
    }

    @Test
    fun testBackingDetails_whenShippableRewardBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        val reward = RewardFactory.reward()
                .toBuilder()
                .id(4)
                .build()

        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(34.0)
                .shippingAmount(4f)
                .rewardId(4)
                .build()

        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .rewards(listOf(reward))
                .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))
        this.backingDetailsIsVisible.assertValue(true)
        this.backingDetailsSubtitle.assertValue(Either.Left("$34 • Digital Bundle"))
        this.backingDetailsTitle.assertValue(R.string.Youre_a_backer)
    }

    @Test
    fun testBackingDetails_whenDigitalReward() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        val noRewardBacking = BackingFactory.backing()
                .toBuilder()
                .amount(13.5)
                .reward(RewardFactory.noReward())
                .build()

        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(noRewardBacking)
                .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))
        this.backingDetailsIsVisible.assertValue(true)
        this.backingDetailsSubtitle.assertValue(Either.Left("$13.50"))
        this.backingDetailsTitle.assertValue(R.string.Youre_a_backer)
    }

    @Test
    fun testBackingDetails_whenBackingIsErrored() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        val backedSuccessfulProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(BackingFactory.backing(Backing.STATUS_ERRORED))
                .state(Project.STATE_SUCCESSFUL)
                .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedSuccessfulProject))
        this.backingDetailsIsVisible.assertValue(true)
        this.backingDetailsSubtitle.assertValue(Either.Right(R.string.We_cant_process_your_pledge))
        this.backingDetailsTitle.assertValue(R.string.Payment_failure)
    }

    @Test
    fun testScrimIsVisible_whenNativeCheckoutDisabled() {
        setUpEnvironment(environment())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.fragmentStackCount(0)
        this.scrimIsVisible.assertNoValues()
    }

    @Test
    fun testScrimIsVisible_whenNotBackedProject() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.vm.inputs.fragmentStackCount(0)
        this.scrimIsVisible.assertValue(false)

        this.vm.inputs.fragmentStackCount(1)
        this.scrimIsVisible.assertValue(false)

        this.vm.inputs.fragmentStackCount(2)
        this.scrimIsVisible.assertValues(false, true)

        this.vm.inputs.fragmentStackCount(1)
        this.scrimIsVisible.assertValues(false, true, false)
    }

    @Test
    fun testScrimIsVisible_whenBackedProject() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.vm.inputs.fragmentStackCount(0)
        this.scrimIsVisible.assertValue(false)

        this.vm.inputs.fragmentStackCount(1)
        this.scrimIsVisible.assertValue(false)

        this.vm.inputs.fragmentStackCount(2)
        this.scrimIsVisible.assertValues(false)

        this.vm.inputs.fragmentStackCount(3)
        this.scrimIsVisible.assertValues(false, true)

        this.vm.inputs.fragmentStackCount(2)
        this.scrimIsVisible.assertValues(false, true, false)
    }

    @Test
    fun testCancelPledgeSuccess() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.projectDataAndNativeCheckoutEnabled.assertValueCount(1)

        this.vm.inputs.pledgeSuccessfullyCancelled()
        this.expandPledgeSheet.assertValue(Pair(false, false))
        this.showCancelPledgeSuccess.assertValueCount(1)
        this.projectDataAndNativeCheckoutEnabled.assertValueCount(2)
    }

    @Test
    fun testManagePledgeMenu_whenProjectBackedAndLive_backingIsPledged() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.managePledgeMenu.assertValue(R.menu.manage_pledge_live)
    }

    @Test
    fun testManagePledgeMenu_whenProjectBackedAndLive_backingIsPreauth() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        val backing = BackingFactory.backing()
                .toBuilder()
                .status(Backing.STATUS_PREAUTH)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.managePledgeMenu.assertValue(R.menu.manage_pledge_preauth)
    }

    @Test
    fun testManagePledgeMenu_whenProjectBackedAndNotLive() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        val successfulBackedProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, successfulBackedProject))

        this.managePledgeMenu.assertValue(R.menu.manage_pledge_ended)
    }

    @Test
    fun testManagePledgeMenu_whenProjectNotBacked() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        this.managePledgeMenu.assertValue(null)
    }

    @Test
    fun testManagePledgeMenu_whenManaging() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.managePledgeMenu.assertValue(R.menu.manage_pledge_live)

        this.vm.inputs.cancelPledgeClicked()
        this.vm.inputs.fragmentStackCount(1)
        this.managePledgeMenu.assertValues(R.menu.manage_pledge_live, null)

        this.vm.inputs.fragmentStackCount(0)
        this.managePledgeMenu.assertValues(R.menu.manage_pledge_live, null, R.menu.manage_pledge_live)
    }

    @Test
    fun testShowCancelPledgeFragment_whenBackingIsCancelable() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        val backing = BackingFactory.backing()
                .toBuilder()
                .cancelable(true)
                .build()

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.cancelPledgeClicked()

        this.showCancelPledgeFragment.assertValue(backedProject)
        this.koalaTest.assertValues("Project Page", "Manage Pledge Button Clicked", "Manage Pledge Option Clicked")
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testShowCancelPledgeFragment_whenBackingIsNotCancelable() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        val backing = BackingFactory.backing()
                .toBuilder()
                .cancelable(false)
                .build()

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.cancelPledgeClicked()

        this.showCancelPledgeFragment.assertNoValues()
        this.koalaTest.assertValues("Project Page", "Manage Pledge Button Clicked", "Manage Pledge Option Clicked")
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testShowConversation() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.contactCreatorClicked()

        this.startMessagesActivity.assertValue(backedProject)
        this.koalaTest.assertValues("Project Page", "Manage Pledge Button Clicked", "Manage Pledge Option Clicked")
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testShowPledgeNotCancelableDialog_whenBackingIsCancelable() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        val backing = BackingFactory.backing()
                .toBuilder()
                .cancelable(true)
                .build()

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.cancelPledgeClicked()
        this.showPledgeNotCancelableDialog.assertNoValues()
    }

    @Test
    fun testShowPledgeNotCancelableDialog_whenBackingIsNotCancelable() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())
        val backing = BackingFactory.backing()
                .toBuilder()
                .cancelable(false)
                .build()

        // Start the view model with a backed project
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.cancelPledgeClicked()
        this.showPledgeNotCancelableDialog.assertValueCount(1)
    }

    @Test
    fun testRevealRewardsFragment_whenBackedProjectLive() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.viewRewardsClicked()

        this.revealRewardsFragment.assertValueCount(1)
        this.koalaTest.assertValues("Project Page", "Manage Pledge Button Clicked", "Manage Pledge Option Clicked")
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testRevealRewardsFragment_whenBackedProjectEnded() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedSuccessfulProject()))

        this.vm.inputs.nativeProjectActionButtonClicked()
        this.vm.inputs.viewRewardsClicked()

        this.revealRewardsFragment.assertValueCount(1)
        this.koalaTest.assertValues("Project Page", "View Your Pledge Button Clicked", "Manage Pledge Option Clicked")
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testShowUpdatePledge_whenFixingPaymentMethod() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        val reward = RewardFactory.reward()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(BackingFactory.backing()
                        .toBuilder()
                        .rewardId(reward.id())
                        .build())
                .rewards(listOf(RewardFactory.noReward(), reward))
                .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.fixPaymentMethodButtonClicked()

        this.showUpdatePledge.assertValuesAndClear(Pair(PledgeData.builder()
                .pledgeFlowContext(PledgeFlowContext.MANAGE_REWARD)
                .reward(reward)
                .projectData(ProjectDataFactory.project(backedProject))
                .build(), PledgeReason.FIX_PLEDGE))
        this.koalaTest.assertValue("Project Page")
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testShowUpdatePledge_whenUpdatingPledge() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        val reward = RewardFactory.reward()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(BackingFactory.backing()
                        .toBuilder()
                        .rewardId(reward.id())
                        .build())
                .rewards(listOf(RewardFactory.noReward(), reward))
                .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.updatePledgeClicked()

        this.showUpdatePledge.assertValuesAndClear(Pair(PledgeData.builder()
                .pledgeFlowContext(PledgeFlowContext.MANAGE_REWARD)
                .reward(reward)
                .projectData(ProjectDataFactory.project(backedProject))
                .build(), PledgeReason.UPDATE_PLEDGE))
        this.koalaTest.assertValues("Project Page", "Manage Pledge Option Clicked")
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testShowUpdatePledge_whenUpdatingPaymentMethod() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        val reward = RewardFactory.reward()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(BackingFactory.backing()
                        .toBuilder()
                        .rewardId(reward.id())
                        .build())
                .rewards(listOf(RewardFactory.noReward(), reward))
                .build()

        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, backedProject))

        this.vm.inputs.updatePaymentClicked()

        this.showUpdatePledge.assertValuesAndClear(Pair(PledgeData.builder()
                .pledgeFlowContext(PledgeFlowContext.MANAGE_REWARD)
                .reward(reward)
                .projectData(ProjectDataFactory.project(backedProject))
                .build(), PledgeReason.UPDATE_PAYMENT))
        this.koalaTest.assertValues("Project Page", "Manage Pledge Option Clicked")
        this.lakeTest.assertValue("Project Page Viewed")
    }

    @Test
    fun testShowUpdatePledgeSuccess_whenUpdatingPayment() {
        val initialBackedProject = ProjectFactory.backedProject()
        val refreshedProject = ProjectFactory.backedProject()
        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(apiClientWithSuccessFetchingProjectFromSlug(refreshedProject))
                .build()
        setUpEnvironment(environment)

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, initialBackedProject))

        this.projectDataAndNativeCheckoutEnabled.assertValues(Pair(ProjectDataFactory.project(initialBackedProject), true))
        this.showUpdatePledgeSuccess.assertNoValues()
        this.updateFragments.assertValue(ProjectDataFactory.project(initialBackedProject))

        this.vm.inputs.pledgePaymentSuccessfullyUpdated()
        this.projectDataAndNativeCheckoutEnabled.assertValues(Pair(ProjectDataFactory.project(initialBackedProject), true),
                Pair(ProjectDataFactory.project(refreshedProject), true))
        this.showUpdatePledgeSuccess.assertValueCount(1)
        this.updateFragments.assertValues(ProjectDataFactory.project(initialBackedProject),
                ProjectDataFactory.project(refreshedProject))
    }

    @Test
    fun testShowUpdatePledgeSuccess_whenUpdatingPledge() {
        val initialBackedProject = ProjectFactory.backedProject()
        val refreshedProject = ProjectFactory.backedProject()
        val environment = environmentWithNativeCheckoutEnabled()
                .toBuilder()
                .apiClient(apiClientWithSuccessFetchingProjectFromSlug(refreshedProject))
                .build()
        setUpEnvironment(environment)

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, initialBackedProject))

        this.projectDataAndNativeCheckoutEnabled.assertValues(Pair(ProjectDataFactory.project(initialBackedProject), true))
        this.showUpdatePledgeSuccess.assertNoValues()
        this.updateFragments.assertValue(ProjectDataFactory.project(initialBackedProject))

        this.vm.inputs.pledgeSuccessfullyUpdated()
        this.projectDataAndNativeCheckoutEnabled.assertValues(Pair(ProjectDataFactory.project(initialBackedProject), true),
                Pair(ProjectDataFactory.project(refreshedProject), true))
        this.showUpdatePledgeSuccess.assertValueCount(1)
        this.updateFragments.assertValues(ProjectDataFactory.project(initialBackedProject),
                ProjectDataFactory.project(refreshedProject))
    }

    @Test
    fun testStartThanksActivity() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a unbacked project
        val project = ProjectFactory.project()
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        this.projectDataAndNativeCheckoutEnabled.assertValueCount(1)

        val checkoutData = CheckoutDataFactory.checkoutData(3L, 20.0, 30.0)
        val pledgeData = PledgeData.with(PledgeFlowContext.NEW_PLEDGE, ProjectDataFactory.project(project), RewardFactory.reward())
        this.vm.inputs.pledgeSuccessfullyCreated(Pair(checkoutData, pledgeData))
        this.expandPledgeSheet.assertValue(Pair(false, false))
        this.startThanksActivity.assertValue(Pair(checkoutData, pledgeData))
        this.projectDataAndNativeCheckoutEnabled.assertValueCount(2)
    }

    @Test
    fun testProjectAndNativeCheckoutEnabled_whenRefreshProjectIsCalled() {
        setUpEnvironment(environmentWithNativeCheckoutEnabled())

        // Start the view model with a backed project
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        this.projectDataAndNativeCheckoutEnabled.assertValueCount(1)

        this.vm.inputs.refreshProject()
        this.projectDataAndNativeCheckoutEnabled.assertValueCount(2)
    }

    private fun apiClientWithErrorFetchingProject(): MockApiClient {
        return object : MockApiClient() {
            override fun fetchProject(project: Project): Observable<Project> {
                return Observable.error(Throwable("boop"))
            }
        }
    }

    private fun apiClientWithSuccessFetchingProject(refreshedProject: Project): MockApiClient {
        return object : MockApiClient() {
            override fun fetchProject(project: Project): Observable<Project> {
                return Observable.just(refreshedProject)
            }
        }
    }

    private fun apiClientWithSuccessFetchingProjectFromSlug(refreshedProject: Project): MockApiClient {
        return object : MockApiClient() {
            override fun fetchProject(slug: String): Observable<Project> {
                return Observable.just(refreshedProject)
            }
        }
    }

    private fun apiClientWithErrorFetchingProjectFromParam(): MockApiClient {
        return object : MockApiClient() {
            override fun fetchProject(param: String): Observable<Project> {
                return Observable.error(Throwable("boop"))
            }
        }
    }

    private fun deepLinkIntent(): Intent {
        val uri = Uri.parse("https://www.kickstarter.com/projects/1186238668/skull-graphic-tee")
        return Intent(Intent.ACTION_VIEW, uri)
    }

    private fun environmentWithNativeCheckoutEnabled() : Environment {
        val currentConfig = MockCurrentConfig()
        currentConfig.config(ConfigFactory.configWithFeatureEnabled(FeatureKey.ANDROID_NATIVE_CHECKOUT))

        return environment()
                .toBuilder()
                .currentConfig(currentConfig)
                .nativeCheckoutPreference(MockBooleanPreference(true))
                .build()
    }
}
