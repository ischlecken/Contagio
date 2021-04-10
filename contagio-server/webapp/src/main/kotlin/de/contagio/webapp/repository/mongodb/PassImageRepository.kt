package de.contagio.webapp.repository.mongodb

import de.contagio.core.domain.entity.PassImage
import org.springframework.data.mongodb.repository.MongoRepository

interface PassImageRepository : MongoRepository<PassImage, String> {
}
