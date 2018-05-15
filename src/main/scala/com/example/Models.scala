package com.example

import io.circe.{Decoder, Encoder, KeyDecoder, KeyEncoder}

case class BetreuerId(id: String) extends AnyVal
object BetreuerId extends ToStringCoder[BetreuerId] {
  override val extractor = (_:BetreuerId).id
  override val factory = BetreuerId(_)
}

case class SchulId(id: String) extends AnyVal
object SchulId extends ToStringCoder[SchulId] {
  override val extractor = (_:SchulId).id
  override val factory = SchulId(_)
}

trait ToStringCoder[T] {
  val extractor: T => String
  val factory: String => T
  implicit lazy val stringKeyEncoder = KeyEncoder.encodeKeyString.contramap(extractor)
  implicit lazy val stringKeyDecoder = KeyDecoder.decodeKeyString.map(factory)
  implicit lazy val stringEncoder = Encoder.encodeString.contramap(extractor)
  implicit lazy val stringDecoder = Decoder.decodeString.map(factory)
}

case class Betreuer(id: BetreuerId, name: String)

case class Zuordnung(betreuerId: BetreuerId, info: String)
case class Zuordnungen(schulId: SchulId, zuordnungen: List[Zuordnung])
