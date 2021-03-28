package de.contagio.core.usecase

import de.contagio.core.domain.entity.User
import de.contagio.core.domain.port.ISaltGenerator
import de.contagio.core.toSHA256
import de.contagio.core.util.SaltGenerator

class CreateUser(
    private val name: String,
    private val password: String,
    private val locationId: Int = 0,
    private val saltGenerator: ISaltGenerator = SaltGenerator()
) {

    fun build(): User {
        if (name.trim().isEmpty())
            throw IllegalArgumentException("name must be set")

        if (password.trim().isEmpty())
            throw IllegalArgumentException("password must be set")

        val salt = saltGenerator.generate()
        return User(
            name = name,
            salt = salt,
            passwordHash = password.toSHA256(salt),
            locationId = locationId
        )
    }
}
