package com.example

import java.nio.ByteBuffer
import java.util.UUID

import io.circe.{Decoder, Encoder}
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

object ZuordnungenInjector extends StreamsApp {

  val startZuordnungen = List.tabulate(5)(_ => rndZuordnungen)

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
      .fromIterable(startZuordnungen)
      .map(b => new ProducerRecord(Topics.ZUORDNUNGEN, b.schulId.toString, b.asJson.noSpaces))
      .doOnNext(println)
  }

  def rndZuordnungen = {
    Zuordnungen(UUID.randomUUID(), List.tabulate(10)(_ => rndZuordnung))
  }

  def rndZuordnung = {
    Zuordnung(UUID.nameUUIDFromBytes(ByteBuffer.allocate(4).putInt(Random.nextInt(10)).array()), Random.nextString(10))
  }
}
