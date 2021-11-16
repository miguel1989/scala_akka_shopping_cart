package com.rockthejvm.udemy

import io.circe.Decoder.Result
import io.circe._
import io.circe.generic.semiauto._

object CirceCodecs extends App {
  final case class Data(key1: String, key2: Int)
  object Data {
    implicit val decoder: Decoder[Data] = deriveDecoder
    implicit val encoder: Encoder[Data] = deriveEncoder
  }
}
