package com.abdelhakim.sportnet.models

data class Room (
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val members: Int = 0,
    val logoUrl: String? = null,
    val bannerUrl: String? = null
)