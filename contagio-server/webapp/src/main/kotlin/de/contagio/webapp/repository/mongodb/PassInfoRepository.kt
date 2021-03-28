package de.contagio.webapp.repository.mongodb

import de.contagio.core.domain.entity.PassInfo
import org.springframework.data.mongodb.repository.MongoRepository

interface PassInfoRepository : MongoRepository<PassInfo, String> {
}
