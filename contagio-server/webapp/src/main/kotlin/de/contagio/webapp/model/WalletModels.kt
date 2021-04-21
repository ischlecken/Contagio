package de.contagio.webapp.model

data class WalletLog(val logs: Collection<String>)

data class WalletRegistration(val pushToken: String)

data class WalletPasses(val lastUpdated: String, val serialNumbers: Collection<String>)
