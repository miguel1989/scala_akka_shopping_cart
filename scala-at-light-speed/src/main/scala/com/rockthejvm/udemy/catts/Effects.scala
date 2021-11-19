package com.rockthejvm.udemy.catts

import scala.io.StdIn

object Effects {

  case class MyIO[A](unsafeRun: () => A) {
    def map[B](f: A => B): MyIO[B] = MyIO(() => f(unsafeRun()))

    def flatMap[B](f: A => MyIO[B]): MyIO[B] = MyIO(() => f(unsafeRun()).unsafeRun())
  }

  val anIO: MyIO[Int] = MyIO(() => {
    println("aaa")
    42
  })

  val currTimeIO: MyIO[Long] = MyIO(() => System.currentTimeMillis())

  def measure[A](computation: MyIO[A]): MyIO[Long] = for {
      startTime <- currTimeIO
      _ <- computation
      finishTime <- currTimeIO
    } yield finishTime - startTime

  def putStrLn(line: String): MyIO[Unit] = MyIO(() => println(line))

  val readLn: MyIO[String] = MyIO(() => StdIn.readLine())

  def testConsole(): Unit = {
    val prog = for {
      line1 <- readLn
      line2 <- readLn
      _ <- putStrLn(line1 + line2)
    } yield ()

    prog.unsafeRun()
  }

  def main(args:Array[String]): Unit = {
    anIO.unsafeRun()

    val measureTest = measure(MyIO(() => Thread.sleep(777)))
    println(measureTest)
    println(measureTest.unsafeRun())

    testConsole()
  }
}
