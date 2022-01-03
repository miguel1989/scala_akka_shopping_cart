package com.rockthejvm.udemy.catts.part2

import cats.effect.{IO, IOApp, Resource}
import com.rockthejvm.udemy.catts.DebugWrapper

import java.io.{File, FileReader}
import java.util.Scanner
import scala.concurrent.duration._
import scala.language.postfixOps

object Resources extends IOApp.Simple {

  class Connection(url: String) {
    def open(): IO[String] = IO(s"opening connection to $url").debugCustom
    def close(): IO[String] = IO(s"closing connection to $url").debugCustom
  }

  val asyncFetchUrl: IO[Unit] = for {
    fib <- (new Connection("aaa.com").open() *> IO.sleep(1.second)).start
    _ <- IO.sleep(1.second) *> fib.cancel
  } yield () //loose resources

  val correctAsyncFetchUrl: IO[Unit] = for {
    conn <- IO(new Connection("aaa.com"))
    fib <- (conn.open() *> IO.sleep(2.second)).onCancel(conn.close().void).start
    _ <- IO.sleep(1.second) *> fib.cancel
  } yield ()

  //the bracket pattern
  val bracketFetchUrl = IO(new Connection("aaa.com"))
    .bracket(conn => conn.open() *> IO.sleep(2.second))(conn => conn.close().void)

  def openFileScanner(path: String): IO[Scanner] = IO(new Scanner(new FileReader(new File(path))))

  def readLineByLine(scanner: Scanner): IO[Unit] =
    if (scanner.hasNextLine)
      IO(scanner.nextLine()).debugCustom >> IO.sleep(100 millis) >> readLineByLine(scanner)
    else
      IO.unit

  def bracketRead(path: String): IO[Unit] = openFileScanner(path)
    .bracket(scanner => readLineByLine(scanner)
//      while (scanner.hasNext) {
//        IO(scanner.next()).debugCustom >> IO.sleep(100 millis)
//      }
//      IO("done").debugCustom.void
    )(scanner => IO(scanner.close()).debugCustom)


  //not looking very good, nesting!!
  def connectionFromConfig(path: String): IO[Unit] = openFileScanner(path).bracket(scanner => {
    IO(new Connection(scanner.nextLine()))
      .bracket(conn => conn.open().debugCustom >> IO.never)(conn => conn.close().debugCustom.void)
  })(scanner => IO("closing file").debugCustom >> IO(scanner.close()))

  val connectionResource: Resource[IO, Connection] = Resource.make(IO(new Connection("aaa.com")))(conn => conn.close().debugCustom.void)
  val resourceFetchUrl: IO[Unit] = for {
    fib <- connectionResource.use(conn => conn.open().debugCustom >> IO.never).start
    _ <- IO.sleep(1.second) >> fib.cancel
  } yield ()

  override def run: IO[Unit] = correctAsyncFetchUrl.void
}
