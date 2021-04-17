package de.contagio.webapp.model

import io.swagger.annotations.ApiModelProperty


data class SwaggerPageable(
    @ApiModelProperty("Number of records per page", example = "20")
    val size: Int?,

    @ApiModelProperty("Results page you want to retrieve (0..N)", example = "0")
    val page: Int?,

    @ApiModelProperty("Sorting criteria in the format: property(,asc|desc)." +
            "Default sort order is ascending. Multiple sort criteria are supported.")
    var sort: String?
)
