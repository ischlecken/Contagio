package de.contagio.core.domain.entity

import org.springframework.data.annotation.Id

enum class ReviewProvider {
    GOLOCAL, MEINUNGSMEISTER, GOOGLE, FACEBOOK;
}

data class ReviewStatistic(
    val reviewProvider: ReviewProvider,
    val count: Int,
    val average: Float,
    val isLocked: Boolean
)

data class LocationReviewStatistic(
    @Id val locationId: String,
    val reviewStatistic: Collection<ReviewStatistic>
)
