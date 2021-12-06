package com.rockthejvm.udemy.catts.part2

import cats.effect.kernel.Outcome
import cats.effect.kernel.Outcome.{Canceled, Errored, Succeeded}
import cats.effect.{Fiber, FiberIO, IO, IOApp, ParallelF}
import com.rockthejvm.udemy.catts.DebugWrapper

import scala.concurrent.duration.FiniteDuration

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

  def processResultsFromFiber[A](io: IO[A]): IO[A] = {
    val result = for {
      fib <- io.debugCustom.start
      result <- fib.join //an effect which waits for the fiber to terminate
    } yield result

    result flatMap {
      case Succeeded(fa) => fa
      case Errored(e) => IO.raiseError(e)
      case Canceled() => IO.raiseError(new RuntimeException("aaa"))
    }
  }

  def tupleIOS[A,B](ioa: IO[A], iob: IO[B]): IO[(A,B)] = {
    val result = for {
      fib1 <- ioa.debugCustom.start
      fib2 <- iob.debugCustom.start
      result1 <- fib1.join //an effect which waits for the fiber to terminate
      result2 <- fib2.join //an effect which waits for the fiber to terminate
    } yield (result1, result2)

    result.flatMap {
      case (Succeeded(fa), Succeeded(fb)) => for {
        a <- fa
          b<- fb
      } yield (a, b)
      case _ => IO.raiseError(new RuntimeException("cancelled"))
    }
  }

  def timeout[A](io: IO[A], duration: FiniteDuration): IO[A] = {
    val computation = for {
      fib <- io.start
      _ <- (IO.sleep(duration) >> fib.cancel).start //careful - can leak
      result <- fib.join
    } yield result

    computation flatMap {
      case Succeeded(fa) => fa
      case _ => IO.raiseError(new RuntimeException("aaa"))
    }
  }

  override def run: IO[Unit] = runOnSomeOtherThread(meaning).debugCustom.void
}
