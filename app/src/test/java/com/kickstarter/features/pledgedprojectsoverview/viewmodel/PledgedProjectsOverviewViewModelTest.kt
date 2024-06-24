package com.kickstarter.features.pledgedprojectsoverview.viewmodel

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import io.reactivex.Observable
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PledgedProjectsOverviewViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: PledgedProjectsOverviewViewModel

    @Before
    fun setUpEnvrionment() {
        viewModel = PledgedProjectsOverviewViewModel.Factory(environment = environment())
            .create(PledgedProjectsOverviewViewModel::class.java)
    }

    @Test
    fun `emits_project_when_message_creator_called`() =
        runTest {
            val projectState = mutableListOf<Project>()

            val project = ProjectFactory.successfulProject()
            viewModel = PledgedProjectsOverviewViewModel.Factory(
                environment = environment().toBuilder()
                    .apolloClientV2(object : MockApolloClientV2() {
                        override fun getProject(slug: String): Observable<Project> {
                            return Observable.just(project)
                        }
                    }).build()
            ).create(PledgedProjectsOverviewViewModel::class.java)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.projectFlow.toList(projectState)
            }
            viewModel.onMessageCreatorClicked("test_project_slug")

            assertEquals(
                projectState.last(),
                project
            )
        }

    @Test
    fun `emits_error_when_message_creator_called`() =
        runTest {
            var snackbarAction = 0
            viewModel = PledgedProjectsOverviewViewModel.Factory(
                environment = environment().toBuilder()
                    .apolloClientV2(object : MockApolloClientV2() {
                        override fun getProject(slug: String): Observable<Project> {
                            return Observable.error(Throwable("error"))
                        }
                    }).build()
            ).create(PledgedProjectsOverviewViewModel::class.java)

            viewModel.provideSnackbarMessage { snackbarAction = it }
            viewModel.onMessageCreatorClicked("test_project_slug")

            // Should equal error string id
            assertEquals(
                snackbarAction,
                R.string.Something_went_wrong_please_try_again
            )
        }

    @Test
    fun `emits_snackbar_when_confirms_address`() =
        runTest {
            var snackbarAction = 0
            viewModel.provideSnackbarMessage { snackbarAction = it }
            viewModel.showSnackbarAndRefreshCardsList()

            // Should equal address confirmed string id
            assertEquals(
                snackbarAction,
                R.string.address_confirmed_snackbar_text
            )
        }
}
