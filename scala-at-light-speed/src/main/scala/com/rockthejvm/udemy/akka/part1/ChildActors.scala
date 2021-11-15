package com.rockthejvm.udemy.akka.part1

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import com.rockthejvm.udemy.akka.part1.ChildActors.Parent.{CreateChild, TellChild}

import scala.util.Random

object ChildActors extends App {
  object Parent {
    case class CreateChild(name: String)

    case class TellChild(message: String)
  }

  class Parent extends Actor {

    import Parent._

    override def receive: Receive = {
      case CreateChild(name) =>
        println("Creating child")
        val child = context.actorOf(Props[Child], name)
        context.become(withChild(child))
    }

    def withChild(child: ActorRef): Receive = {
      case TellChild(msg) => child forward msg
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case msg: String => println(s"${self.path} i got message ${msg}")
    }
  }

  val system = ActorSystem("behavior")
  //  val parent = system.actorOf(Props[Parent], "parent")
  //
  //  parent ! CreateChild("medved")
  //  parent ! TellChild("preved")
  //
  //  val childSelection = system.actorSelection("/user/parent/medved")
  //  childSelection ! "aaa"

  case class Initialize(nChildren: Int)

  case class WordCountTask(id: Int, text: String)

  case class WordCountReply(id: Int, count: Int)

  class WordCounterMaster extends Actor {
    val logger: LoggingAdapter = Logging(context.system, this)

    override def receive: Receive = {
      case Initialize(num) =>
        logger.info(s"init with $num")
        val workers = for (i <- 1 to num) yield context.actorOf(Props[WordCounterWorker], s"worker$i")
        context.become(withChildren(workers))
    }

    def withChildren(workers: Seq[ActorRef], currIdx: Int = 0, currTaskId: Int = 0, requestMap: Map[Int, ActorRef] = Map()): Receive = {
      case msg: String =>
        println(s"processing msg $msg")
        val originalSender = sender()
        val worker = workers(currIdx)
        worker ! WordCountTask(currTaskId, msg)
        val nextIdx = (currIdx + 1) % workers.length
        val newRequestMap = requestMap + (currTaskId -> originalSender)
        context.become(withChildren(workers, nextIdx, currTaskId + 1, newRequestMap))

      case WordCountReply(id, count) =>
        println(s"i have received reqId = ${id} with count = ${count}")
        val sender = requestMap(id)
        sender ! count
        context.become(withChildren(workers, currTaskId, currTaskId + 1, requestMap - id))
    }
  }

  class WordCounterWorker extends Actor with ActorLogging {
    val random = new Random()

    override def receive: Receive = {
      case WordCountTask(id, text) =>
        log.info("child [{}] doing task {} with id ${}", self.path, text, id)
        Thread.sleep(random.nextInt(300))
        sender() ! WordCountReply(id, text.split(" ").length)
    }
  }

  val master = system.actorOf(Props[WordCounterMaster])
  master ! Initialize(1)
  master ! "hello my name is miguel"
  master ! "medved preved"
  master ! "scaa 4ever and 4ever"
}
