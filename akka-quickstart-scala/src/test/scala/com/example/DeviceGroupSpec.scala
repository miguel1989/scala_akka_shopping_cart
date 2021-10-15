package com.example

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpecLike

class DeviceGroupSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike{

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
      respondTempProbe.expectMessage(Device.RespondTemperature(43, Some(37.1)))
    }

    "ignore requests for otherGroup" in {
      val deviceRegisteredProbe = createTestProbe[DeviceManager.DeviceRegistered]()
      val deviceGroupActor = spawn(DeviceGroup("myGroup"))

      deviceGroupActor ! DeviceManager.RequestTrackDevice("myGroup2", "device1", deviceRegisteredProbe.ref)
      deviceRegisteredProbe.expectNoMessage(500.milliseconds)
    }
  }
}
