package com.kickstarter.services

import android.content.Context
import androidx.work.WorkerParameters
import com.kickstarter.KSApplication
import com.kickstarter.libs.qualifiers.ApplicationContext
import okhttp3.MediaType
import okhttp3.RequestBody
import rx.schedulers.Schedulers
import javax.inject.Inject

class LakeWorker(@ApplicationContext applicationContext: Context, params: WorkerParameters) : TrackingWorker(applicationContext, params) {
    @Inject
    lateinit var lakeService: LakeService
    @Inject
    lateinit var clientId: String

    override fun doWork(): Result {
        (applicationContext as KSApplication).component().inject(this)
        val body = RequestBody.create(MediaType.parse("application/json"), this.eventData)
        val result = this.lakeService
                .track(body, this.clientId)
                .subscribeOn(Schedulers.io())
                .toBlocking()
                .first()
        return handleResult(result)
    }
}
