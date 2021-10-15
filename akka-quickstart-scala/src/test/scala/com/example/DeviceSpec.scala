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
  }
}
