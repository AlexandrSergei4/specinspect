package com.alki.specinspect.data.models

import kotlinx.serialization.Serializable

/**
 * Пример Пользователь приложения
 */
@Serializable
data class User(
    val username: String,                    // Имя пользователя
    val avatar: String,                      // Аватар (эмодзи или URL)
    val shareId: String,                     // ID для добавления в друзья
)
