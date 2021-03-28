package de.contagio.core.domain.entity

import org.springframework.data.annotation.Id

data class User(@Id val name: String, val passwordHash: String, val salt: String, val locationId: Int)
