package de.contagio.webapp.repository.mongodb

import de.contagio.core.domain.entity.*
import org.springframework.data.mongodb.repository.MongoRepository

interface PassInfoEnvelopeRepository : MongoRepository<PassInfoEnvelope, String> {
    fun findByIssueStatus(issueStatus: IssueStatus): Collection<PassInfoEnvelope>

    fun findByDeviceInstallationStatus(deviceInstallationStatus: DeviceInstallationStatus): Collection<PassInfoEnvelope>
}

interface PassUpdateLogRepository : MongoRepository<PassUpdateLog, String>

interface EncryptedPayloadRepository : MongoRepository<EncryptedPayload, String>
interface TesterRepository : MongoRepository<Tester, String>
interface TeststationRepository : MongoRepository<Teststation, String>
interface DeviceInfoRepository : MongoRepository<DeviceInfo, String>

interface RegistrationInfoRepository : MongoRepository<RegistrationInfo, String> {
    fun findByDeviceLibraryIdentifierAndSerialNumber(
        deviceLibraryIdentifier: String,
        serialNumber: String
    ): Collection<RegistrationInfo>

    fun findByDeviceLibraryIdentifier(deviceLibraryIdentifier: String): Collection<RegistrationInfo>

    fun findBySerialNumber(serialNumber: String): Collection<RegistrationInfo>

    fun countBySerialNumber(serialNumber: String): Long
}

interface UpdatePassRequestRepository : MongoRepository<UpdatePassRequest, String>

interface DeviceTokenRepository : MongoRepository<DeviceToken, String> {
    fun findByDeviceToken(deviceToken: String): Collection<DeviceToken>

    fun findBySerialNumber(serialNumber: String): Collection<DeviceToken>
}
