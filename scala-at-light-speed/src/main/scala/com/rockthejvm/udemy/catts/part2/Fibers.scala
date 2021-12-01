package com.rockthejvm.udemy.catts.part2

import cats.effect.kernel.Outcome
import cats.effect.{Fiber, FiberIO, IO, IOApp, ParallelF}
import com.rockthejvm.udemy.catts.DebugWrapper

object Fibers extends IOApp.Simple {

  val meaning: IO[Int] = IO.pure(42)
  val favoriteLang: IO[String] = IO.pure("Scala")

  def simpleIOComposition(): IO[Unit] = for {
    mol <- meaning.debugCustom
    lang <- favoriteLang.debugCustom
  } yield ()

  val aFiber: IO[FiberIO[Int]] = meaning.debugCustom.start

  def differentThreadIOs(): IO[Unit] = for {
    _ <- aFiber
    _ <- favoriteLang.debugCustom
  } yield ()

  def runOnSomeOtherThread[A](io: IO[A]): IO[Outcome[IO, Throwable, A]] = for {
    fib <- io.start
    result <- fib.join //an effect which waits for the fiber to terminate
  } yield result

  def throwOnAnotherThread() = for {
    fib <- IO.raiseError[Int](new RuntimeException("medved err")).start
    result <- fib.join
  } yield result

  override def run: IO[Unit] = runOnSomeOtherThread(meaning).debugCustom.void
}
