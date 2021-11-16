package com.rockthejvm.udemy.akka.part2

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

object StartStopActors extends App {

  val system = ActorSystem("aaa")

  object Parent {
    case class StartChild(name:String)
    case class StopChild(name: String)
    case object Stop
  }
  class Parent extends Actor with ActorLogging {
    import Parent._
    override def receive: Receive = withChildren(Map())

    def withChildren(children: Map[String, ActorRef]): Receive = {
      case StartChild(name) => log.info("Starting child {}", name)
        context.become(withChildren(children + (name -> context.actorOf(Props[Child], name))))
      case StopChild(name) =>
        log.info("Stopping child {}", name)
        children.get(name).foreach(childRef => context.stop(childRef))
      case Stop =>
        log.info("stop my self")
        context.stop(self)
    }
  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }

  import Parent._
  val parent = system.actorOf(Props[Parent], "parent")
  parent ! StartChild("medved")
  parent ! StartChild("medved2")
//  val child = system.actorSelection("/user/parent/medved")
//  child ! "hi"

  parent ! Stop
}
