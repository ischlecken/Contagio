package de.contagio.webapp.repository.mongodb

import de.contagio.core.domain.entity.*
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.util.*

interface PassInfoRepository : MongoRepository<PassInfoEnvelope, String> {
    @Query("{issueStatus : {\$ne : ?0}}")
    fun findByIssueStatusNotEqual(issueStatus: IssueStatus): Collection<PassInfoEnvelope>

    fun findByImageId(imageId: String): Optional<PassInfoEnvelope>
}


interface PassRepository : MongoRepository<Pass, String> {
}


interface PassImageRepository : MongoRepository<PassImage, String> {
}


interface TesterRepository : MongoRepository<Tester, String> {
}

interface TeststationRepository : MongoRepository<Teststation, String> {
}


interface DeviceInfoRepository : MongoRepository<DeviceInfo, String> {
}


interface RegistrationInfoRepository : MongoRepository<RegistrationInfo, String> {
    fun findByDeviceLibraryIdentifierAndSerialNumber(
        deviceLibraryIdentifier: String,
        serialNumber: String
    ): Collection<RegistrationInfo>

    fun findByDeviceLibraryIdentifier(deviceLibraryIdentifier: String): Collection<RegistrationInfo>

    fun findBySerialNumber(serialNumber: String): Collection<RegistrationInfo>

    fun countBySerialNumber(serialNumber: String): Long
}
