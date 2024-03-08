package com.ensias.sportnet.models

data class User(
    val id: String = "",
    val username: String = "",
    var description: String = "",
    val email: String = "",
    var profilePictureUrl: String = "",
    val likes: String = "",
    val commentsLikes: String = "",
    val createdAt: Long = 0,
)
