package de.contagio.webapp

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles(profiles = ["buildinfo","integrationtest"])
class MMControlAPIApplicationIT {

    @Test
    fun springcontextLoaded() {
        assertTrue(true)
    }
}
