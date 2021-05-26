package de.contagio.webapp.service

import de.contagio.core.domain.entity.DeviceInfo
import de.contagio.core.domain.entity.RegistrationInfo
import de.contagio.core.util.UIDGenerator
import de.contagio.webapp.repository.mongodb.DeviceInfoRepository
import de.contagio.webapp.repository.mongodb.RegistrationInfoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private var logger = LoggerFactory.getLogger(WalletService::class.java)

@Service
open class WalletService(
    private val deviceInfoRepository: DeviceInfoRepository,
    private val registrationInfoRepository: RegistrationInfoRepository,
    private val passCommandProcessor: PassCommandProcessor
) {

    private val uidGenerator = UIDGenerator()

    open fun register(deviceLibraryIdentifier: String, serialNumber: String, pushToken: String) {
        registrationInfoRepository.save(
            RegistrationInfo(
                id = uidGenerator.generate(),
                deviceLibraryIdentifier = deviceLibraryIdentifier,
                serialNumber = serialNumber
            )
        )

        deviceInfoRepository.save(
            DeviceInfo(
                deviceLibraryIdentifier = deviceLibraryIdentifier,
                pushToken = pushToken
            )
        )

        passCommandProcessor.passInstalled(serialNumber)
    }

    open fun unregister(deviceLibraryIdentifier: String, serialNumber: String): Boolean {
        var result = false

        try {
            registrationInfoRepository
                .findByDeviceLibraryIdentifierAndSerialNumber(
                    deviceLibraryIdentifier,
                    serialNumber
                )
                .forEach {
                    registrationInfoRepository.deleteById(it.id)
                    result = true
                }

            if (registrationInfoRepository.countBySerialNumber(serialNumber) == 0L)
                passCommandProcessor.passRemoved(serialNumber)
        } catch (ex: Exception) {
            logger.error("Exception while unregister $serialNumber", ex)
        }

        return result
    }
}
