package de.contagio.webapp.restcontroller

import org.springframework.http.MediaType

const val BASE_RESTAPI_PREFIX = "/co_v"
const val BASE_RESTAPI_1 = BASE_RESTAPI_PREFIX + "1"
const val PASS = "$BASE_RESTAPI_1/pass"
const val TESTSTATION = "$BASE_RESTAPI_1/teststations"
const val TESTER = "$BASE_RESTAPI_1/tester"
const val WALLET = "$BASE_RESTAPI_1/wallet"
const val APPS = "$BASE_RESTAPI_1/apps"

val pkpassMediatype = MediaType("application", "vnd.apple.pkpass")
