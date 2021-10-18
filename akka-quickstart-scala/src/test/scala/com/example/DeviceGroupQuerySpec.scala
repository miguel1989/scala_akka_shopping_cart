package com.example

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorRef
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt

class DeviceGroupQuerySpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "DeviceGroupQuerySpec actor" must {
    "be able to return temperatures for working devices" in {
      val device1 = spawn(Device("groupId", "deviceId1"))
      val device2 = spawn(Device("groupId", "deviceId2"))
      val deviceTempRecordedProbe = createTestProbe[Device.TemperatureRecorded]()

      device1 ! Device.RecordTemperature(1, 37.1, deviceTempRecordedProbe.ref)
      deviceTempRecordedProbe.expectMessage(Device.TemperatureRecorded(requestId = 1))

      val deviceManagerRespondAllProbe = createTestProbe[DeviceManager.RespondAllTemperatures]()

      val deviceIdToActor: Map[String, ActorRef[Device.Command]] = Map("deviceId1" -> device1, "deviceId2" -> device2)
      val deviceGroupQuery = spawn(DeviceGroupQuery(deviceIdToActor, 2, deviceManagerRespondAllProbe.ref, 5.seconds))

      val response = deviceManagerRespondAllProbe.receiveMessage()
      response.requestId should === (2)
      response.temperatures should === (Map("deviceId1" -> DeviceManager.Temperature(37.1), "deviceId2" -> DeviceManager.TemperatureNotAvailable))
    }
  }
}
