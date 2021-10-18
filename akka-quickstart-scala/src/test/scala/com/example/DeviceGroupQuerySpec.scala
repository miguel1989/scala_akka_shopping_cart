package com.example

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorRef
import com.example.DeviceGroupQuery.WrappedRespondTemperature
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

    "be able to return DeviceNotAvailable if device is stopped" in {
      val device1 = spawn(Device("groupId", "deviceId1"))
      val device2 = createTestProbe[Device.Command]()//spawn(Device("groupId", "deviceId2"))
      val deviceTempRecordedProbe = createTestProbe[Device.TemperatureRecorded]()

      device1 ! Device.RecordTemperature(1, 37.1, deviceTempRecordedProbe.ref)
      deviceTempRecordedProbe.expectMessage(Device.TemperatureRecorded(requestId = 1))

      val deviceManagerRespondAllProbe = createTestProbe[DeviceManager.RespondAllTemperatures]()
      val deviceIdToActor: Map[String, ActorRef[Device.Command]] = Map("deviceId1" -> device1, "deviceId2" -> device2.ref)

      spawn(DeviceGroupQuery(deviceIdToActor, 2, deviceManagerRespondAllProbe.ref, 5.seconds))

      device2.expectMessageType[Device.ReadTemperature]

      device2.stop()

      val response = deviceManagerRespondAllProbe.receiveMessage()
      response.requestId should === (2)
      response.temperatures should === (Map("deviceId1" -> DeviceManager.Temperature(37.1), "deviceId2" -> DeviceManager.DeviceNotAvailable))
    }

    "be able to return temperature reading even if device is stopped" in {
      val device1 = createTestProbe[Device.Command]()
      val device2 = createTestProbe[Device.Command]()
      val deviceManagerRespondAllProbe = createTestProbe[DeviceManager.RespondAllTemperatures]()

      val deviceIdToActor: Map[String, ActorRef[Device.Command]] = Map("device1" -> device1.ref, "device2" -> device2.ref)

      val queryActor = spawn(DeviceGroupQuery(deviceIdToActor, 2, deviceManagerRespondAllProbe.ref, 5.seconds))

      device1.expectMessageType[Device.ReadTemperature]
      device2.expectMessageType[Device.ReadTemperature]

      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", Some(1.1)))
      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device2", Some(2.2)))

      device2.stop()

      val response = deviceManagerRespondAllProbe.receiveMessage()
      response.requestId should === (2)
      response.temperatures should === (Map("device1" -> DeviceManager.Temperature(1.1), "device2" -> DeviceManager.Temperature(2.2)))
    }

    "be able to return DeviceTimedOut if device does not answer in time" in {
      val device1 = createTestProbe[Device.Command]()
      val device2 = createTestProbe[Device.Command]()
      val deviceManagerRespondAllProbe = createTestProbe[DeviceManager.RespondAllTemperatures]()

      val deviceIdToActor: Map[String, ActorRef[Device.Command]] = Map("device1" -> device1.ref, "device2" -> device2.ref)

      val queryActor = spawn(DeviceGroupQuery(deviceIdToActor, 2, deviceManagerRespondAllProbe.ref, 100.millis))

      device1.expectMessageType[Device.ReadTemperature]
      device2.expectMessageType[Device.ReadTemperature]

      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", Some(1.1)))

      val response = deviceManagerRespondAllProbe.receiveMessage()
      response.requestId should === (2)
      response.temperatures should === (Map("device1" -> DeviceManager.Temperature(1.1), "device2" -> DeviceManager.DeviceTimedOut))
    }

  }
}
