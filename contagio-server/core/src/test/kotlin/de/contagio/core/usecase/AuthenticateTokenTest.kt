package de.contagio.core.usecase

import de.contagio.core.util.MemoryTokenRepository
import java.util.*
import kotlin.test.*

class AuthenticateTokenTest {

    @Test
    fun emptyToken_throwsException() {
        assertFails {
            AuthenticateToken(MemoryTokenRepository()).authenticate("")
        }
    }


    @Test
    fun tokenWithBlanks_throwsException() {
        assertFails {
            AuthenticateToken(MemoryTokenRepository()).authenticate("   ")
        }
    }

    @Test
    fun unknownToken_isNull() {
        assertNull(AuthenticateToken(MemoryTokenRepository()).authenticate(UUID.randomUUID().toString()))
    }

    @Test
    fun knownToken_isNotNull() {
        val tokenRepository = MemoryTokenRepository()
        val user = CreateUser(name = "test", password = "test123").build()
        val token = AuthenticateUser(tokenRepository, user).authenticate("test123")
        val authUser = AuthenticateToken(tokenRepository).authenticate(token!!)

        assertNotNull(authUser)
    }

    @Test
    fun knownToken_returnsUser() {
        val tokenRepository = MemoryTokenRepository()
        val user = CreateUser(name = "test", password = "test123").build()
        val token = AuthenticateUser(tokenRepository, user).authenticate("test123")
        val authUser = AuthenticateToken(tokenRepository).authenticate(token!!)

        assertEquals(user.name, authUser!!.name)
    }
}
