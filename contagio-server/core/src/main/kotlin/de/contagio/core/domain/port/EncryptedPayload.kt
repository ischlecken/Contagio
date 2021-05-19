package de.contagio.core.domain.port

import de.contagio.core.domain.entity.IEncryptedPayload

fun interface IFindEncryptedPayload {
    fun execute(id: String?): IEncryptedPayload?
}


fun interface ISaveEncryptedPayload {
    fun execute(id: String, obj: Any, key: String): IEncryptedPayload
}


fun interface IDeleteEncryptedPayload {
    fun execute(id: String?)
}

fun interface ISaveRawEncryptedPayload {
    fun execute(id: String, data: ByteArray, key: String): IEncryptedPayload
}
