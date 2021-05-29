package de.contagio.core

import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun String.toSHA256(salt: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val s = salt+this
    val digest = md.digest(s.toByteArray())

    return digest.fold("") { str, it -> str + "%02x".format(it) }
}


private val lastModifiedDateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)

fun String?.lastModifiedDateTime(): String? {
    val ts = if (!this.isNullOrEmpty())
        Instant.ofEpochSecond(this.toLong() + 1)
    else
        null

    return ts.lastModifiedDateTime()
}


fun Instant?.lastModifiedDateTime() =
    if (this != null)
        lastModifiedDateTimeFormatter.format(this.atZone(ZoneId.of("GMT")))
    else
        null
