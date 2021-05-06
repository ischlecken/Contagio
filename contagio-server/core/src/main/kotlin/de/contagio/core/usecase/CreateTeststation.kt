package de.contagio.core.usecase

import de.contagio.core.domain.entity.Address
import de.contagio.core.domain.entity.CreateTeststationDTO
import de.contagio.core.domain.entity.Teststation
import de.contagio.core.domain.port.ISaveTeststation
import de.contagio.core.util.UIDGenerator

class CreateTeststation(
    private val saveTeststation: ISaveTeststation
) {
    private val uidGenerator = UIDGenerator()

    fun execute(createTeststationDTO: CreateTeststationDTO) {
        saveTeststation.execute(
            Teststation(
                id = uidGenerator.generate(),
                name = createTeststationDTO.name,
                address = Address(
                    city = createTeststationDTO.city,
                    zipcode = createTeststationDTO.zipcode,
                    street = createTeststationDTO.street,
                    hno = createTeststationDTO.hno
                )
            )
        )
    }
}
