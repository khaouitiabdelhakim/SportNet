package com.abdelhakim.sportnet.models

data class Room(
    val id: String,
    val title: String,
    val description: String,
    val members: Int,
    val logoUrl: String?,
    val bannerUrl: String?
)