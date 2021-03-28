package de.contagio.webapp.service.data

import org.springframework.data.domain.Sort

val qualitySort: Sort = Sort.by(Sort.Direction.DESC, "quality")
