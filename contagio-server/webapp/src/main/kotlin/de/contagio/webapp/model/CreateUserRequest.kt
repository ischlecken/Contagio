package de.contagio.webapp.model

data class CreateUserRequest(val name: String, val password: String, val locationId: Int = 0)
