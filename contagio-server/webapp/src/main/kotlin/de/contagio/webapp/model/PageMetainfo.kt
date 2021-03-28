package de.contagio.webapp.model

enum class HtmlLinkType(val value: String) {
    PREV("prev"), NEXT("next"), CANONICAL("canonical")
}

data class PageMetainfo(
    val title: String? = null,
    val description: String? = null,
    val robots: String? = null,
    val home: String? = null,
    val imprint: String? = null,
    val links: Map<HtmlLinkType, String> = emptyMap()
)
