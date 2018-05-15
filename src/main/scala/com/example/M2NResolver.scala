package com.example

import com.lightbend.kafka.scala.streams.ImplicitConversions._
import com.lightbend.kafka.scala.streams.KTableS
import io.circe.generic.auto._
import org.apache.kafka.streams.StreamsConfig._

// Import the Circe generic support

object M2NResolver extends StreamsApp with CirceKafkaImplicits{

  override val properties = Map(
          APPLICATION_ID_CONFIG       -> "m2n-resolver",
          BOOTSTRAP_SERVERS_CONFIG    -> "localhost:9092",
          PROCESSING_GUARANTEE_CONFIG -> EXACTLY_ONCE,
  )

  val betreuer = builder.stream[BetreuerId, Betreuer](Topics.BETREUER)

  val zuordnungen = builder.stream[SchulId, Zuordnungen](Topics.ZUORDNUNGEN)

  val latestBetreuer: KTableS[BetreuerId, Betreuer] =
    betreuer.groupByKey.reduce((_, newval) => newval)

  val latestZuordnungen: KTableS[SchulId, Zuordnungen] =
    zuordnungen.groupByKey.reduce((_, newval) => newval)

  case class SchulZuInfo(schulId: SchulId, info: String)

  val allSchulenFromBetreuer: KTableS[BetreuerId, List[SchulZuInfo]] = zuordnungen
    .flatMap((k, v) => v.zuordnungen.map(z => (z.betreuerId, SchulZuInfo(v.schulId, z.info))))
    .mapValues(List(_))
    .groupByKey
    .reduce(_ ++ _)

  case class BetreuerZuordnungInfo(info: String, betrName: String)

  val allBetreuerInfoForSchule: KTableS[SchulId, Map[BetreuerId, BetreuerZuordnungInfo]] =
    allSchulenFromBetreuer
      .join(latestBetreuer, (_: List[SchulZuInfo], _: Betreuer))
      .toStream
      .flatMap {
        case (_, (zuInfoList, betreuer)) =>
          zuInfoList.map(s =>
            (s.schulId, betreuer.id -> BetreuerZuordnungInfo(s.info, betreuer.name)))
      }
      .mapValues(Map(_))
      .groupByKey
      .reduce(_ ++ _)

  val result: KTableS[SchulId, List[BetreuerZuordnungInfo]] = latestZuordnungen
    .join(allBetreuerInfoForSchule, (zuords, info: Map[BetreuerId, BetreuerZuordnungInfo]) => {
      zuords.zuordnungen.map(z => info.get(z.betreuerId).get)
    })

  result.toStream.peek((k, v) => println(s"$k: $v"))
}
