package com.rockthejvm.udemy

import cats.effect.IO

package object catts {
  implicit class DebugWrapper[A](io: IO[A]){
    def debugCustom: IO[A] = for {
      a <- io
      thread = Thread.currentThread().getName
      _ = println(s"[${thread}] ${a}")
    } yield a
  }
}
