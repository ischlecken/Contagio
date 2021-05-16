package de.contagio.webapp.util

import de.contagio.core.domain.port.PageRequest
import de.contagio.core.domain.port.PagedResult
import de.contagio.core.domain.port.SortDirection
import de.contagio.core.domain.port.Sorting
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

val defaultSort = listOf(Sorting("updated", SortDirection.desc))

fun PageRequest.toPageRequest(): org.springframework.data.domain.PageRequest {
    val sort = mutableListOf<Sort.Order>()

    this.sort.forEach {
        sort.add(
            Sort.Order(
                when (it.direction) {
                    SortDirection.asc -> Sort.Direction.ASC
                    SortDirection.desc -> Sort.Direction.DESC
                },
                it.column
            )
        )
    }

    return org.springframework.data.domain.PageRequest.of(pageNo, pageSize, Sort.by(sort))
}

fun Pageable.toPageRequest(): PageRequest {
    val sort = mutableListOf<Sorting>()

    this.sort.forEach {
        sort.add(
            Sorting(
                column = it.property,
                direction = when( it.direction) {
                    Sort.Direction.DESC -> SortDirection.desc
                    Sort.Direction.ASC -> SortDirection.asc
                    else -> SortDirection.asc
                }
            )
        )
    }

    return PageRequest(
        pageNo = this.pageNumber,
        pageSize = this.pageSize,
        sort = sort
    )
}

fun <T> org.springframework.data.domain.Page<T>.toPagedResult(): PagedResult<T> {
    return PagedResult(
        content = this.content,
        isFirst = this.isFirst,
        isLast = this.isLast,
        pageNo = this.pageable.pageNumber,
        pageSize = this.pageable.pageSize,
        totalElements = this.totalElements.toInt(),
        totalPages = this.totalPages
    )
}
