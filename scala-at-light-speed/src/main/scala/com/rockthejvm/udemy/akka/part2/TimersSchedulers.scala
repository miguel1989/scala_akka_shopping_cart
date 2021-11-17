package com.rockthejvm.udemy.akka.part2

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

import scala.concurrent.duration.DurationInt

object TimersSchedulers extends App {

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }

  val system = ActorSystem("aaa")
  val simpleActor = system.actorOf(Props[SimpleActor])

  system.scheduler.scheduleOnce(1.second) {
    simpleActor ! "reminder"
  }(system.dispatcher)
}
