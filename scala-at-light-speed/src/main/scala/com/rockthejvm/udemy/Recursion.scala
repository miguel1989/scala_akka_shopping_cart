package com.rockthejvm.udemy

import scala.annotation.tailrec

object Recursion extends App {

  def factorial(n: Int): Int = {
    if (n <= 1) 1
    else n * factorial(n - 1)
  }

  def tailFactorial(n: Int): Int = {
    @tailrec
    def factHelper(x: Int, accumulator: Int): Int = {
      if (x <= 1) accumulator
      else factHelper(x - 1, x * accumulator)
    }

    factHelper(n, 1)
  }

  @tailrec
  def tailConcatStrings(str: String, count: Int, result: String): String = {
    if (count <= 1) {
      return result + str
    }
    tailConcatStrings(str, count - 1, result + str)
  }

  //miguel
  @tailrec
  def tailIsPrime(n: Int, divider: Int): Boolean = {
    if (divider <= 1) true
    else if (n % divider == 0) false
    else tailIsPrime(n, divider - 1)
  }

  //daniel
  def isPrime(n: Int): Boolean = {
    def isPrimeTail(t: Int, isStillPrime: Boolean): Boolean = {
      if (!isStillPrime) false
      else if (t <= 1) true
      else isPrimeTail(t - 1, n % t != 0 && isStillPrime)
    }

    isPrimeTail(n / 2, isStillPrime = true)
  }

  //f0 = 0
  //f1 = 1
  //f(n) = f(n-1) + f(n-2)
  //0,1,1,2,3,5,8,13
  def fibo(n: Int): Int = {
    def tailFibo(i: Int, res1: Int, res2: Int): Int = {
      if (i >= n) res1
      else tailFibo(i + 1, res1 + res2, res1)
    }
    if (n <= 2){
      return 1
    }
    tailFibo(2, 1, 1)
  }


  println(factorial(5))
  println(tailFactorial(5))
  println(tailConcatStrings("Abc", 5, ""))
  println(tailIsPrime(6, 5))
  println(tailIsPrime(7, 6))
}
