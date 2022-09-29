package com.kickstarter.services

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.services.transformers.encodeRelayId
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface ApolloClientTypeV2 {
    fun createSetupIntent(project: Project? = null): Observable<String>
    fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): Observable<StoredCard>
    fun getStoredCards(): Observable<List<StoredCard>>
    fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data>
}

class KSApolloClientV2(val service: ApolloClient) : ApolloClientTypeV2 {

    override fun createSetupIntent(project: Project?): Observable<String> {
        return Observable.defer {
            val createSetupIntentMut = CreateSetupIntentMutation.builder()
                .apply {
                    if (project != null) this.projectId(encodeRelayId(project))
                }
                .build()

            val ps = PublishSubject.create<String>()
            this.service.mutate(createSetupIntentMut)
                .enqueue(object : ApolloCall.Callback<CreateSetupIntentMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<CreateSetupIntentMutation.Data>) {
                        if (response.hasErrors()) ps.onError(java.lang.Exception(response.errors?.first()?.message))
                        else {
                            ps.onNext(response.data?.createSetupIntent()?.clientSecret() ?: "")
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): Observable<StoredCard> {
        return Observable.defer {
            val ps = PublishSubject.create<StoredCard>()
            service.mutate(
                SavePaymentMethodMutation.builder()
                    .paymentType(savePaymentMethodData.paymentType)
                    .stripeToken(savePaymentMethodData.stripeToken)
                    .stripeCardId(savePaymentMethodData.stripeCardId)
                    .reusable(savePaymentMethodData.reusable)
                    .intentClientSecret(savePaymentMethodData.intentClientSecret)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<SavePaymentMethodMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<SavePaymentMethodMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        }

                        val paymentSource = response.data?.createPaymentSource()?.paymentSource()
                        paymentSource?.let {
                            val storedCard = StoredCard.builder()
                                .expiration(it.expirationDate())
                                .id(it.id())
                                .lastFourDigits(it.lastFour())
                                .type(it.type())
                                .build()
                            ps.onNext(storedCard)
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun getStoredCards(): Observable<List<StoredCard>> {
        return Observable.defer {
            val ps = PublishSubject.create<List<StoredCard>>()
            this.service.query(UserPaymentsQuery.builder().build())
                .enqueue(object : ApolloCall.Callback<UserPaymentsQuery.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<UserPaymentsQuery.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        } else {
                            val cardsList = mutableListOf<StoredCard>()
                            response.data?.me()?.storedCards()?.nodes()?.map {
                                it?.let { cardData ->
                                    val card = StoredCard.builder()
                                        .expiration(cardData.expirationDate())
                                        .id(cardData.id())
                                        .lastFourDigits(cardData.lastFour())
                                        .type(it.type())
                                        .build()
                                    cardsList.add(card)
                                }
                            }
                            ps.onNext(cardsList)
                            ps.onComplete()
                        }
                    }
                })
            return@defer ps
        }
    }

    override fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<DeletePaymentSourceMutation.Data>()
            service.mutate(
                DeletePaymentSourceMutation.builder()
                    .paymentSourceId(paymentSourceId)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<DeletePaymentSourceMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<DeletePaymentSourceMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        }

                        response.data?.let { ps.onNext(it) }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }
}
