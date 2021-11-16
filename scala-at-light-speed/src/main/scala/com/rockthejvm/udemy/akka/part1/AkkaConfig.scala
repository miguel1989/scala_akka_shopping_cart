package com.rockthejvm.udemy.akka.part1

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

case object AkkaConfig extends App {
  val configStr =
    """
      akka {
        loglevel = "DEBUG"
      }
      """.stripMargin

  val config = ConfigFactory.parseString(configStr)
  val system = ActorSystem("demo", ConfigFactory.load(config))
}
