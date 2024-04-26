package com.fogsim.fog.core.models

data class Task(
    val duration: Long,
    val data: Data,
    val analyzedData: AnalyzedData? = null,
    val createdAt: Long = System.currentTimeMillis()
)