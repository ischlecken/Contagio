package de.contagio.webapp.config

import de.contagio.core.domain.port.*
import de.contagio.core.usecase.*
import de.contagio.webapp.model.properties.ContagioProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class UsecaseConfig {

    @Bean
    open fun listOfAllTesterInfo(
        findAllTester: IFindAllTester,
        findAllTeststation: IFindAllTeststation
    ) = ListOfAllTesterInfo(findAllTeststation, findAllTester)

    @Bean
    open fun searchAllTesterTeststation(
        findAllTester: IFindAllTester,
        findAllTeststation: IFindAllTeststation
    ) = SearchAllTesterTeststation(findAllTeststation, findAllTester)

    @Bean
    open fun searchTestWithTeststation(
        findTester: IFindTester,
        findTeststation: IFindTeststation
    ) = SearchTesterWithTeststation(findTester, findTeststation)

    @Bean
    open fun searchTeststation(
        findTeststation: IFindTeststation
    ) = SearchTeststation(findTeststation)

    @Bean
    open fun createTeststation(
        saveTeststation: ISaveTeststation
    ) = CreateTeststation(saveTeststation)

    @Bean
    open fun updateTeststation(
        saveTeststation: ISaveTeststation
    ) = UpdateTeststation(saveTeststation)


    @Bean
    open fun createTester(
        saveTester: ISaveTester
    ) = CreateTester(saveTester)

    @Bean
    open fun updateTester(
        saveTester: ISaveTester
    ) = UpdateTester(saveTester)


    @Bean
    open fun searchPassInfo(
        findPassInfoEnvelope: IFindPassInfoEnvelope,
        findEncryptedPayload: IFindEncryptedPayload,
        getEncryptionKey: IGetEncryptionKey,
        searchTesterWithTeststation: SearchTesterWithTeststation
    ) = SearchPassInfo(findPassInfoEnvelope, findEncryptedPayload, getEncryptionKey, searchTesterWithTeststation)

    @Bean
    open fun searchPassesForDevice(
        findRegisteredSerialNumbers: IFindRegisteredSerialNumbers,
        findPassInfoEnvelope: IFindPassInfoEnvelope,
        findEncryptedPayload: IFindEncryptedPayload,
        getEncryptionKey: IGetEncryptionKey
    ) = SearchPassesForDevice(findRegisteredSerialNumbers, findPassInfoEnvelope,findEncryptedPayload,getEncryptionKey)

    @Bean
    open fun urlBuilder(contagioProperties: ContagioProperties): UrlBuilder {
        return UrlBuilder(contagioProperties.baseUrl)
    }




    @Bean
    open fun updatePass(
        findPassInfoEnvelope: IFindPassInfoEnvelope,
        findEncryptedPayload: IFindEncryptedPayload,
        savePassInfoEnvelope: ISavePassInfoEnvelope,
        saveEncryptedPayload: ISaveEncryptedPayload,
        saveRawEncryptedPayload: ISaveRawEncryptedPayload,
        searchTesterWithTeststation: SearchTesterWithTeststation,
        getEncryptionKey: IGetEncryptionKey,
        urlBuilder: UrlBuilder
    ) = UpdatePass(
        findPassInfoEnvelope = findPassInfoEnvelope,
        findEncryptedPayload = findEncryptedPayload,
        savePassInfoEnvelope = savePassInfoEnvelope,
        saveEncryptedPayload = saveEncryptedPayload,
        saveRawEncryptedPayload = saveRawEncryptedPayload,
        searchTesterWithTeststation = searchTesterWithTeststation,
        getEncryptionKey = getEncryptionKey,
        urlBuilder = urlBuilder
    )

    @Bean
    open fun createPass(
        savePassInfoEnvelope: ISavePassInfoEnvelope,
        saveEncryptedPayload: ISaveEncryptedPayload,
        saveRawEncryptedPayload: ISaveRawEncryptedPayload,
        searchTesterWithTeststation: SearchTesterWithTeststation,
        urlBuilder: UrlBuilder
    ) = CreatePass(
        savePassInfoEnvelope,
        saveEncryptedPayload,
        saveRawEncryptedPayload,
        searchTesterWithTeststation,
        urlBuilder
    )

    @Bean
    open fun updateOnlyPassInfoEnvelope(
        findPassInfoEnvelope: IFindPassInfoEnvelope,
        savePassInfoEnvelope: ISavePassInfoEnvelope
    ) = UpdateOnlyPassInfoEnvelope(findPassInfoEnvelope, savePassInfoEnvelope)

    @Bean
    open fun notifyAllDevicesWithInstalledSerialNumber(
        findRegistrationInfoBySerialNumber: IFindRegistrationInfoBySerialNumber,
        findDeviceInfo: IFindDeviceInfo,
        notifyDevice: INotifyDevice
    ) = NotifyAllDevicesWithInstalledSerialNumber(findRegistrationInfoBySerialNumber, findDeviceInfo, notifyDevice)
}
