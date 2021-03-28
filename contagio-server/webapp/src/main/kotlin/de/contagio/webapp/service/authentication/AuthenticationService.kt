package de.contagio.webapp.service.authentication

import de.contagio.core.domain.entity.User
import de.contagio.core.domain.port.ITokenRepository
import de.contagio.core.util.MemoryTokenRepository
import org.springframework.stereotype.Service

@Service
open class AuthenticationService : ITokenRepository {
    private val tokenRepository = MemoryTokenRepository()

    override fun findUserUsingToken(token: String): User? {
        return tokenRepository.findUserUsingToken(token)
    }

    override fun saveTokenForUser(token: String, user: User): String {
        return tokenRepository.saveTokenForUser(token, user)
    }

}
