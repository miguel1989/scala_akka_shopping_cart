package com.example

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import java.time.Duration
import scala.concurrent.Future

final case class DeviceRequest(name: String)

class DeviceManagerRoutes(deviceManager: ActorRef[DeviceManager.Command])(implicit val system: ActorSystem[_]) extends FailFastCirceSupport {

  import io.circe.generic.auto._ //implicit encoders and decoders

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout: Timeout = Timeout.create(Duration.ofSeconds(3))

  def getDeviceList(groupName: String): Future[DeviceManager.ReplyDeviceList] = {
    deviceManager.ask(DeviceManager.RequestDeviceList(1, groupName, _))
  }

  def registerDevice(groupName: String, deviceName: String): Future[DeviceManager.DeviceRegistered] = {
    deviceManager.ask(DeviceManager.RequestTrackDevice(groupName, deviceName, _))
  }

  val topLevelRoute: Route = pathPrefix("api")(
    concat(
      pathSingleSlash { //todo wtf is this!?
        get {
          complete("single slash!")
        }
      },
      path("pong") {
        get {
          complete("PONG!")
        }
      },
      pathPrefix("device-manager")(deviceManagerRoute)
    )
  )

  def deviceManagerRoute: Route = concat(
    pathEnd {
      get {
        complete("This is an empty device manager api path")
      }
    },
    path(Segment / "device-list") { groupName =>
      get {
        complete(getDeviceList(groupName))
      }
    },
    path(Segment / "device") { groupName =>
      post {
        entity(as[DeviceRequest]) { deviceRequest =>
          onSuccess(registerDevice(groupName, deviceRequest.name)) { response =>
            complete("device " + deviceRequest.name + " is registered")
          }
        }
      }
    }
  )
}
