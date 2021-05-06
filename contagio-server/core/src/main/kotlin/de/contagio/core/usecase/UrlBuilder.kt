package de.contagio.core.usecase

class UrlBuilder(private val baseUrl: String) {

    fun verifyURL(serialNumber: String): String {
        return "$baseUrl/verify?serialNumber=$serialNumber"
    }

    fun passURL(passId: String): String {
        return "$baseUrl/co_v1/pass/$passId"
    }

    val walletURL = "$baseUrl/co_v1/wallet/"
}
