package de.contagio.webapp.repository.mongodb

import de.contagio.core.domain.entity.Pass
import org.springframework.data.mongodb.repository.MongoRepository

interface PassRepository : MongoRepository<Pass, String> {
}
