package de.contagio.core.usecase

class UrlBuilder(private val baseUrl: String) {

    fun verifyURL(serialNumber: String): String {
        return "$baseUrl/verify?serialNumber=$serialNumber"
    }

    fun passURL(passId: String): String {
        return "$baseUrl/co_v1/pass/$passId"
    }

    @Suppress("unused")
    fun showpassForSerialNumberURL(serialNumber: String): String {
        return "$showpassURL?serialNumber=$serialNumber"
    }

    val homeURL = baseUrl
    val passURL = "$baseUrl/pass"
    val showpassURL = "$baseUrl/showpass"
    val createpassURL = "$baseUrl/createpass"
    val testerURL = "$baseUrl/tester"
    val teststationURL = "$baseUrl/teststation"

    val deviceinfoURL = "$baseUrl/deviceinfo"
    val registrationinfoURL = "$baseUrl/registrationinfo"

    val createtesterURL = "$baseUrl/createtester"
    val edittesterURL = "$baseUrl/edittester"
    val createteststationURL = "$baseUrl/createteststation"
    val editteststationURL = "$baseUrl/editteststation"

    val walletURL = "$baseUrl/co_v1/wallet/"
}
