package de.contagio.core.usecase

import de.contagio.core.util.MemoryTokenRepository
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthenticateUserTest {


    @Test
    fun emptyPassword_throwsException() {
        assertFails {
            AuthenticateUser(MemoryTokenRepository(), CreateUser("bla", "fasel").build()).authenticate("")
        }
    }

    @Test
    fun wrongPassword_notAuthenticated() {
        assertNull(AuthenticateUser(MemoryTokenRepository(), CreateUser("bla", "fasel").build()).authenticate("123"))
    }

    @Test
    fun correctPassword_authenticated() {
        assertNotNull(AuthenticateUser(MemoryTokenRepository(), CreateUser("bla", "fasel").build()).authenticate("fasel"))
    }
}
