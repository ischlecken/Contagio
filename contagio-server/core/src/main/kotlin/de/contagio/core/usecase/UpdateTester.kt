package de.contagio.core.usecase

import de.contagio.core.domain.entity.Person
import de.contagio.core.domain.entity.Tester
import de.contagio.core.domain.entity.UpdateTesterDTO
import de.contagio.core.domain.port.ISaveTester
import java.time.Instant


class UpdateTester(
    private val saveTester: ISaveTester
) {

    fun execute(tester: Tester, updateTesterDTO: UpdateTesterDTO) {
        saveTester.execute(
            tester.copy(
                person = Person(
                    firstName = updateTesterDTO.firstName,
                    lastName = updateTesterDTO.lastName,
                    phoneNo = updateTesterDTO.phoneNo,
                    email = updateTesterDTO.email
                ),
                modified = Instant.now()
            )
        )
    }
}
