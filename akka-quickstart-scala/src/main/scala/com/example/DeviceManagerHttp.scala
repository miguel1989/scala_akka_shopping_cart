package com.example

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.util.{Failure, Success}

object DeviceManagerHttp {

  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext
    val futureBinding = Http().newServerAt("localhost", 8081).bind(routes)

    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing](context => {
      val deviceManager = context.spawn(DeviceManager(), "deviceManager")

      context.watch(deviceManager)

      val deviceManagerRoutes = new DeviceManagerRoutes(deviceManager)(context.system)
      startHttpServer(deviceManagerRoutes.topLevelRoute)(context.system)

      Behaviors.empty
    })

    ActorSystem[Nothing](rootBehavior, "httpServer")
  }
}
