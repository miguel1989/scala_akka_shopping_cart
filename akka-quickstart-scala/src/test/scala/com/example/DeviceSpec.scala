package com.example

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class DeviceSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Device actor" must {

    "reply with empty temperature" in {
      val probe = createTestProbe[Device.RespondTemperature]()
      val deviceActor = spawn(Device("groupId", "deviceId"))

      deviceActor ! Device.ReadTemperature(requestId = 42, probe.ref)

      val response = probe.receiveMessage()
      response.requestId should ===(42)
      response.value should ===(None)
    }

    "reply with last temp reading" in {
      val respondTempProbe = createTestProbe[Device.RespondTemperature]()
      val tempRecordedProbe = createTestProbe[Device.TemperatureRecorded]()
      val deviceActor = spawn(Device("groupId", "deviceId"))

      deviceActor ! Device.RecordTemperature(42, 37.1, tempRecordedProbe.ref)
      tempRecordedProbe.expectMessage(Device.TemperatureRecorded(requestId = 42))

      deviceActor ! Device.ReadTemperature(43, respondTempProbe.ref)
      val response = respondTempProbe.receiveMessage()
      response.requestId should ===(43)
      response.value should ===(Some(37.1))
    }
  }
}
