package de.contagio.webapp.config

import de.contagio.core.domain.entity.PassSigningInfo
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
        findUpdatePassRequest: IFindUpdatePassRequest
    ) = SearchPassesForDevice(findRegisteredSerialNumbers, findPassInfoEnvelope, findUpdatePassRequest)

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
        urlBuilder: UrlBuilder,
        passSigningInfo: PassSigningInfo
    ) = UpdatePass(
        findPassInfoEnvelope = findPassInfoEnvelope,
        findEncryptedPayload = findEncryptedPayload,
        savePassInfoEnvelope = savePassInfoEnvelope,
        saveEncryptedPayload = saveEncryptedPayload,
        saveRawEncryptedPayload = saveRawEncryptedPayload,
        searchTesterWithTeststation = searchTesterWithTeststation,
        urlBuilder = urlBuilder,
        passSigningInfo = passSigningInfo
    )

    @Bean
    open fun createPass(
        savePassInfoEnvelope: ISavePassInfoEnvelope,
        saveEncryptedPayload: ISaveEncryptedPayload,
        saveRawEncryptedPayload: ISaveRawEncryptedPayload,
        searchTesterWithTeststation: SearchTesterWithTeststation,
        urlBuilder: UrlBuilder,
        passSigningInfo: PassSigningInfo
    ) = CreatePass(
        savePassInfoEnvelope,
        saveEncryptedPayload,
        saveRawEncryptedPayload,
        searchTesterWithTeststation,
        urlBuilder,
        passSigningInfo
    )

    @Bean
    open fun updateOnlyPassInfoEnvelope(
        findPassInfoEnvelope: IFindPassInfoEnvelope,
        savePassInfoEnvelope: ISavePassInfoEnvelope
    ) = UpdatePassInfoEnvelope(findPassInfoEnvelope, savePassInfoEnvelope)

    @Bean
    open fun notifyAllDevicesWithInstalledSerialNumber(
        findRegistrationInfoBySerialNumber: IFindRegistrationInfoBySerialNumber,
        findDeviceInfo: IFindDeviceInfo,
        notifyDevice: INotifyDevice,
        findDeviceTokenBySerialNumber: IFindDeviceTokenBySerialNumber,
        notifyTeststation: INotifyTeststation,
        savePassUpdateLog: ISavePassUpdateLog
    ) = NotifyAllDevicesWithInstalledSerialNumber(
        findRegistrationInfoBySerialNumber,
        findDeviceInfo,
        notifyDevice,
        findDeviceTokenBySerialNumber,
        notifyTeststation,
        savePassUpdateLog
    )

    @Bean
    open fun deletePass(
        findPassInfoEnvelope: IFindPassInfoEnvelope,
        deleteEncryptedPayload: IDeleteEncryptedPayload,
        updatePassInfoEnvelope: UpdatePassInfoEnvelope
    ) = DeletePass(
        findPassInfoEnvelope,
        deleteEncryptedPayload,
        updatePassInfoEnvelope
    )

    @Bean
    open fun lazyUpdatePassInfo(
        getEncryptionKey: IGetEncryptionKey,
        findUpdatePassRequest: IFindUpdatePassRequest,
        deleteUpdatePassRequest: IDeleteUpdatePassRequest,
        updatePass: UpdatePass,
        searchPassInfo: SearchPassInfo
    ) = LazyUpdatePassInfo(
        getEncryptionKey,
        findUpdatePassRequest,
        deleteUpdatePassRequest,
        updatePass,
        searchPassInfo
    )

    @Bean
    open fun registerDeviceToken(
        saveDeviceToken: ISaveDeviceToken,
        deleteDeviceToken: IDeleteDeviceToken
    ) = RegisterDeviceToken(saveDeviceToken, deleteDeviceToken)
}
