package de.contagio.core.usecase

import de.contagio.core.domain.entity.CreateTesterDTO
import de.contagio.core.domain.entity.Person
import de.contagio.core.domain.entity.Tester
import de.contagio.core.domain.port.ISaveTester
import de.contagio.core.util.UIDGenerator

class CreateTester(
    private val saveTester: ISaveTester
) {
    private val uidGenerator = UIDGenerator()

    fun execute(createTesterDTO: CreateTesterDTO) {
        saveTester.execute(
            Tester(
                id = uidGenerator.generate(),
                teststationId = createTesterDTO.teststationId,
                person = Person(
                    firstName = createTesterDTO.firstName,
                    lastName = createTesterDTO.lastName,
                    phoneNo = createTesterDTO.phoneNo,
                    email = createTesterDTO.email
                )
            )
        )
    }
}
