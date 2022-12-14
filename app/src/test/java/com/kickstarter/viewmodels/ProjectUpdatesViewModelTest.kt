package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.EventName
import com.kickstarter.mock.factories.ProjectDataFactory.project
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.UpdateFactory.update
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.services.apiresponses.UpdatesEnvelope
import com.kickstarter.services.apiresponses.updatesresponse.UpdatesGraphQlEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ProjectData
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import rx.subjects.BehaviorSubject

class ProjectUpdatesViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ProjectUpdatesViewModel.ViewModel

    private val horizontalProgressBarIsGone = TestSubscriber<Boolean>()
    private val isFetchingUpdates = TestSubscriber<Boolean>()
    private val projectAndUpdates = TestSubscriber<Pair<Project, List<Update>>>()
    private val startUpdateActivity = TestSubscriber<Pair<Project, Update>>()

    private fun setUpEnvironment(env: Environment, project: Project, projectData: ProjectData) {
        vm = ProjectUpdatesViewModel.ViewModel(env)
        vm.outputs.horizontalProgressBarIsGone().subscribe(horizontalProgressBarIsGone)
        vm.outputs.isFetchingUpdates().subscribe(isFetchingUpdates)
        vm.outputs.projectAndUpdates().subscribe(projectAndUpdates)
        vm.outputs.startUpdateActivity().subscribe(startUpdateActivity)

        // Configure the view model with a project intent.
        vm.intent(
            Intent().putExtra(IntentKey.PROJECT, project)
                .putExtra(IntentKey.PROJECT_DATA, projectData)
        )
    }

    @Test
    fun init_whenViewModelInstantiated_shouldTrackPageViewEvent() {
        val project = project()
        setUpEnvironment(environment(), project, project(project))

        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testHorizontalProgressBarIsGone() {
        val project = project()
        setUpEnvironment(environment(), project, project(project))

        horizontalProgressBarIsGone.assertValues(false, true)
    }

    @Test
    fun testIsFetchingUpdates() {
        val project = project()
        setUpEnvironment(environment(), project, project(project))

        isFetchingUpdates.assertValues(true, false)
    }

    @Test
    fun testProjectAndUpdates() {
        val updates = listOf(
            update(),
            update()
        )
        val project = project()

        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun getProjectUpdates(
                    slug: String,
                    cursor: String?,
                    limit: Int
                ): Observable<UpdatesGraphQlEnvelope> {
                    return Observable.just(
                        UpdatesGraphQlEnvelope
                            .builder()
                            .updates(updates)
                            .build()
                    )
                }
            }).build(),
            project, project(project)
        )

        val projectAndUpdates = BehaviorSubject.create<Pair<Project, List<Update>>>()
        vm.outputs.projectAndUpdates().subscribe(projectAndUpdates)

        assertEquals(project, projectAndUpdates.value.first)
        assertEquals(updates[0], projectAndUpdates.value.second[0])
    }

    @Test
    fun test_projectAndUpdates_whenUpdatesListIsEmpty() {
        val project = project()

        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun getProjectUpdates(
                    slug: String,
                    cursor: String?,
                    limit: Int
                ): Observable<UpdatesGraphQlEnvelope> {
                    return Observable.just(
                        UpdatesGraphQlEnvelope
                            .builder()
                            .updates(emptyList())
                            .build()
                    )
                }
            }).build(),
            project, project(project)
        )

        val projectAndUpdates = BehaviorSubject.create<Pair<Project, List<Update>>>()
        vm.outputs.projectAndUpdates().subscribe(projectAndUpdates)

        assertEquals(project, projectAndUpdates.value.first)
        assertTrue(projectAndUpdates.value.second.isEmpty())

        isFetchingUpdates.assertValues(true, false)
        horizontalProgressBarIsGone.assertValues(false, true)
    }

    @Test
    fun testStartUpdateActivity() {
        val update = update()
        val updates = listOf(update)
        val project = project()

        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun getProjectUpdates(
                    slug: String,
                    cursor: String?,
                    limit: Int
                ): Observable<UpdatesGraphQlEnvelope> {
                    return Observable.just(
                        UpdatesGraphQlEnvelope
                            .builder()
                            .updates(updates)
                            .build()
                    )
                }
            }).build(),
            project, project(project)
        )

        vm.inputs.updateClicked(update)
        startUpdateActivity.assertValues(Pair.create(project, update))
    }

    private fun urlsEnvelope(): UpdatesEnvelope.UrlsEnvelope {
        return UpdatesEnvelope.UrlsEnvelope
            .builder()
            .api(
                UpdatesEnvelope.UrlsEnvelope.ApiEnvelope
                    .builder()
                    .moreUpdates("http://more.updates.please")
                    .build()
            )
            .build()
    }
}
