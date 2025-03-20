package com.example.eclinic.firebase

data class Chat(
    var id: String = "",
    var senderId: String = "",
    var receiverId: String = "",
    var message: String = "",
    var timestamp: String = ""
)