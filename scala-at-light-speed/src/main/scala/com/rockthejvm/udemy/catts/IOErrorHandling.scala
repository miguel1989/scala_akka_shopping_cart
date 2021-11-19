package com.rockthejvm.udemy.catts

import cats.effect.IO

import scala.util.{Failure, Success, Try}

object IOErrorHandling {

  val failedCompute: IO[Int] = IO.delay(throw new RuntimeException("AAAA"))
  val aFailure: IO[Int] = IO.raiseError(new RuntimeException("BBB")) //usi this

  val dealWithIt: IO[AnyVal] = aFailure.handleErrorWith {
    case _: RuntimeException => IO.delay(println("vsvdsvs"))
  }

  val effectAEither:IO[Either[Throwable, Int]] = aFailure.attempt
  val resultAsStr: IO[String] = aFailure.redeem(ex => s"fail ${ex.getMessage}", res => s"success ${res}")
  val resultAsEffect: IO[Unit] = aFailure.redeemWith(ex => IO.delay(println(s"fail ${ex.getMessage}")), res => IO.delay(println(s"success ${res}")))


  def option2IO[A](opt: Option[A])(ifEmpty: Throwable): IO[A] =
    opt match {
      case None => IO.raiseError(ifEmpty)
      case Some(res) => IO(res)
    }

  def try2IO[A](aTry: Try[A]): IO[A] =
    aTry match {
      case Success(value) => IO(value)
      case Failure(exception) => IO.raiseError(exception)
    }

  def either2IO[A](anEither: Either[Throwable, A]): IO[A] =
    anEither match {
      case Left(value) => IO.raiseError(value)
      case Right(value) => IO(value)
    }

  def handleIOError[A](io: IO[A])(handler: Throwable => A): IO[A] =
    io.redeem(ex => handler(ex), value => value)
//    io.attempt.map {
//      case Left(ex) => handler(ex)
//      case Right(value) => value
//    }

  def handleIOErrorWith[A](io: IO[A])(handler: Throwable => IO[A]): IO[A] =
//    io.redeemWith(handler, value => IO.pure(value))
    io.redeemWith(handler, IO.pure)
//    io.attempt.flatMap {
//      case Left(ex) => handler(ex)
//      case Right(value) => IO(value)
//    }

  def main(args:Array[String]): Unit = {
    import cats.effect.unsafe.implicits.global
    resultAsEffect.unsafeRunSync()

  }
}
