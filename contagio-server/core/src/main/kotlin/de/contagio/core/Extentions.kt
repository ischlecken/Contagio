package de.contagio.core

import java.security.MessageDigest

fun String.toSHA256(salt: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val s = salt+this
    val digest = md.digest(s.toByteArray())

    return digest.fold("") { str, it -> str + "%02x".format(it) }
}
