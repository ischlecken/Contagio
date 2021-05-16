package de.contagio.core.domain.port


enum class IdType {
    SERIALNUMBER, PASSID, IMAGEID
}

fun interface IGetEncryptionKey {
    fun execute(type: IdType, id: String): String?
}


fun interface ISetEncryptionKey {
    fun execute(type: IdType, id: String, key:String)
}
