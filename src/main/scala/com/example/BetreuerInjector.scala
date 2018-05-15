package com.example

import java.nio.ByteBuffer
import java.util.UUID

import monix.execution.Scheduler
import monix.kafka._
import monix.reactive.Observable
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.streams.StreamsConfig._
import monix.execution.Scheduler.Implicits.global

import scala.util.Random

// Import the Circe generic support

import io.circe.generic.auto._
import io.circe.syntax._

object BetreuerInjector extends StreamsApp {

  val names = List("Herbert",
                   "Daniel",
                   "Luzifer",
                   "Tommi",
                   "Albert",
                   "Andreas",
                   "Moritz",
                   "Alexander",
                   "Max",
                   "Gretel")

  val famNames =
    List("Heinrich", "Müller", "Wöhrle", "Meier", "Palm", "Qiao", "Schlemmer", "Seifert", "Harms")

  val startBetreuer = List.tabulate(10)(rndBetreuer)

  override val properties = Map(
          APPLICATION_ID_CONFIG    -> "m2n-resolver",
          BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092"
  )

  val producerCfg = KafkaProducerConfig.default.copy(
          bootstrapServers = List("127.0.0.1:9092")
  )

  val producer = KafkaProducerSink[String, String](producerCfg, Scheduler.io())
  examples
    .bufferIntrospective(1024)
    .consumeWith(producer)
    .runAsync

  def examples: Observable[ProducerRecord[String, String]] = {
    Observable
      .fromIterable(startBetreuer)
      .map(b => new ProducerRecord(Topics.BETREUER, b.id.toString, b.asJson.noSpaces))
      .doOnNext(println)
  }

  def rndBetreuer(id: Int) = {
    Betreuer(UUID.nameUUIDFromBytes(ByteBuffer.allocate(4).putInt(id).array()),
             s"${names(Random.nextInt(names.size))} ${famNames(Random.nextInt(famNames.size))}")
  }
}
