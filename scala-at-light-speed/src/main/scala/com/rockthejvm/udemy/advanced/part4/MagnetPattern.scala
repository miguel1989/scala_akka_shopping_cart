package com.rockthejvm.udemy.advanced.part4

import scala.concurrent.Future

object MagnetPattern extends App {
  //method overloading problem

  case class P2PRequest()

  case class P2PResponse()

  trait Actor {
    def receive(statusCode: Int): Int

    def receive(status: String): Int

    def receive(req: P2PRequest): Int

    def receive(resp: P2PResponse): Int
    //lots of other overloads
  }

  trait MessageMagnet[Result] {
    def apply(): Result
  }

  def receive[R](magnet: MessageMagnet[R]): R = magnet()

  implicit class FromP2PRequest(request: P2PRequest) extends MessageMagnet[Int] {
    def apply(): Int = {
      println("handling p2p request")
      42
    }
  }
  implicit class FromP2PResponse(response: P2PResponse) extends MessageMagnet[Int] {
    def apply(): Int = {
      println("handling p2p response")
      72
    }
  }

  implicit class FromP2PFutureResponse(future: Future[P2PResponse]) extends MessageMagnet[Int] {
    def apply(): Int = 1
  }
  implicit class FromP2PFutureRequest(future: Future[P2PRequest]) extends MessageMagnet[Int] {
    def apply(): Int = 2
  }

  receive(P2PRequest())
  receive(P2PResponse())

  trait MathLib {
    def addOne(x: Int): Int = x + 1
    def addOne(s: String): Int = s.toInt + 1
  }

  trait AddMagnet {
    def apply(): Int
  }

  def add1(magnet: AddMagnet): Int = magnet()

  implicit class AddInt(x:Int) extends AddMagnet {
    override def apply(): Int = x + 1
  }
  implicit class AddString(s:String) extends AddMagnet {
    override def apply(): Int = s.toInt + 1
  }

  val addF = add1 _
  println(addF(1))
  println(addF("123"))
}
