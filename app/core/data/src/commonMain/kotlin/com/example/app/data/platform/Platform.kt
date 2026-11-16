package com.example.app.data.platform

/** Базовый URL API: в браузере — origin страницы, на Android — из настроек сборки (см. androidApp/build.gradle.kts). */
internal expect val apiBaseUrl: String

internal expect fun platformStorageGet(key: String): String?
internal expect fun platformStorageSet(key: String, value: String)
