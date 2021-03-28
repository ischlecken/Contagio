package de.contagio.core.usecase

import de.contagio.core.domain.entity.PassInfo
import kotlin.test.Test
import kotlin.test.assertEquals

class CreatePassTest {

    @Test
    fun createPass_expectedValuesAndValid() {
        val createPass =
            CreatePass(
                teamIdentifier = "teamid",
                passTypeIdentifier = "passTypeId",
                authenticationToken = "0123456789abcdef"
            )

        val pass = createPass.build(
            PassInfo("123", " abc ")
        )

        assertEquals("123", pass.serialNumber)
        assertEquals("teamid", pass.teamIdentifier)
        assertEquals("passTypeId", pass.passTypeIdentifier)
        assertEquals("0123456789abcdef", pass.authenticationToken)

        val validationErrors = pass.validationErrors
        assertEquals(0, validationErrors.size)
    }
}
