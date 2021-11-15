package com.rockthejvm.udemy.akka.part1

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.rockthejvm.udemy.akka.part1.ActorBehavior.Mom.MomStart

object ActorBehavior extends App {

  object FussyKid {
    case object KidAccept
    case object KidReject

    val HAPPY = "happy"
    val SAD = "sad"
  }

  class FussyKid extends Actor {
    import FussyKid._
    import Mom._

    var state: String = HAPPY

    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) =>
        if (state == HAPPY)
          sender() ! KidAccept
        else sender() ! KidReject
    }
  }

  class StatelessKid extends Actor {
    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, discardOld = false)
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept
    }
    def sadReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false)
      case Food(CHOCOLATE) => context.unbecome()
      case Ask(_) => sender() ! KidReject
    }
  }

  object Mom {
    case class MomStart(kidRef: ActorRef)
    case class Food(food: String)
    case class Ask(message: String)

    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocoalte"
  }

  class Mom extends Actor {
    import Mom._
    import FussyKid._
    override def receive: Receive = {
      case MomStart(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Ask("do you want to play")
      case KidAccept => println("my kid accepted")
      case KidReject => println("my kid rejected")
    }
  }

  val system = ActorSystem("behavior")
  val kid = system.actorOf(Props[FussyKid])
  val stateLessKid = system.actorOf(Props[StatelessKid])
  val mom = system.actorOf(Props[Mom])
  mom ! MomStart(stateLessKid)



  object Counter {
    case class Increment()
    case class Decrement()
    case class PrintCounter()
  }

  class Counter extends Actor {
    import Counter._

    override def receive: Receive = countReceive()

    def countReceive(currCount: Int = 0): Receive = {
      case Increment => context.become(countReceive(currCount + 1))
      case Decrement => context.become(countReceive(currCount - 1))
    }

  }
}
