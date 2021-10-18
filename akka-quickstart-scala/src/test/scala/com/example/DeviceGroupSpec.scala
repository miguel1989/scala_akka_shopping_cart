package com.example

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpecLike

class DeviceGroupSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "DeviceGroup actor" must {
    "be able to register a device actor" in {
      val deviceRegisteredProbe = createTestProbe[DeviceManager.DeviceRegistered]()
      val deviceGroupActor = spawn(DeviceGroup("myGroup"))

      deviceGroupActor ! DeviceManager.RequestTrackDevice("myGroup", "device1", deviceRegisteredProbe.ref)
      val response1 = deviceRegisteredProbe.receiveMessage()
      val deviceActor1 = response1.device

      deviceGroupActor ! DeviceManager.RequestTrackDevice("myGroup", "device2", deviceRegisteredProbe.ref)
      val response2 = deviceRegisteredProbe.receiveMessage()
      val deviceActor2 = response2.device

      deviceGroupActor ! DeviceManager.RequestTrackDevice("myGroup", "device1", deviceRegisteredProbe.ref)
      val response3 = deviceRegisteredProbe.receiveMessage()
      val deviceActor3 = response3.device

      deviceActor1 should !==(deviceActor2)
      deviceActor1 should ===(deviceActor3)

      //check that deviceActor can read temperature
      val respondTempProbe = createTestProbe[Device.RespondTemperature]()
      val tempRecordedProbe = createTestProbe[Device.TemperatureRecorded]()
      deviceActor1 ! Device.RecordTemperature(42, 37.1, tempRecordedProbe.ref)
      tempRecordedProbe.expectMessage(Device.TemperatureRecorded(requestId = 42))

      deviceActor1 ! Device.ReadTemperature(43, respondTempProbe.ref)
      respondTempProbe.expectMessage(Device.RespondTemperature(43, "device1", Some(37.1)))
    }

    "ignore requests for otherGroup" in {
      val deviceRegisteredProbe = createTestProbe[DeviceManager.DeviceRegistered]()
      val deviceGroupActor = spawn(DeviceGroup("myGroup"))

      deviceGroupActor ! DeviceManager.RequestTrackDevice("myGroup2", "device1", deviceRegisteredProbe.ref)
      deviceRegisteredProbe.expectNoMessage(500.milliseconds)
    }

    "be able to list active devices and shut down 1 device" in {
      val deviceRegisteredProbe = createTestProbe[DeviceManager.DeviceRegistered]()
      val replyDeviceListProbe = createTestProbe[DeviceManager.ReplyDeviceList]()
      val deviceGroupActor = spawn(DeviceGroup("myGroup"))

      deviceGroupActor ! DeviceManager.RequestTrackDevice("myGroup", "device1", deviceRegisteredProbe.ref)
      val response1 = deviceRegisteredProbe.receiveMessage()
      val deviceActor1 = response1.device

      deviceGroupActor ! DeviceManager.RequestTrackDevice("myGroup", "device2", deviceRegisteredProbe.ref)
      deviceRegisteredProbe.receiveMessage()

      deviceGroupActor ! DeviceManager.RequestDeviceList(1, "myGroup", replyDeviceListProbe.ref)
      replyDeviceListProbe.expectMessage(DeviceManager.ReplyDeviceList(1, Set("device1", "device2")))

      //-----------------------------------------------------------------------------------
      deviceActor1 ! Device.Passivate
      deviceRegisteredProbe.expectTerminated(deviceActor1, deviceRegisteredProbe.remainingOrDefault)

      // using awaitAssert to retry because it might take longer for the groupActor
      // to see the Terminated, that order is undefined
      deviceRegisteredProbe.awaitAssert {
        deviceGroupActor ! DeviceManager.RequestDeviceList(2, "myGroup", replyDeviceListProbe.ref)
        replyDeviceListProbe.expectMessage(DeviceManager.ReplyDeviceList(2, Set("device2")))
      }
    }
  }
}
