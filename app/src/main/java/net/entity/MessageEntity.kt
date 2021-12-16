package net.entity

import java.io.Serializable

data class MessageEntity(
    var phone: String,
    var content: String,
    var date: String,
    var type: String
):Serializable
