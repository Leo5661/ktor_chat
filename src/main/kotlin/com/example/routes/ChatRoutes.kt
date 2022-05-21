package com.example.routes

import com.example.room.MemberAlreadyExistsException
import com.example.room.RoomController
import com.example.sessions.ChatSession
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlin.Exception

fun Route.chatSocket(roomController: RoomController){
    webSocket("/chat-socket") {
        val session = call.sessions.get<ChatSession>()
        if(session == null){
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No Session."))
            return@webSocket
        }

        try {
            roomController.onJoin(
                userName = session.userName,
                sessionId = session.sessionId,
                socket = this
            )
            incoming.consumeEach { frame ->
                if( frame is Frame.Text){
                    roomController.sendMessage(
                        senderUserName = session.userName,
                        message = frame.readText()
                    )
                }
            }
        } catch (e: MemberAlreadyExistsException){
            call.respond(HttpStatusCode.Conflict)
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            roomController.tryDisconnect(session.userName)
        }


    }
}

fun Route.getAllMessages(roomController: RoomController){
    get("/messages"){
        call.respond(
            HttpStatusCode.OK,
            roomController.getAllMessage()
        )
    }
}