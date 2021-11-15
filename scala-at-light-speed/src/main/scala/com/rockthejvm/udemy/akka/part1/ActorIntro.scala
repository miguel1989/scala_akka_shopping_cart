package com.rockthejvm.udemy.akka.part1

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorIntro extends App {
  val actorSystem = ActorSystem("firstSystem")

  class WordCountActor extends Actor {
    var totalWords = 0

    override def receive: PartialFunction[Any, Unit] = {
      case message: String => {
        println(" string msg ")
        totalWords += message.split(" ").length
      }
      case msg => println(s"i cant understand message $msg")
    }
  }

  val wordCounter: ActorRef = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  wordCounter ! "hello medved"

  object Person {
    def props(name: String): Props = Props(new Person(name))
  }

  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case msg: String => println(s"Hi, $name")
    }
  }

  val personActor = actorSystem.actorOf(Person.props("john"))
  personActor ! "aaa"

  object Counter {
    case class Increment()

    case class Decrement()

    case class PrintCounter()
  }

  class Counter extends Actor {

    import Counter._

    var counter = 0

    override def receive: Receive = {
      case Increment => counter += 1
      case Decrement => counter -= 1
      case PrintCounter => println(s"Counter is $counter")
    }
  }

  val counterActor = actorSystem.actorOf(Props[Counter], "simpleCounter")
  counterActor ! Counter.Increment
  counterActor ! Counter.PrintCounter
  counterActor ! Counter.Decrement
  counterActor ! Counter.PrintCounter

  case class Deposit(amount: Double, replyTo: ActorRef)

  case class Withdraw(amount: Double, replyTo: ActorRef)

  case class Statement(replyTo: ActorRef)

  case class Success(msg: String)

  case class Failure(msg: String)

  class BankAccount extends Actor {
    var amount: Double = 0

    override def receive: Receive = {
      case Deposit(am, replyTo) =>
        if (am < 0)
          replyTo ! Failure("invalid deposit")
        else {
          amount += am
          replyTo ! Success(s"$am added to bank account")
        }
      case Withdraw(am, replyTo) =>
        if (am < 0)
          replyTo ! Failure(s"cant withdraw $am")
        else if (am > amount)
          replyTo ! Failure(s"cant withdraw $am")
        else {
          amount -= am
          replyTo ! Success(s"$am withdrawed from bank account")
        }
      case Statement(replyTo) => replyTo ! Success(s"amount is $amount")
    }
  }

  class BankPrinter extends Actor {
    override def receive: Receive = {
      case Success(msg) => println(s"[success] $msg")
      case Failure(msg) => println(s"[failure] $msg")
    }
  }

  val bankAccount = actorSystem.actorOf(Props[BankAccount], "bank")
  val bankPrinter = actorSystem.actorOf(Props[BankPrinter], "bankPrinter")

  bankAccount ! Deposit(100, bankPrinter)
  bankAccount ! Statement(bankPrinter)
  bankAccount ! Withdraw(105, bankPrinter)
  bankAccount ! Statement(bankPrinter)
  bankAccount ! Withdraw(10, bankPrinter)
  bankAccount ! Statement(bankPrinter)
}
