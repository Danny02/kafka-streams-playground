package com.example

import com.ovoenergy.kafka.serialization.circe.{circeJsonDeserializer, circeJsonSerializer}
import com.ovoenergy.kafka.serialization.core
import io.circe.{Decoder, Encoder, KeyDecoder, KeyEncoder}
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serdes}

trait CirceKafkaImplicits extends CirceKafkaLowerImplicits {

  implicit def genKeySerde[T](implicit ke: KeyEncoder[T], kd: KeyDecoder[T]): Serde[T] = {
    val serde      = Serdes.String()
    val serializer = core.serializer((t, data: T) => serde.serializer().serialize(t, ke(data)))
    val deserializer =
      core.deserializer { (t, data) =>
        if (data == null)
          null.asInstanceOf[T]
        else
          kd(serde.deserializer().deserialize(t, data)).get
      }
    Serdes.serdeFrom(serializer, deserializer)
  }

}

trait CirceKafkaLowerImplicits {

  def circeJsonDeserializerNullAware[T: Decoder]: Deserializer[T] = core.deserializer {
    (topic, data) =>
      if (data == null)
        null.asInstanceOf[T]
      else
        circeJsonDeserializer.deserialize(topic, data)
  }

  implicit def genSerde[T: Encoder: Decoder] =
    Serdes.serdeFrom(circeJsonSerializer, circeJsonDeserializerNullAware)
}
