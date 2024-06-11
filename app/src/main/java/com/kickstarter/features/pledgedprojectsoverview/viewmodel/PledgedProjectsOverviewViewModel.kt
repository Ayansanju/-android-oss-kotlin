package com.kickstarter.features.pledgedprojectsoverview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagingData
import com.kickstarter.features.pledgedprojectsoverview.ui.PPOCardDataMock
import com.kickstarter.libs.Environment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PledgedProjectsOverviewViewModel(environment: Environment) : ViewModel() {

    private val ppoCards = MutableStateFlow<PagingData<PPOCardDataMock>>(PagingData.empty())
    private val totalAlerts = MutableStateFlow<Int>(0)

    val ppoCardsState: StateFlow<PagingData<PPOCardDataMock>> = ppoCards.asStateFlow()
    val totalAlertsState: StateFlow<Int> = totalAlerts.asStateFlow()

    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PledgedProjectsOverviewViewModel(environment) as T
        }
    }
}
