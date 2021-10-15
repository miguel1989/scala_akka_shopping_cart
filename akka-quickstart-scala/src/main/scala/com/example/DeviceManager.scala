package com.example

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}

object DeviceManager {
  def apply(groupId: String): Behavior[Command] =
    Behaviors.setup(context => new DeviceManager(context, groupId))

  trait Command

  final case class RequestTrackDevice(groupId: String, deviceId: String, replyTo: ActorRef[DeviceRegistered])
    extends DeviceManager.Command with DeviceGroup.Command

  final case class DeviceRegistered(device: ActorRef[Device.Command])
}

class DeviceManager(context: ActorContext[DeviceManager.Command], groupId: String)
  extends AbstractBehavior[DeviceManager.Command](context) {

  context.log.info("DeviceManager {} started", groupId)

  override def onMessage(msg: DeviceManager.Command): Behavior[DeviceManager.Command] = ???

  override def onSignal: PartialFunction[Signal, Behavior[DeviceManager.Command]] = {
    case PostStop =>
      context.log.info("DeviceManager {} stopped", groupId)
      this
  }
}
