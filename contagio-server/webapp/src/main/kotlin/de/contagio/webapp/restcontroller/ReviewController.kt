package de.contagio.webapp.restcontroller

import de.contagio.core.domain.entity.LocationReviewStatistic
import de.contagio.core.domain.entity.ReviewProvider
import de.contagio.core.domain.entity.ReviewStatistic
import de.contagio.core.domain.entity.User
import de.contagio.webapp.service.authentication.ValidateAuthorization
import io.swagger.annotations.ApiImplicitParam
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
@RequestMapping(REVIEWS)
open class ReviewController {

    @GetMapping("statistic")
    @ValidateAuthorization
    @ApiImplicitParam(
        name = "Authorization",
        value = "Access Token",
        required = true,
        allowEmptyValue = false,
        paramType = "header",
        dataTypeClass = String::class,
        example = "Bearer access_token"
    )
    open fun getStatistic(user: User): ResponseEntity<LocationReviewStatistic> {
        val locationReviewStatistic =
            LocationReviewStatistic(
                user.locationId.toString(),
                listOf(
                    ReviewStatistic(ReviewProvider.GOLOCAL, 10, 4.2f, false)
                )
            )

        return ResponseEntity.ok(locationReviewStatistic)
    }
}
