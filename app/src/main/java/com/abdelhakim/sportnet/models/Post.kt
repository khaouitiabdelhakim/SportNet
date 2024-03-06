package com.abdelhakim.sportnet.models

data class Post(
    val id: String,
    val userId: String,
    val roomId: String,
    val text: String,
    val contentUrl: String?,
    val time: Long,
    val likes: Int,
    val shares: Int,
    val views: Int
)