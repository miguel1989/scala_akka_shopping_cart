package com.example.chat

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object ChatClient {
  def apply(): Behavior[ChatRoom.SessionEvent] = {
    Behaviors.receive((context, message) => {
      message match {
        case ChatRoom.SessionGranted(handle) =>
          handle ! ChatRoom.PostMessage("Preved Medved")
          Behaviors.same
        case ChatRoom.MessagePosted(screenName: String, msg: String) =>
          context.log.info("message has been posted by '{}': {}", screenName, msg)
          Behaviors.stopped
      }
    })
  }
}
