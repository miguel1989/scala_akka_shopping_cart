package com.rockthejvm.udemy.advanced.part3

import java.util.concurrent.Executors

object Intro extends App {
  val aThread = new Thread(() => println("running in a thread"))

  aThread.start()
  aThread.join()//block unitll finished

  val pool = Executors.newFixedThreadPool(10)

  pool.execute(() => {
    println("Hello")
  })


  //producer-consumer problem

  class SimpleContainer {
    private var value = 0
    def isEmpty: Boolean = value == 0
    def get: Int =  {
      val result = value
      value = 0
      result
    }
    def set(newVal: Int): Unit = value = newVal
  }

  def naiveProdCons() ={
    val container = new SimpleContainer()

    val consumer = new Thread(() => {
      println("waiting")
      while (container.isEmpty) {
        println("actively waiting")
      }
      println("i have consumed " + container.get)
    })

    val producer = new Thread(() => {
      println("computing")
      Thread.sleep(1000)
      val result = 42
      println("produced " + result)

      container.set(result)
    })

    consumer.start()
    producer.start()
  }
}
