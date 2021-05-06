package de.contagio.core.usecase

import de.contagio.core.domain.entity.Address
import de.contagio.core.domain.entity.Teststation
import de.contagio.core.domain.entity.UpdateTeststationDTO
import de.contagio.core.domain.port.IFindTeststation
import de.contagio.core.domain.port.ISaveTeststation
import java.time.Instant

class UpdateTeststation(
    private val findTeststation: IFindTeststation,
    private val saveTeststation: ISaveTeststation
) {

    fun execute(teststation: Teststation, updateTeststationDTO: UpdateTeststationDTO) {
        saveTeststation.execute(
            teststation.copy(
                name = updateTeststationDTO.name,
                address = Address(
                    city = updateTeststationDTO.city,
                    zipcode = updateTeststationDTO.zipcode,
                    street = updateTeststationDTO.street,
                    hno = updateTeststationDTO.hno
                ),
                modified = Instant.now()
            )
        )
    }
}
