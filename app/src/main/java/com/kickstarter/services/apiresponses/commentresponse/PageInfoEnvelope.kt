package com.kickstarter.services.apiresponses.commentresponse

class PageInfoEnvelope private constructor(
    val hasPreviousPage: Boolean?,
    val hasNextPage: Boolean?,
    val startCursor: String?,
    val endCursor: String?
) {

    data class Builder(
        var hasPreviousPage: Boolean? = null,
        var hasNextPage: Boolean? = null,
        var startCursor: String? = null,
        var endCursor: String? = null
    ) {

        fun hasPreviousPage(hasPreviousPage: Boolean) = apply { this.hasPreviousPage = hasPreviousPage }
        fun hasNextPage(hasNextPage: Boolean) = apply { this.hasNextPage = hasNextPage }
        fun startCursor(startCursor: String) = apply { this.startCursor = startCursor }
        fun endCursor(endCursor: String) = apply { this.endCursor = endCursor }
        fun build() = PageInfoEnvelope(hasPreviousPage, hasNextPage, startCursor, endCursor)
    }

    companion object {
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(this.hasPreviousPage, this.hasNextPage, this.startCursor, this.endCursor)
}
