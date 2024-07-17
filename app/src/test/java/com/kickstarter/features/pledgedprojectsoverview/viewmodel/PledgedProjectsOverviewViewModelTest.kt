package com.kickstarter.features.pledgedprojectsoverview.viewmodel

import androidx.paging.PagingSource
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCard
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCardFactory
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewEnvelope
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewQueryData
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import io.reactivex.Observable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PledgedProjectsOverviewViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: PledgedProjectsOverviewViewModel

    @Before
    fun setUpEnvrionment() {
        viewModel = PledgedProjectsOverviewViewModel.Factory(environment = environment())
            .create(PledgedProjectsOverviewViewModel::class.java)
    }

    @Test
    fun `emits project when message creator called`() =
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
    fun `emits error when message creator called`() =
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
    fun `emits snackbar when confirms address`() =
        runTest {
            var snackbarAction = 0
            viewModel.provideSnackbarMessage { snackbarAction = it }
            viewModel.showSnackbarAndRefreshCardsList()

            // Should equal address confirmed string id
            assertEquals(
                snackbarAction,
                R.string.address_confirmed_snackbar_text_fpo
            )
        }

    @Test
    fun `emits error state when errored`() =
        runTest {
            val mockApolloClientV2 = object : MockApolloClientV2() {

                override fun getPledgedProjectsOverviewPledges(inputData: PledgedProjectsOverviewQueryData): Observable<PledgedProjectsOverviewEnvelope> {
                    return Observable.error(Throwable())
                }
            }

            val environment = environment().toBuilder().apolloClientV2(mockApolloClientV2).build()

            viewModel = PledgedProjectsOverviewViewModel.Factory(environment = environment)
                .create(PledgedProjectsOverviewViewModel::class.java)

            val uiState = mutableListOf<PledgedProjectsOverviewUIState>()

            val mutableTotalAlerts = MutableStateFlow<Int>(0)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.ppoUIState.toList(uiState)
                viewModel.pagingSource = PledgedProjectsPagingSource(requireNotNull(environment.apolloClientV2()), 25, mutableTotalAlerts)
                viewModel.getPledgedProjects()
                var result = viewModel.pagingSource.load(PagingSource.LoadParams.Refresh("", 0, false))
                assert(result is PagingSource.LoadResult.Error<String, PPOCard>)
            }

            assertEquals(
                uiState,
                listOf(
                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false),
                    PledgedProjectsOverviewUIState(isLoading = true, isErrored = false),
                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = true)
                )
            )
        }

    @Test
    fun `emits empty state when no pledges`() =
        runTest {
            val mockApolloClientV2 = object : MockApolloClientV2() {

                override fun getPledgedProjectsOverviewPledges(inputData: PledgedProjectsOverviewQueryData): Observable<PledgedProjectsOverviewEnvelope> {
                    return Observable.just(PledgedProjectsOverviewEnvelope.builder().totalCount(0).build())
                }
            }

            viewModel = PledgedProjectsOverviewViewModel.Factory(environment = environment().toBuilder().apolloClientV2(mockApolloClientV2).build())
                .create(PledgedProjectsOverviewViewModel::class.java)

            val uiState = mutableListOf<PledgedProjectsOverviewUIState>()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.ppoUIState.toList(uiState)
            }

            viewModel.getPledgedProjects()

            assertEquals(
                uiState,
                listOf(
                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false),
                    PledgedProjectsOverviewUIState(isLoading = true, isErrored = false),
                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false)
                )
            )
        }

    @Test
    fun `emits loading then success state when successful`() =
        runTest {
            val mockApolloClientV2 = object : MockApolloClientV2() {

                override fun getPledgedProjectsOverviewPledges(inputData: PledgedProjectsOverviewQueryData): Observable<PledgedProjectsOverviewEnvelope> {
                    return Observable.just(PledgedProjectsOverviewEnvelope.builder().totalCount(10).pledges(listOf(PPOCardFactory.confirmAddressCard())).build())
                }
            }

            val environment = environment().toBuilder().apolloClientV2(mockApolloClientV2).build()

            viewModel = PledgedProjectsOverviewViewModel.Factory(environment = environment)
                .create(PledgedProjectsOverviewViewModel::class.java)

            val uiState = mutableListOf<PledgedProjectsOverviewUIState>()

            val mutableTotalAlerts = MutableStateFlow<Int>(0)
            viewModel.pagingSource = PledgedProjectsPagingSource(requireNotNull(environment.apolloClientV2()), 25, mutableTotalAlerts)
            viewModel.getPledgedProjects()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.ppoUIState.toList(uiState)
                var result = viewModel.pagingSource.load(PagingSource.LoadParams.Refresh("", 0, false))
//                assert(result is PagingSource.LoadResult.Error<String, PPOCard>)
            }

            assertEquals(
                uiState,
                listOf(
                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false),
                    PledgedProjectsOverviewUIState(isLoading = true, isErrored = false),
                    PledgedProjectsOverviewUIState(isLoading = false, isErrored = false)
                )
            )
        }
}
