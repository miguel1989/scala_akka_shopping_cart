package com.rockthejvm.udemy.advanced.part4

object PimpMyLibrary extends App {
  implicit class RichInt(val value: Int) extends AnyVal {
    def isEven: Boolean = value % 2 == 0
    def sqrt: Double = Math.sqrt(value)
  }

  42.isEven

  implicit class RichString(str: String) {
    def encrypt(num: Int): String = str.map(c => (c + num).asInstanceOf[Char])
  }

  implicitly
}
