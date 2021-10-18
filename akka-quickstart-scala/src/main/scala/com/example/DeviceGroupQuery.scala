package com.example

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, TimerScheduler}
import com.example.DeviceGroupQuery.{CollectionTimeout, DeviceTerminated, WrappedRespondTemperature}
import com.example.DeviceManager.{DeviceNotAvailable, DeviceTimedOut, RespondAllTemperatures, Temperature, TemperatureNotAvailable, TemperatureReading}

import scala.concurrent.duration.FiniteDuration

object DeviceGroupQuery {

  def apply(
             deviceIdToActor: Map[String, ActorRef[Device.Command]],
             requestId: Long,
             requester: ActorRef[DeviceManager.RespondAllTemperatures],
             timeout: FiniteDuration): Behavior[Command] = {
    Behaviors.setup { context =>
      Behaviors.withTimers { timers =>
        new DeviceGroupQuery(deviceIdToActor, requestId, requester, timeout, context, timers)
      }
    }
  }
  trait Command

  private case object CollectionTimeout extends Command

  final case class WrappedRespondTemperature(response: Device.RespondTemperature) extends Command

  private final case class DeviceTerminated(deviceId: String) extends Command
}

class DeviceGroupQuery(
                        deviceIdToActor: Map[String, ActorRef[Device.Command]],
                        requestId: Long,
                        requester: ActorRef[DeviceManager.RespondAllTemperatures],
                        timeout: FiniteDuration,
                        context: ActorContext[DeviceGroupQuery.Command],
                        timers: TimerScheduler[DeviceGroupQuery.Command])
  extends AbstractBehavior[DeviceGroupQuery.Command](context) {

  context.log.info("DeviceGroupQuery({}) started with timeout {}", requestId, timeout)

  var stillWaiting: Set[String] = deviceIdToActor.keySet
  var deviceTempMap: Map[String, TemperatureReading] = Map.empty

  timers.startSingleTimer(CollectionTimeout, CollectionTimeout, timeout)

  private val respondTemperatureAdapter: ActorRef[Device.RespondTemperature] =
    context.messageAdapter(item => WrappedRespondTemperature(item)) ///WrappedRespondTemperature.apply

  deviceIdToActor.foreach {
    case (deviceId, device) =>
      context.watchWith(device, DeviceTerminated(deviceId))
      device ! Device.ReadTemperature(0, respondTemperatureAdapter) //instead of context.self
  }

  override def onMessage(msg: DeviceGroupQuery.Command): Behavior[DeviceGroupQuery.Command] = {
    msg match {
      case WrappedRespondTemperature(response) =>
        val temperatureReading = response.value match {
          case Some(value) => Temperature(value)
          case None => TemperatureNotAvailable
        }

        deviceTempMap += (response.deviceId -> temperatureReading)
        stillWaiting -= response.deviceId

        respondWhenAllCollected()

      case DeviceTerminated(deviceId) =>
        if (stillWaiting(deviceId)) {
          deviceTempMap += (deviceId -> DeviceNotAvailable)
          stillWaiting -= deviceId
        }

        respondWhenAllCollected()

      case CollectionTimeout =>
        deviceTempMap ++= stillWaiting.map(deviceId => (deviceId -> DeviceTimedOut))
        stillWaiting = Set.empty

        respondWhenAllCollected()
    }

  }

  private def respondWhenAllCollected(): Behavior[DeviceGroupQuery.Command] = {
    if (stillWaiting.isEmpty) {
      requester ! RespondAllTemperatures(requestId, deviceTempMap)
      return Behaviors.stopped
    }
    this
  }
}
