package de.contagio.webapp.restcontroller

import de.contagio.core.domain.entity.User
import de.contagio.core.usecase.AuthenticateUser
import de.contagio.core.usecase.CreateUser
import de.contagio.webapp.model.CreateUserRequest
import de.contagio.webapp.repository.mongodb.UserRepository
import de.contagio.webapp.service.authentication.AuthenticationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping(USERS)
open class UserController(
    private val userRepository: UserRepository,
    private val authenticationService: AuthenticationService
) {

    @GetMapping
    open fun getUser(@RequestParam name: String): ResponseEntity<User> {
        val result = userRepository.findById(name)

        return if (result.isPresent) ResponseEntity.ok(result.get()) else ResponseEntity.notFound().build()
    }

    @GetMapping("/all")
    open fun allUses(): Collection<User> {
        return userRepository.findAll()
    }

    @GetMapping("/authenticate")
    open fun authenticateUser(@RequestParam name: String, @RequestParam password: String): ResponseEntity<String> {
        val user = userRepository.findById(name)
        if (!user.isPresent)
            return ResponseEntity.notFound().build()

        return AuthenticateUser(authenticationService, user.get()).authenticate(password)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }


    @PostMapping
    open fun createOrUpdateUser(@RequestBody createUserRequest: CreateUserRequest): User {
        return userRepository.save(
            CreateUser(
                name = createUserRequest.name,
                password = createUserRequest.password,
                locationId = createUserRequest.locationId
            ).build()
        )
    }

    @DeleteMapping
    open fun deleteUser(@RequestParam name: String): ResponseEntity<Unit> {
        var result = ResponseEntity.notFound().build<Unit>()

        if (userRepository.existsById(name)) {
            userRepository.deleteById(name)
            result = ResponseEntity.ok().build()
        }

        return result
    }
}
