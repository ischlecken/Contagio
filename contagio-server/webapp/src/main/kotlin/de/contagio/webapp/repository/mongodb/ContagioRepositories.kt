package de.contagio.webapp.repository.mongodb

import de.contagio.core.domain.entity.*
import org.springframework.data.mongodb.repository.MongoRepository

interface PassInfoRepository : MongoRepository<PassInfo, String> {
    fun findByDeviceLibraryIdentifier(deviceLibraryIdentifier: String): Collection<PassInfo>
}


interface PassRepository : MongoRepository<Pass, String> {
}


interface PassImageRepository : MongoRepository<PassImage, String> {
}


interface TesterRepository : MongoRepository<Tester, String> {
}

interface TeststationRepository : MongoRepository<Teststation, String> {
}
