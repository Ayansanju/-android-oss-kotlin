package com.kickstarter.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime

@Parcelize
class ProjectFaq private constructor(
    val id: Long,
    val answer: String,
    val createdAt: DateTime? = null,
    val question: String
): Parcelable {

    @Parcelize
    data class Builder(
        var id: Long = -1,
        var answer: String = "",
        var createdAt: DateTime? = null,
        var question: String = ""
    ) : Parcelable {
        fun id(id: Long) = apply { this.id = id }
        fun answer(answer: String) = apply { this.answer = answer }
        fun createdAt(createdAt: DateTime?) = apply { this.createdAt = createdAt }
        fun question(question: String) = apply { this.question = question }
        fun build() = ProjectFaq(id = id, answer = answer, createdAt = createdAt, question = question)
    }

    companion object {
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(this.id, this.answer, this.createdAt, this.question)

    override fun equals(other: Any?): Boolean =
        if (other is ProjectFaq) {
            other.id == this.id && other.answer == this.answer &&
            other.createdAt == this.createdAt && other.question == this.question
        } else false
}