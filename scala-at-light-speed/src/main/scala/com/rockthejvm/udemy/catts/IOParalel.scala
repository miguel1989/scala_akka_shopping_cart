package com.rockthejvm.udemy.catts

import cats.Parallel
import cats.effect.IO.Par
import cats.effect.{IO, IOApp}

object IOParalel extends IOApp.Simple {

  import cats.syntax.apply._

  val aniIO: IO[String] = IO((s"[${Thread.currentThread().getName}] Ani"))
  val kamranIO: IO[String] = IO((s"[${Thread.currentThread().getName}] Kamran"))

  val composedIO: IO[String] = for {
    ani <- aniIO
    kamran <- kamranIO
  } yield s"$ani and $kamran"

  val meaningOfLife: IO[Int] = IO.delay(42)
  val favoriteLang: IO[String] = IO.delay("scala")
  val goalInLife: IO[String] = (meaningOfLife.debugCustom, favoriteLang.debugCustom).mapN((num, str) => num.toString + " " + str)

  //paralellism
  import cats.effect.implicits._
  val parallelIO1: IO.Par[Int] = Parallel[IO].parallel(meaningOfLife.debugCustom)
  val parallelIO2: IO.Par[String] = Parallel[IO].parallel(favoriteLang.debugCustom)
  val goalInLifeParallel: IO.Par[String] = (parallelIO1, parallelIO2).mapN((num, str) => num.toString + " " + str)
  val seqGoalInLife: IO[String] = Parallel[IO].sequential(goalInLifeParallel)

  import cats.syntax.parallel._
  val goalInLife_v3: IO[String] = (meaningOfLife.debugCustom, favoriteLang.debugCustom).parMapN((num, str) => num.toString + " " + str)


  val aFailure: IO[String] = IO.raiseError(new RuntimeException("aaa"))
  val parallelWithFail: IO[String] = (favoriteLang.debugCustom, aFailure.debugCustom).parMapN(_ + _)

  override def run: IO[Unit] =
//    seqGoalInLife.map(println)
    parallelWithFail.debugCustom.void
}
