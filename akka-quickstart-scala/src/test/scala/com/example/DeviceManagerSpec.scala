package com.example

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class DeviceManagerSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "DeviceManager actor" must {
    "be able to register groups with devices" in {
      val deviceManagerActor = spawn(DeviceManager(), "DeviceManager")
      val deviceRegisteredProbe = createTestProbe[DeviceManager.DeviceRegistered]()

      deviceManagerActor ! DeviceManager.RequestTrackDevice("myGroup1", "device1", deviceRegisteredProbe.ref)
      deviceRegisteredProbe.receiveMessage()

      deviceManagerActor ! DeviceManager.RequestTrackDevice("myGroup1", "device2", deviceRegisteredProbe.ref)
      deviceRegisteredProbe.receiveMessage()

      deviceManagerActor ! DeviceManager.RequestTrackDevice("myGroup2", "device3", deviceRegisteredProbe.ref)
      deviceRegisteredProbe.receiveMessage()

      val replyDeviceListProbe = createTestProbe[DeviceManager.ReplyDeviceList]()
      deviceManagerActor ! DeviceManager.RequestDeviceList(1, "wrong", replyDeviceListProbe.ref)
      replyDeviceListProbe.expectMessage(DeviceManager.ReplyDeviceList(1, Set.empty))

      deviceManagerActor ! DeviceManager.RequestDeviceList(2, "myGroup1", replyDeviceListProbe.ref)
      replyDeviceListProbe.expectMessage(DeviceManager.ReplyDeviceList(2, Set("device1", "device2")))

      deviceManagerActor ! DeviceManager.RequestDeviceList(3, "myGroup2", replyDeviceListProbe.ref)
      replyDeviceListProbe.expectMessage(DeviceManager.ReplyDeviceList(3, Set("device3")))
    }
  }
}
