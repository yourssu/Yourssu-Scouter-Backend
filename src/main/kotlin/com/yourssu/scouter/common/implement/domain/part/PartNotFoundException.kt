package com.yourssu.scouter.common.implement.domain.part

class PartNotFoundException(
    override val message: String
) : RuntimeException(message)
