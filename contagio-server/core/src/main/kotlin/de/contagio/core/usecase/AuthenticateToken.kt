package de.contagio.core.usecase

import de.contagio.core.domain.entity.User
import de.contagio.core.domain.port.ITokenRepository

class AuthenticateToken(private val tokenRepository: ITokenRepository) {

    fun authenticate(token: String): User? {
        if (token.trim().isEmpty())
            throw IllegalArgumentException("token should not be empty")

        return tokenRepository.findUserUsingToken(token)
    }
}
