package com.rockthejvm.udemy.catts

import cats.effect.{ExitCode, IO, IOApp}
import com.rockthejvm.udemy.catts.IOErrorHandling.resultAsEffect

import scala.io.StdIn

object IOApps {

  def smallProg(): IO[Unit] = for {
    line1 <- IO(StdIn.readLine())
    _ <- IO.delay(println("you wrote " + line1))
  } yield ()

  def main(args:Array[String]): Unit = {
    import cats.effect.unsafe.implicits.global
    smallProg.unsafeRunSync()
  }
}

object TestApp extends IOApp {
  import IOApps._
  override def run(args: List[String]): IO[ExitCode] = {
//    smallProg().map(_ => ExitCode.Success)
    smallProg().as(ExitCode.Success)
  }
}

object TestAppSimple extends IOApp.Simple {
  import IOApps._
  override def run: IO[Unit] = smallProg()
}
