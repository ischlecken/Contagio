package de.contagio.core.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

@Suppress("SpellCheckingInspection")
class CreateUserTest {

    @Test
    fun createUserWithEmptyName_throwsException() {
        assertFails {
            CreateUser("", "2").build()
        }
    }

    @Test
    fun createUserWithNameContainingBlanks_throwsException() {
        assertFails {
            CreateUser("   ", "2").build()
        }
    }

    @Test
    fun createPasswordWithEmptyName_throwsException() {
        assertFails {
            CreateUser("hugo", "").build()
        }
    }

    @Test
    fun createPasswordWithNameContainingBlanks_throwsException() {
        assertFails {
            CreateUser("hugo", "  ").build()
        }
    }

    @Test
    fun createUser_saltIsNotEmpty() {
        val user = CreateUser("hugo", "hguo123").build()

        assertEquals(32, user.salt.length)
    }

    @Test
    fun createUser_passwordhashIsExpected() {
        val user = CreateUser(name = "hugo", password = "hguo123", saltGenerator = { "blafasel" }).build()

        assertEquals("3f2689211b6861bf7a807bc6d7adb2b619ae40310f49bdaecca8301996164877", user.passwordHash)
    }
}
