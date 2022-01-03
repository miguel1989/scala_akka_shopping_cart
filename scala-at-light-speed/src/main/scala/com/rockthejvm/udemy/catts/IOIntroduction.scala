package com.rockthejvm.udemy.catts

import cats.effect.IO

import scala.io.StdIn

object IOIntroduction {

  val firstIO: IO[Int] = IO.pure(42) //no side effects
  val delayedIO: IO[Int] = IO.delay {
    println("medved")
    52
  }

  val delayedIO2: IO[Int] = IO { // apply == delay
    println("medved2")
    62
  }

  def smallProg(): IO[Unit] = for {
   line1 <- IO(StdIn.readLine())
   line2 <- IO(StdIn.readLine())
    _ <- IO.delay(println(line1 + line2))
  } yield ()

  //mapN combine effects as tuples
  import cats.syntax.apply._
  val combined: IO[Int] = (firstIO, delayedIO).mapN(_ + _)

  def smallProg2(): IO[Unit] = (IO(StdIn.readLine()),IO(StdIn.readLine())).mapN(_ + _).map(println)

  //Exercises
  //1
  def seqAndTakeLast[A,B](ioa: IO[A], iob: IO[B]): IO[B] = for {
    _ <- ioa
    iobRes <- iob
  } yield iobRes
  def seqAndTakeLast_v2[A,B](ioa: IO[A], iob: IO[B]): IO[B] =
    ioa.flatMap(_ => iob)
  def seqAndTakeLast_v3[A,B](ioa: IO[A], iob: IO[B]): IO[B] =
    ioa *> iob //andThen operator
  def seqAndTakeLast_v4[A,B](ioa: IO[A], iob: IO[B]): IO[B] =
    ioa >> iob //andThen with by-name call

  def seqAndTakeFirst[A,B](ioa: IO[A], iob: IO[B]): IO[A] = for {
    ioaRes <- ioa
    _ <- iob
  } yield ioaRes
  def seqAndTakeFirst_v2[A,B](ioa: IO[A], iob: IO[B]): IO[A] =
    ioa.flatMap(aRes => iob.map(_ => aRes))

  def seqAndTakeFirst_v3[A,B](ioa: IO[A], iob: IO[B]): IO[A] =
    ioa <* iob

  //3
  import cats.effect.unsafe.implicits.global
  def forever[A](io: IO[A]): IO[A] =
    io.flatMap(_ => forever(io))
//    io.flatMap(_ => {
//      val newIo = IO(io.unsafeRunSync())
//      forever(newIo)
//    })
  def forever2[A](io: IO[A]): IO[A] =
    io >> forever2(io)

  def forever3[A](io: IO[A]): IO[A] =
    io *> forever3(io)

  def forever4[A](io: IO[A]): IO[A] =
    io.foreverM

  //4
  def convert[A,B](ioa: IO[A], value: B): IO[B] =
    ioa.map(_ => value)
  def convert2[A,B](ioa: IO[A], value: B): IO[B] =
    ioa.as(value)

  //5
  def asUnit[A](ioa: IO[A]): IO[Unit] =
    ioa.map(_ => ())
  def asUnit2[A](ioa: IO[A]): IO[Unit] =
    ioa.as(())//dont use this - not readable
  def asUnit3[A](ioa: IO[A]): IO[Unit] =
    ioa.void

  //6 - fix recursion
  def sum(n: Int): Int =
    if (n <= 0) 0
    else n + sum(n - 1)

  def sumIO(n:Int): IO[Int] =
    if (n <= 0) IO(0)
    else for {
      lastNum <- IO(n)
      prevSum <- sumIO(n - 1)
    } yield lastNum + prevSum


  //7 write fibbonacchi
  def fibo(n:Int): IO[BigInt] = {
    if (n < 2) IO(1)
    else for {
      last <- IO(fibo(n - 1)).flatten //the same as flatmap(x => x)
      prev <- IO.defer(fibo(n - 2))
    } yield last + prev
  }

  def main(args:Array[String]): Unit = {
    import cats.effect.unsafe.implicits.global
//    println(firstIO.unsafeRunSync())
//    println(delayedIO.unsafeRunSync())
//    println(combined.unsafeRunSync())
//    println(smallProg().unsafeRunSync())

    println(seqAndTakeLast(delayedIO, delayedIO2).unsafeRunSync())
    println(seqAndTakeFirst(delayedIO, delayedIO2).unsafeRunSync())
//    forever2(delayedIO).unsafeRunSync()

    println(sumIO(20000).unsafeRunSync())
    println(fibo(100).unsafeRunSync())
  }
}
