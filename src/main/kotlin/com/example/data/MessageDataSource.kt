package com.example.data

import com.example.data.model.Message

interface MessageDataSource {

    suspend fun getAllMessage(): List<Message>

    suspend fun insertMessage(message: Message)
}