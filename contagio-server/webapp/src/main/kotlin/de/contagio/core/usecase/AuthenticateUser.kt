package de.contagio.core.usecase

import de.contagio.core.domain.entity.User
import de.contagio.core.domain.port.ITokenGenerator
import de.contagio.core.domain.port.ITokenRepository
import de.contagio.core.toSHA256
import de.contagio.core.util.TokenGenerator

class AuthenticateUser(
    private val tokenRepository: ITokenRepository,
    private val user: User,
    private val tokenGenerator: ITokenGenerator = TokenGenerator()
) {

    fun authenticate(password: String): String? {
        if (password.trim().isEmpty())
            throw IllegalArgumentException("password must not be empty")

        return if (password.toSHA256(user.salt) == user.passwordHash) {
            tokenRepository.saveTokenForUser(tokenGenerator.generate(), user)
        } else
            null
    }
}
