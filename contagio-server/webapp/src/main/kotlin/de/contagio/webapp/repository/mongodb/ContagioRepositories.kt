package de.contagio.webapp.repository.mongodb

import de.contagio.core.domain.entity.*
import org.springframework.data.mongodb.repository.MongoRepository

interface PassInfoRepository : MongoRepository<PassInfo, String> {
}


interface PassRepository : MongoRepository<Pass, String> {
}


interface TesterRepository : MongoRepository<Tester, String> {
}

interface TeststationRepository : MongoRepository<Teststation, String> {
}


interface PassImageRepository : MongoRepository<PassImage, String> {
}
