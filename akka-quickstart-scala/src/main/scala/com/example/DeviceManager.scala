package com.example

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}

object DeviceManager {
  def apply(): Behavior[DeviceManager.Command] = Behaviors.setup(context => new DeviceManager(context))

  trait Command

  final case class RequestTrackDevice(groupId: String, deviceId: String, replyTo: ActorRef[DeviceRegistered])
    extends Command with DeviceGroup.Command

  final case class DeviceRegistered(device: ActorRef[Device.Command])

  final case class RequestDeviceList(requestId: Long, groupId: String, replyTo: ActorRef[ReplyDeviceList])
    extends Command with DeviceGroup.Command

  final case class ReplyDeviceList(requestId: Long, ids: Set[String])

  private final case class DeviceGroupTerminated(groupId: String) extends DeviceManager.Command
}

class DeviceManager(context: ActorContext[DeviceManager.Command])
  extends AbstractBehavior[DeviceManager.Command](context) {
  import DeviceManager._

  context.log.info("DeviceManager started")

  var groupIdToActor = Map.empty[String, ActorRef[DeviceGroup.Command]]

  override def onMessage(msg: DeviceManager.Command): Behavior[DeviceManager.Command] = {
    msg match {
      case trackMsg@DeviceManager.RequestTrackDevice(groupId, _, _) =>
        val optDeviceGroupActor = groupIdToActor.get(groupId)
        optDeviceGroupActor match {
          case Some(deviceGroupActor) =>
            deviceGroupActor ! trackMsg
          case None =>
            context.log.info("Creating device group for {}", groupId)
            val deviceGroupActor = context.spawn(DeviceGroup(groupId), s"group-$groupId")
            context.watchWith(deviceGroupActor, DeviceGroupTerminated(groupId))
            deviceGroupActor ! trackMsg
            groupIdToActor += (groupId -> deviceGroupActor)
        }
        this
      case requestMsg@DeviceManager.RequestDeviceList(requestId, groupId, replyTo) =>
        groupIdToActor.get(groupId) match {
          case Some(deviceGroupActor) =>
            deviceGroupActor ! requestMsg
          case None =>
            replyTo ! DeviceManager.ReplyDeviceList(requestId, Set.empty)
        }
        this
      case DeviceGroupTerminated(groupId) =>
        context.log.info("Device group actor for {} has been terminated", groupId)
        groupIdToActor -= groupId
        this
    }
  }

  override def onSignal: PartialFunction[Signal, Behavior[DeviceManager.Command]] = {
    case PostStop =>
      context.log.info("DeviceManager stopped")
      this
  }

}
