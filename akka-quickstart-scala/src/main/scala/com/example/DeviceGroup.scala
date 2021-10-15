package com.example

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}

object DeviceGroup {
  def apply(groupId: String): Behavior[Command] =
    Behaviors.setup(context => new DeviceGroup(context, groupId))

  trait Command

  private final case class DeviceTerminated(device: ActorRef[Device.Command], groupId: String, deviceId: String)
    extends Command
}

class DeviceGroup(context: ActorContext[DeviceGroup.Command], groupId: String)
  extends AbstractBehavior[DeviceGroup.Command](context) {

  context.log.info("DeviceGroup {} started", groupId)

  private var deviceIdToActor = Map.empty[String, ActorRef[Device.Command]]

  override def onMessage(msg: DeviceGroup.Command): Behavior[DeviceGroup.Command] = {
    msg match {
      case trackMsg@DeviceManager.RequestTrackDevice(`groupId`, deviceId, replyTo) =>
        val optActorRef: Option[ActorRef[Device.Command]] = deviceIdToActor.get(deviceId)
        optActorRef match {
          case Some(deviceActor) => replyTo ! DeviceManager.DeviceRegistered(deviceActor)
          case None =>
            context.log.info("Creating device actor for {}", trackMsg.deviceId)
            val deviceActor = context.spawn(Device(groupId, deviceId), s"device-$deviceId")
            deviceIdToActor += deviceId -> deviceActor
            replyTo ! DeviceManager.DeviceRegistered(deviceActor)
        }
        this
      case DeviceManager.RequestTrackDevice(gId, _, _) =>
        context.log.warn("Ignoring TrackDevice request for {}. This actor is responsible for {}.", gId, groupId)
        this
    }
  }


  override def onSignal: PartialFunction[Signal, Behavior[DeviceGroup.Command]] = {
    case PostStop =>
      context.log.info("DeviceGroup {} stopped", groupId)
      this
  }
}
