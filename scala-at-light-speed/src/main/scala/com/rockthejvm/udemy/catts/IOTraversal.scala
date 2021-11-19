package com.rockthejvm.udemy.catts

import cats.Traverse
import cats.effect.{IO, IOApp}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object IOTraversal extends IOApp.Simple {

  def heavyComputation(str: String): Future[Int] = Future {
    Thread.sleep(1000)
    str.split(" ").length
  }

  val workLoad: List[String] = List(" I Lke cats", "Scala is very awesome", "preved medved")
  import cats.instances.list._
  val listTraverse: Traverse[List] = Traverse[List]

  def clunckyFutures(): Unit = {
    //would be hard to obrain the Future[List[Int]]
    val futures: List[Future[Int]] = workLoad.map(heavyComputation)
    futures.foreach(_.foreach(println))
  }

  def traverseFutures(): Unit = {
    val singleFuture: Future[List[Int]] = listTraverse.traverse(workLoad)(heavyComputation)
    singleFuture.foreach(println)
  }

  def computeAsIO(str: String): IO[Int] = IO {
    Thread.sleep(1000)
    str.split(" ").length
  }.debugCustom

  val ios: List[IO[Int]] = workLoad.map(computeAsIO)
  val singleIo: IO[List[Int]] = listTraverse.traverse(workLoad)(computeAsIO)

  import cats.syntax.parallel._
  val parallelSingleIo: IO[List[Int]] = workLoad.parTraverse(computeAsIO)

  def sequence[A](listOfIOs: List[IO[A]]): IO[List[A]] =
    Traverse[List].traverse(listOfIOs)(x => x)

  def sequence[M[_]: Traverse, A](listOfIOs: M[IO[A]]): IO[M[A]] =
    Traverse[M].traverse(listOfIOs)(x => x)

  def parSequence[M[_]: Traverse, A](listOfIOs: M[IO[A]]): IO[M[A]] =
    listOfIOs.parTraverse(x => x)

  override def run: IO[Unit] = {
    sequence(ios).map(_.sum).debugCustom.void
    //parallelSingleIo.map(_.sum).debugCustom.void
  }
  //    singleIo.void
}
