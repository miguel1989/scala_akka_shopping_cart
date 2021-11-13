package com.rockthejvm.udemy.akka

import akka.actor.ActorSystem

object Playground extends App {
  val actorSystem = ActorSystem("Medved")
  println(actorSystem.name)
}
