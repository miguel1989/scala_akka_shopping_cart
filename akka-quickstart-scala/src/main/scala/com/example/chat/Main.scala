package com.example.chat

import akka.NotUsed
import akka.actor.typed.{ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors

object Main {

  def apply(): Behavior[NotUsed] =
    Behaviors.setup(context => {
      val chatRoom = context.spawn(ChatRoom(), "chatRoom")
      val chatClient = context.spawn(ChatClient(), "chatClient")
      context.watch(chatClient)

      chatRoom ! ChatRoom.GetSession("MEDVED-SCREEN", chatClient)

      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    })

  def main(args: Array[String]): Unit = {
    ActorSystem(Main(), "chatRoomDemo")
  }
}
