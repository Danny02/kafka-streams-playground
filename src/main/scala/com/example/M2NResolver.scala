package com.example

import java.nio.charset.StandardCharsets
import java.util.UUID

import com.lightbend.kafka.scala.streams.KTableS
import com.lightbend.kafka.scala.streams.DefaultSerdes._
import com.lightbend.kafka.scala.streams.ImplicitConversions._
import com.ovoenergy.kafka.serialization.core
import com.ovoenergy.kafka.serialization.core.deserializer
import io.circe.{Decoder, Encoder, Error, Json}
import io.circe.generic.auto._
import io.circe.parser.parse
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serdes, Serializer}
import org.apache.kafka.streams.StreamsConfig._
import org.apache.kafka.streams.kstream.Materialized

// Import the Circe generic support

import com.ovoenergy.kafka.serialization.circe._

object M2NResolver extends StreamsApp {

//  def nullAwareSerializer[T](s: Serializer[T]): Serializer[Option[T]] =
//    core.serializer((topic , data) => data.map(s.serialize(topic, _)).getOrElse(null))
//
//  implicit def genserde[T: Encoder: Decoder] =
//    Serdes.serdeFrom(nullAwareSerializer(circeJsonSerializer), core.optionalDeserializer(circeJsonDeserializer))

  def circeJsonDeserializerNullAware[T: Decoder]: Deserializer[T] = deserializer { (topic, data) =>
    if (data == null)
      null.asInstanceOf[T]
    else
      circeJsonDeserializer.deserialize(topic, data)
  }

  implicit def genserde[T: Encoder: Decoder] =
    Serdes.serdeFrom(circeJsonSerializer, circeJsonDeserializerNullAware)

  override val properties = Map(
          APPLICATION_ID_CONFIG    -> "m2n-resolver2",
          BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092"
  )

  type BetreuerId = String
  type SchulId    = String

  val betreuer = builder.stream[BetreuerId, Betreuer](Topics.BETREUER)

  val zuordnungen = builder.stream[SchulId, Zuordnungen](Topics.ZUORDNUNGEN)

  val latestBetreuer: KTableS[BetreuerId, Betreuer] =
    betreuer.groupByKey.reduce((_, newval) => newval, "betreuer-store")

  val latestZuordnungen: KTableS[SchulId, Zuordnungen] =
    zuordnungen.groupByKey.reduce((_, newval) => newval, "zuordnungen-store")

  case class SchulZuInfo(schulId: UUID, info: String)

  val allSchulenFromBetreuer: KTableS[BetreuerId, List[SchulZuInfo]] = zuordnungen
    .flatMap((k, v) =>
      v.zuordnungen.map(z => (z.betreuerId.toString, SchulZuInfo(v.schulId, z.info))))
    .mapValues(List(_))
    .groupByKey
    .reduce(_ ++ _, "schulen-of-betreuer-store")

  case class SchulBetrInfo(schulId: UUID, info: String, betrName: String)

  val allBetreuerInfoForSchule: KTableS[SchulId, Map[UUID, (String, String)]] =
    allSchulenFromBetreuer
      .join(latestBetreuer, (_: List[SchulZuInfo], _: Betreuer))
      .toStream
      .peek((k, v) => s"$k: $v")
      .flatMap((bid, t) => t._1.map(s => (s.schulId.toString, (s.info, t._2.name, t._2.id))))
      .mapValues(t => Map(t._3 -> (t._1, t._2)))
      .groupByKey
      .reduce(_ ++ _, "betreuer-of-schule-store")

  allBetreuerInfoForSchule.toStream.peek((k, v) => s"$k: $v")

//
//  case class ZuordInfo(name: String, info: String)
//
//  val result: KTableS[SchulId, List[(String, String)]] = latestZuordnungen
//    .join(allBetreuerInfoForSchule, (zuords, info: Map[UUID, (String, String)]) => {
//      zuords.zuordnungen.map(z => info.get(z.betreuerId).get)
//    })
//
//  result.toStream.to("outputs-stream")
}
