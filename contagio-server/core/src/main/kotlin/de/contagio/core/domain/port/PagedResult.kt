package de.contagio.core.domain.port

data class PagedResult<T>(
    val content: List<T> = emptyList(),
    val isFirst: Boolean = false,
    val isLast: Boolean = false,
    val pageNo: Int = 0,
    val pageSize: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Int = 0
) {
    val hasContent = content.isNotEmpty()
}

data class PageRequest(val pageNo: Int = 0, val pageSize: Int = 10, val sort: Collection<Sorting> = emptyList())

@Suppress("EnumEntryName")
enum class SortDirection {
    asc, desc
}

data class Sorting(val column: String, val direction: SortDirection = SortDirection.asc)
