package de.contagio.core.util

import de.contagio.core.domain.entity.User
import de.contagio.core.domain.port.ITokenRepository

class MemoryTokenRepository : ITokenRepository {
    private var users = mutableMapOf<String, User>()

    override fun findUserUsingToken(token: String): User? {
        return users[token]
    }

    override fun saveTokenForUser(token: String,user: User): String {
        users[token] = user

        return token
    }
}
