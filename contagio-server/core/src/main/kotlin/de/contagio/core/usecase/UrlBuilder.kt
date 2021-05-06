package de.contagio.core.usecase

class UrlBuilder(private val baseUrl: String) {

    fun verifyURL(serialNumber: String): String {
        return "$baseUrl/verify?serialNumber=$serialNumber"
    }

    fun passURL(passId: String): String {
        return "$baseUrl/co_v1/pass/$passId"
    }

    val homeURL = baseUrl
    val overviewURL = "$baseUrl/overview"
    val showpassURL = "$baseUrl/showpass"
    val createpassURL = "$baseUrl/createpass"
    val testerURL = "$baseUrl/tester"
    val teststationURL = "$baseUrl/teststation"

    val createtesterURL = "$baseUrl/createtester"
    val edittesterURL = "$baseUrl/edittester"
    val createteststationURL = "$baseUrl/createteststation"
    val editteststationURL = "$baseUrl/editteststation"

    val walletURL = "$baseUrl/co_v1/wallet/"
}
