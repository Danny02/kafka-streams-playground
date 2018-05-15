package com.example

import java.util.concurrent.CountDownLatch

import com.lightbend.kafka.scala.streams.StreamsBuilderS
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

import scala.collection.JavaConverters.mapAsJavaMapConverter

trait StreamsApp {
  val builder = new StreamsBuilderS()

  val properties: Map[_, _]

  def main(args: Array[String]): Unit = {
    val topology = builder.build()
    println(topology.describe())
    val streams = new KafkaStreams(topology, new StreamsConfig(properties.asJava))

    val latch = new CountDownLatch(1)
    // attach shutdown handler to catch control-c
    Runtime.getRuntime.addShutdownHook(new Thread("streams-shutdown-hook") {
      override def run(): Unit = {
        println("Trying to shutdown stream")
        streams.close
        println("Stream shutdown")
        latch.countDown()
      }
    })

    try {
      println("Starting Stream")
      streams.start
      println("Stream started")
      latch.await()
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        System.exit(1)
    }
  }
}
