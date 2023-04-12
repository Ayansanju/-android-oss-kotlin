package com.kickstarter.viewmodels.usecases

import android.content.SharedPreferences
import android.util.Pair
import com.facebook.appevents.cloudbridge.ConversionsAPIEventName
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.FirebaseHelper
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.toHashedSHAEmail
import com.kickstarter.models.Project
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.ui.SharedPreferenceKey
import rx.Observable
import type.AppDataInput
import type.CustomDataInput
import type.TriggerCapiEventInput

class SendCAPIEventUseCase(
    sharedPreferences: SharedPreferences,
    ffClient: FeatureFlagClientType,
) {
    private val canSendCAPIEventFlag = (
        ffClient.getBoolean(FlagKey.ANDROID_CONSENT_MANAGEMENT) &&
            sharedPreferences.getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false) &&
            ffClient.getBoolean(FlagKey.ANDROID_CAPI_INTEGRATION)
        )

    fun sendCAPIEvent(
        project: Observable<Project>,
        currentUser: CurrentUserType,
        apolloClient: ApolloClientType,
        eventName: ConversionsAPIEventName,
        pledgeAmountAndCurrency: Observable<Pair<String?, String?>> = Observable.just(Pair(null, null)),
    ): Observable<Pair<TriggerCapiEventMutation.Data, TriggerCapiEventInput>> {
        val androidApp = "a2"

        return project
            .filter {
                it.sendMetaCapiEvents()
            }
            .filter { canSendCAPIEventFlag }
            .compose(Transformers.combineLatestPair(currentUser.observable()))
            .compose(Transformers.combineLatestPair(project))
            .compose(Transformers.combineLatestPair(pledgeAmountAndCurrency))
            .map {
                val userEmail = it.first.first.second?.email()
                val hashedEmail = if (it.first.first.second == null || userEmail.isNullOrEmpty()) {
                    userEmail.orEmpty()
                } else {
                    userEmail.toHashedSHAEmail()
                }

                TriggerCapiEventInput.builder()
                    .appData(AppDataInput.builder().extinfo(listOf(androidApp)).build())
                    .eventName(eventName.rawValue)
                    .projectId(encodeRelayId(it.first.second))
                    .externalId(FirebaseHelper.identifier)
                    .userEmail(hashedEmail)
                    .customData(
                        CustomDataInput.builder().currency(it.second.second)
                            .value(it.second.first).build(),
                    )
                    .build()
            }
            .switchMap { input ->
                apolloClient.triggerCapiEvent(
                    input,
                ).map { Pair(it, input) }
                    .compose(Transformers.neverError()).share()
            }
    }
}
