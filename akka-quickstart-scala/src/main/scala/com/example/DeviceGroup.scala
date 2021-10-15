package com.example

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import com.example.DeviceGroup.DeviceTerminated
import com.example.DeviceManager.DeviceRegistered

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
      //this is more elegant way to handle RequestTrackDevice
//      case trackMsg@DeviceManager.RequestTrackDevice(`groupId`, deviceId, replyTo) =>
//        registerDevice(deviceId, replyTo)
//        this
      case DeviceManager.RequestTrackDevice(gId, deviceId, replyTo) =>
        if (gId != groupId) {
          context.log.warn("Ignoring TrackDevice request for {}. This actor is responsible for {}.", gId, groupId)
          return Behaviors.unhandled
        }
        registerDevice(deviceId, replyTo)
        this
      case DeviceGroup.DeviceTerminated(_, _, deviceId) =>
        //todo maybe check that deviceId is there!?
        deviceIdToActor -= deviceId
        this
      case DeviceManager.RequestDeviceList(requestId, groupIdParam, replyTo) =>
        if (groupId != groupIdParam) {
          context.log.warn("Ignoring RequestDeviceList request for {}. This actor is responsible for {}.", groupIdParam, groupId)
          return Behaviors.unhandled
        }
        replyTo ! DeviceManager.ReplyDeviceList(requestId, deviceIdToActor.keySet)
        this
    }
  }

  private def registerDevice(deviceId: String, replyTo: ActorRef[DeviceRegistered]): Unit = {
    val optActorRef: Option[ActorRef[Device.Command]] = deviceIdToActor.get(deviceId)
    optActorRef match {
      case Some(deviceActor) =>
        replyTo ! DeviceManager.DeviceRegistered(deviceActor)
      case None =>
        context.log.info("Creating device actor for {}", deviceId)
        val deviceActor = context.spawn(Device(groupId, deviceId), s"device-$deviceId")
        context.watchWith(deviceActor, DeviceTerminated(deviceActor, groupId, deviceId))
        deviceIdToActor += (deviceId -> deviceActor)
        replyTo ! DeviceManager.DeviceRegistered(deviceActor)
    }
  }


  override def onSignal: PartialFunction[Signal, Behavior[DeviceGroup.Command]] = {
    case PostStop =>
      context.log.info("DeviceGroup {} stopped", groupId)
      this
  }
}
