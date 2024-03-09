package com.ensias.sportnet.models

data class Post(
    val id: String = "",
    val userId: String = "",
    val userProfileUrl: String = "",
    val username: String = "",
    val roomId: String = "",
    var text: String = "",
    val contentUrl: String = "",
    val time: Long = 0,
    var likes: Int = 0,
    var shares: Int = 0,
    var views: Int = 0,
    var comments: Int = 0,
)
