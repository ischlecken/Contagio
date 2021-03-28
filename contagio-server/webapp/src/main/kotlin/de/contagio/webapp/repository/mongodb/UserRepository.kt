package de.contagio.webapp.repository.mongodb

import de.contagio.core.domain.entity.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, String>

