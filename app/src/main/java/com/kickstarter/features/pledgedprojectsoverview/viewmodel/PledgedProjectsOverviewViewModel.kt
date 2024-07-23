package com.kickstarter.features.pledgedprojectsoverview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kickstarter.R
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCard
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewQueryData
import com.kickstarter.libs.Environment
import com.kickstarter.models.Project
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

private val PAGE_LIMIT = 25
class PledgedProjectsPagingSource(
    private val apolloClient: ApolloClientTypeV2,
    private var totalAlerts: MutableStateFlow<Int>,
    private val limit: Int = PAGE_LIMIT,

) : PagingSource<String, PPOCard>() {
    override fun getRefreshKey(state: PagingState<String, PPOCard>): String {
        return "" // - Default first page is empty string when paginating with graphQL
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, PPOCard> {
        return try {
            var ppoCardsList = emptyList<PPOCard>()
            var nextPageEnvelope: PageInfoEnvelope? = null
            var inputData = PledgedProjectsOverviewQueryData(limit, params.key ?: "")
            var result: LoadResult<String, PPOCard> = LoadResult.Error(Throwable())

            apolloClient.getPledgedProjectsOverviewPledges(
                inputData = inputData
            )
                .asFlow()
                .catch {
                    result = LoadResult.Error(it)
                }
                .collect { envelope ->
                    totalAlerts.emit(envelope.totalCount ?: 0)
                    ppoCardsList = envelope.pledges() ?: emptyList()
                    nextPageEnvelope = if (envelope.pageInfoEnvelope?.hasNextPage == true) envelope.pageInfoEnvelope else null
                    result = LoadResult.Page(
                        data = ppoCardsList,
                        prevKey = null,
                        nextKey = nextPageEnvelope?.endCursor
                    )
                }
            return result
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}

data class PledgedProjectsOverviewUIState(
    val isLoading: Boolean = false,
    val isErrored: Boolean = false,
)
class PledgedProjectsOverviewViewModel(environment: Environment) : ViewModel() {

    private val mutablePpoCards = MutableStateFlow<PagingData<PPOCard>>(PagingData.empty())
    private var mutableProjectFlow = MutableSharedFlow<Project>()
    private var snackbarMessage: (stringID: Int) -> Unit = {}
    private val apolloClient = requireNotNull(environment.apolloClientV2())

    private val mutableTotalAlerts = MutableStateFlow<Int>(0)
    val totalAlertsState = mutableTotalAlerts.asStateFlow()

    private val mutablePPOUIState = MutableStateFlow(PledgedProjectsOverviewUIState())
    val ppoCardsState: StateFlow<PagingData<PPOCard>> = mutablePpoCards.asStateFlow()

    private var pagingSource = PledgedProjectsPagingSource(apolloClient, mutableTotalAlerts, PAGE_LIMIT)

    val ppoUIState: StateFlow<PledgedProjectsOverviewUIState>
        get() = mutablePPOUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = PledgedProjectsOverviewUIState()
            )

    fun showSnackbarAndRefreshCardsList() {
        snackbarMessage.invoke(R.string.address_confirmed_snackbar_text_fpo)
        // TODO: MBL-1556 refresh the PPO list (i.e. requery the PPO list).
    }

    val projectFlow: SharedFlow<Project>
        get() = mutableProjectFlow
            .asSharedFlow()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
            )

    init {
        getPledgedProjects()
    }

    fun getPledgedProjects() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Pager(
                    PagingConfig(
                        pageSize = PAGE_LIMIT,
                        prefetchDistance = 3,
                        enablePlaceholders = true,
                    )
                ) {
                    pagingSource
                }
                    .flow
                    .onStart {
                        emitCurrentState(isLoading = true)
                    }.catch {
                        emitCurrentState(isErrored = true)
                    }.collectLatest { pagingData ->
                        mutablePpoCards.value = pagingData
                        emitCurrentState()
                    }
            } catch (e: Exception) {
                emitCurrentState(isErrored = true)
            }
        }
    }

    fun provideSnackbarMessage(snackBarMessage: (Int) -> Unit) {
        this.snackbarMessage = snackBarMessage
    }

    fun onMessageCreatorClicked(projectName: String) {
        viewModelScope.launch {
            apolloClient.getProject(
                slug = projectName,
            )
                .asFlow()
                .onStart {
                    emitCurrentState(isLoading = true)
                }.map { project ->
                    mutableProjectFlow.emit(project)
                }.catch {
                    snackbarMessage.invoke(R.string.Something_went_wrong_please_try_again)
                }.onCompletion {
                    emitCurrentState()
                }.collect()
        }
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false, isErrored: Boolean = false) {
        mutablePPOUIState.emit(
            PledgedProjectsOverviewUIState(
                isLoading = isLoading,
                isErrored = isErrored,
            )
        )
    }

    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PledgedProjectsOverviewViewModel(environment) as T
        }
    }
}
