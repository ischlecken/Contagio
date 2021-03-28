package de.contagio.core.domain.port

import de.contagio.core.domain.entity.User

interface ITokenRepository {
    fun findUserUsingToken(token: String): User?
    fun saveTokenForUser(token: String,user: User): String
}
