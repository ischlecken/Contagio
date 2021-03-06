package de.contagio.core.domain.entity


import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EncryptedPayloadText {

    private val key = Base64
        .getEncoder()
        .encodeToString(
            byteArrayOf(
                0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x00,
                0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x10
            )
        )

    @Test
    fun encryptData_isEqual() {
        val data = byteArrayOf(
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x00,
            0x21, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x29, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x00,
            0x31, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x39, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x00,
            0x41, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x49, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x00,
            0x51, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x59, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x10
        )

        val encryptedPayload = EncryptedPayload.toEncryptedPayload(
            id = "1",
            data = data,
            key = key
        )
        val decryptedPayload = encryptedPayload.get(key)

        assertTrue(data.contentEquals(decryptedPayload))
    }

    @Test
    fun encryptObj_isEqual() {
        val person = Person(
            firstName = "bla",
            lastName = "fasel",
            phoneNo = "0123",
            email = "bla@fasel.de"
        )

        val encryptedPayload = EncryptedPayload.toEncryptedJsonPayload("12345", person, key)
        val decryptedObj = encryptedPayload.getObject(key, Person::class.java)

        assertEquals(person, decryptedObj)
    }
}
