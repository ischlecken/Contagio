package de.contagio.webapp.repository.mongodb

import de.contagio.core.domain.entity.LocationReviewStatistic
import org.springframework.data.mongodb.repository.MongoRepository

interface LocationReviewStatisticRepository : MongoRepository<LocationReviewStatistic, String> {
}
