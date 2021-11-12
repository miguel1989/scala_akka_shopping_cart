package com.rockthejvm.udemy.advanced.part3

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Random, Success, Try}

object Promises extends App {
  val promise = Promise[Int]() //controller over the future
  val future = promise.future

  future.onComplete {
    case Success(r) => println(s"[consumer] i've received $r")
  }

  val producer = new Thread(() => {
    println("[producer] creating numbers")
    Thread.sleep(300)
    promise.success(42)
    println("[producer] is done")
  })

  producer.start()
  Thread.sleep(1000)

  //excercise
  //fulfill immediately
  println(Future.successful(32))

  def inSeq(future1: Future[Int], future2: Future[Int]): Future[Int] = {
    //future1.flatMap(_ => future2)
    for {
      res1 <- future1
      res2 <- future2
    } yield res2
  }

  def first[A](first: Future[A], second:Future[A]): Future[A] = {
    val promise = Promise[A]
    def tryComplete(promise: Promise[A], result: Try[A]) = result match {
      case Success(r) => try {
        promise.success(r)
      } catch {
        case _ =>
      }
      case Failure(e) => try {
        promise.failure(e)
      } catch {
        case _ =>
      }
    }
    first.onComplete(tryComplete(promise, _))
    second.onComplete(promise.tryComplete)

    promise.future
  }

  def retryUntil[T](action: () => Future[T], predicate: T => Boolean): Future[T] = {
    action().filter(predicate).recoverWith{
      case _=> retryUntil(action, predicate)
    }
  }

  val future1 = Future { 222 }
  val future2 = Future { 111 }
  inSeq(future1, future2).foreach(println)

  val random = new Random()
  val action = () => Future {
    val num = random.nextInt(100)
    println(s"num is $num")
    num
  }
  val predicate = (x:Int) => x < 50

  retryUntil(action, predicate).foreach(println)
}
