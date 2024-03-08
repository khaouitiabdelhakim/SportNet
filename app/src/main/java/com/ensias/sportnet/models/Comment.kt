package com.ensias.sportnet.models

data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val profilePictureUrl: String = "",
    val comment: String = "",
    val createdAt: Long = 0,
    var likes:Int = 0
)